/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.lint

import com.intellij.psi.impl.compiled.ClsMethodImpl
import com.intellij.psi.impl.compiled.ClsParameterImpl
import kotlinx.metadata.jvm.annotations
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UAnonymousClass
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getContainingDeclaration
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getParameterForArgument
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.withContainingElements

/**
 * Returns whether this [UCallExpression] is invoked within the body of a Composable function or
 * lambda, and is not `remember`ed.
 *
 * This searches parent declarations until we find a lambda expression or a function, and looks
 * to see if these are Composable. If they are Composable, this function returns whether or not
 * this call expression is inside the block of a `remember` call.
 */
fun UCallExpression.invokedInComposableBodyAndNotRemembered(): Boolean {
    val visitor = ComposableBodyVisitor(this)
    if (!visitor.isComposable()) return false
    return visitor.parentUElements().none {
        (it as? UCallExpression)?.let { element ->
            element.methodName == Names.Runtime.Remember.shortName &&
                element.resolve()?.isInPackageName(Names.Runtime.PackageName) == true
        } == true
    }
}

/**
 * Returns whether this [UCallExpression] is invoked within the body of a Composable function or
 * lambda.
 *
 * This searches parent declarations until we find a lambda expression or a function, and looks
 * to see if these are Composable.
 */
fun UCallExpression.isInvokedWithinComposable(): Boolean {
    return ComposableBodyVisitor(this).isComposable()
}

// TODO: https://youtrack.jetbrains.com/issue/KT-45406
// KotlinUMethodWithFakeLightDelegate.hasAnnotation() (for reified functions for example)
// doesn't find annotations, so just look at the annotations directly.
// Note: annotations is deprecated but the replacement uAnnotations isn't available on the
// version of lint / uast we compile against, shouldn't be an issue when the above issue is fixed.
/**
 * Returns whether this method is @Composable or not
 */
@Suppress("DEPRECATION")
val UMethod.isComposable
    get() = annotations.any { it.qualifiedName == Names.Runtime.Composable.javaFqn }

/**
 * Returns whether this variable's type is @Composable or not
 */
val UVariable.isComposable: Boolean
    get() {
        // Annotation on the lambda
        val annotationOnLambda = when (val initializer = uastInitializer) {
            is ULambdaExpression -> {
                val source = initializer.sourcePsi
                if (source is KtFunction) {
                    // Anonymous function, val foo = @Composable fun() {}
                    source.hasComposableAnnotation
                } else {
                    // Lambda, val foo = @Composable {}
                    initializer.findAnnotation(Names.Runtime.Composable.javaFqn) != null
                }
            }
            else -> false
        }
        // Annotation on the type, foo: @Composable () -> Unit = { }
        val annotationOnType = typeReference?.isComposable == true
        return annotationOnLambda || annotationOnType
    }

/**
 * Returns whether this parameter's type is @Composable or not
 */
val UParameter.isComposable: Boolean
    get() = when (sourcePsi) {
        // The parameter is in a class file. Currently type annotations aren't currently added to
        // the underlying type (https://youtrack.jetbrains.com/issue/KT-45307), so instead we use
        // the metadata annotation.
        is ClsParameterImpl -> {
            // Find the containing method, so we can get metadata from the containing class
            val containingMethod = getContainingUMethod()!!.sourcePsi as ClsMethodImpl
            val kmFunction = containingMethod.toKmFunction()

            val kmValueParameter = kmFunction?.valueParameters?.find {
                it.name == name
            }

            kmValueParameter?.type?.annotations?.find {
                it.className == Names.Runtime.Composable.kmClassName
            } != null
        }
        // The parameter is in a source declaration
        else -> typeReference!!.isComposable
    }

/**
 * Returns whether this lambda expression is @Composable or not
 */
val ULambdaExpression.isComposable: Boolean
    get() = when (val lambdaParent = uastParent) {
        // Function call with a lambda parameter
        is UCallExpression -> {
            val parameter = lambdaParent.getParameterForArgument(this)
            (parameter.toUElement() as? UParameter)?.isComposable == true
        }
        // A local / non-local lambda variable
        is UVariable -> {
            lambdaParent.isComposable
        }
        // Either a new UAST type we haven't handled, or non-Kotlin declarations
        else -> false
    }

/**
 * Helper class that visits parent declarations above the provided [callExpression], until it
 * finds a lambda or method. This 'boundary' is used as the indicator for whether this
 * [callExpression] can be considered to be inside a Composable body or not.
 *
 * @see isComposable
 * @see parentUElements
 */
private class ComposableBodyVisitor(
    private val callExpression: UCallExpression
) {
    /**
     * @return whether the body can be considered Composable or not
     */
    fun isComposable(): Boolean = when (val element = parentUElements.last()) {
        is UMethod -> element.isComposable
        is ULambdaExpression -> element.isComposable
        else -> false
    }

    /**
     * Returns all parent [UElement]s until and including the boundary lambda / method.
     */
    fun parentUElements() = parentUElements

    /**
     * The outermost UElement that corresponds to the surrounding UDeclaration that contains
     * [callExpression], with the following special cases:
     *
     * - if the containing UDeclaration is a local property, we ignore it and search above as
     * it still could be created in the context of a Composable body
     * - if the containing UDeclaration is an anonymous class (object { }), we ignore it and
     * search above as it still could be created in the context of a Composable body
     */
    private val boundaryUElement by lazy {
        // The nearest property / function / etc declaration that contains this call expression
        var containingDeclaration = callExpression.getContainingDeclaration()

        fun UDeclaration.isLocalProperty() = (sourcePsi as? KtProperty)?.isLocal == true
        fun UDeclaration.isAnonymousClass() = this is UAnonymousClass
        fun UDeclaration.isPropertyInsideAnonymousClass() =
            getContainingUClass()?.isAnonymousClass() == true

        while (
            containingDeclaration != null &&
            (
                containingDeclaration.isLocalProperty() ||
                    containingDeclaration.isAnonymousClass() ||
                    containingDeclaration.isPropertyInsideAnonymousClass()
                )
        ) {
            containingDeclaration = containingDeclaration.getContainingDeclaration()
        }

        containingDeclaration
    }

    private val parentUElements by lazy {
        val elements = mutableListOf<UElement>()

        // Look through containing elements until we find a lambda or a method
        for (element in callExpression.withContainingElements) {
            elements += element
            when (element) {
                // TODO: consider handling the case of a lambda inside an inline function call,
                //  such as `apply` or `forEach`. These calls don't really change the
                //  'composability' here, but there may be other inline function calls that
                //  capture the lambda and invoke it elsewhere, so we might need to look for
                //  a callsInPlace contract in the metadata for the function, or the body of the
                //  source definition.
                is ULambdaExpression -> break
                is UMethod -> break
                // Stop when we reach the parent declaration to avoid escaping the scope.
                boundaryUElement -> break
            }
        }
        elements
    }
}

/**
 * Returns whether this type reference is @Composable or not
 */
private val UTypeReferenceExpression.isComposable: Boolean
    get() {
        if (type.hasAnnotation(Names.Runtime.Composable.javaFqn)) return true

        // Annotations should be available on the PsiType itself in 1.4.30+, but we are
        // currently on an older version of UAST / Kotlin embedded compiled
        // (https://youtrack.jetbrains.com/issue/KT-45244), so we need to manually check the
        // underlying type reference. Until then, the above check will always fail.
        return (sourcePsi as? KtTypeReference)?.hasComposableAnnotation == true
    }

/**
 * Returns whether this annotated declaration has a Composable annotation
 */
private val KtAnnotated.hasComposableAnnotation: Boolean
    get() = annotationEntries.any {
        (it.toUElement() as UAnnotation).qualifiedName == Names.Runtime.Composable.javaFqn
    }
