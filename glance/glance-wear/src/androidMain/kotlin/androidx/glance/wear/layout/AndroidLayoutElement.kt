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

package androidx.glance.wear.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.glance.Applier
import androidx.glance.Emittable
import androidx.glance.Modifier
import androidx.wear.tiles.LayoutElementBuilders

/**
 * Add [LayoutElementBuilders.LayoutElement] into the glance composition.
 *
 * @param layoutElement the layout element to add to the composition
 */
@Composable
fun AndroidLayoutElement(layoutElement: LayoutElementBuilders.LayoutElement) {
    ComposeNode<EmittableAndroidLayoutElement, Applier>(
        factory = ::EmittableAndroidLayoutElement,
        update = {
            this.set(layoutElement) { this.layoutElement = it }
        },
    )
}

internal class EmittableAndroidLayoutElement : Emittable {
    override var modifier: Modifier = Modifier
    lateinit var layoutElement: LayoutElementBuilders.LayoutElement

    override fun toString() = "EmittableAndroidLayoutElement()"
}
