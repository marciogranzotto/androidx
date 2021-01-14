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

package androidx.wear.watchface.editor.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.wear.complications.data.ComplicationData
import androidx.wear.watchface.editor.EditorSession
import androidx.wear.watchface.editor.setWatchRequestResult
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSchema
import com.google.common.util.concurrent.ListenableFuture

/** @hide */
@RestrictTo(LIBRARY)
internal interface FragmentController {
    /** Show the [ConfigFragment] which lets the user select what they want to configure. */
    fun showConfigFragment()

    /**
     * Show the [ComplicationConfigFragment] which lets the user select the complication
     * they want to configure.
     */
    fun showComplicationConfigSelectionFragment()

    /** Show the [StyleConfigFragment] which lets the user configure the watch face style. */
    fun showStyleConfigFragment(
        settingId: String,
        styleSchema: UserStyleSchema,
        userStyle: UserStyle
    )

    /** Lets the user configure the complication provider for a single complication slot. */
    fun showComplicationConfig(complicationId: Int): ListenableFuture<Boolean>
}

// Reference time for editor screenshots for analog watch faces.
// 2020/10/10 at 09:30 Note the date doesn't matter, only the hour.
private const val ANALOG_WATCHFACE_REFERENCE_TIME_MS = 1602318600000L

// Reference time for editor screenshots for digital watch faces.
// 2020/10/10 at 10:10 Note the date doesn't matter, only the hour.
private const val DIGITAL_WATCHFACE_REFERENCE_TIME_MS = 1602321000000L

/**
 * Config activity for the watch face, which supports complication and provider selection, as well
 * as userStyle configuration.
 */
class WatchFaceConfigActivity : FragmentActivity() {
    internal val complicationData = HashMap<Int, ComplicationData>()

    internal lateinit var editorSession: EditorSession
    internal lateinit var fragmentController: FragmentController
    internal lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val editorSession = EditorSession.createOnWatchEditingSession(
            this,
            intent!!.apply {
                // TODO(alexclarke): Remove this when SysUI creates compatible intents.
                putExtra("INSTANCE_ID", "FakeID")
            }
        )!!
        init(
            editorSession,
            object : FragmentController {
                @SuppressLint("SyntheticAccessor")
                override fun showConfigFragment() {
                    showFragment(ConfigFragment())
                }

                @SuppressLint("SyntheticAccessor")
                override fun showComplicationConfigSelectionFragment() {
                    showFragment(ComplicationConfigFragment())
                }

                @SuppressLint("SyntheticAccessor")
                override fun showStyleConfigFragment(
                    settingId: String,
                    styleSchema: UserStyleSchema,
                    userStyle: UserStyle
                ) {
                    showFragment(
                        StyleConfigFragment.newInstance(settingId, styleSchema, userStyle)
                    )
                }

                /**
                 * Displays a config screen which allows the user to select the data source for the
                 * complication.
                 */
                @SuppressWarnings("deprecation")
                override fun showComplicationConfig(
                    complicationId: Int
                ): ListenableFuture<Boolean> =
                    editorSession.launchComplicationProviderChooser(complicationId)
            },
            Handler(Looper.getMainLooper())
        )
    }

    private fun focusCurrentFragment() {
        val curFragment = supportFragmentManager.findFragmentById(android.R.id.content)
        if (curFragment != null) {
            curFragment.view?.importantForAccessibility =
                View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }

        // Clear focus on the old fragment that is behind the new one, and announce the new title.
        // This prevents the Talkback linear navigation from selecting elements that are behind the
        // new fragment.
        window.decorView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    private fun showFragment(fragment: Fragment) {
        val curFragment = supportFragmentManager.findFragmentById(android.R.id.content)
        curFragment?.view?.importantForAccessibility =
            View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        // The new fragment will have its importance set by OnBackStackChangedListener.
        supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    @VisibleForTesting
    internal fun init(
        editorSession: EditorSession,
        fragmentController: FragmentController,
        handler: Handler
    ) {
        this.editorSession = editorSession
        this.fragmentController = fragmentController
        this.handler = handler

        supportFragmentManager
            .addOnBackStackChangedListener {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    finish()
                } else {
                    focusCurrentFragment()
                }
            }

        var topLevelOptionCount = editorSession.userStyleSchema.userStyleSettings.size
        val hasBackgroundComplication = editorSession.backgroundComplicationId != null
        if (hasBackgroundComplication) {
            topLevelOptionCount++
        }
        val numComplications = editorSession.complicationState.size
        val hasNonBackgroundComplication =
            numComplications > (if (hasBackgroundComplication) 1 else 0)
        if (hasNonBackgroundComplication) {
            topLevelOptionCount++
        }

        when {
            // More than one top level option, so show a fragment which lets the user choose what
            // they want to configure.
            topLevelOptionCount > 1 -> fragmentController.showConfigFragment()

            // For a single complication go directly to the provider selector.
            numComplications == 1 -> {
                val onlyComplication = editorSession.complicationState.entries.first()
                fragmentController.showComplicationConfig(onlyComplication.key)
            }

            // For multiple complications select the complication to configure first.
            numComplications > 1 -> fragmentController.showComplicationConfigSelectionFragment()

            // For a single style, go select the option.
            editorSession.userStyleSchema.userStyleSettings.size == 1 -> {
                // There should only be a single userStyle setting if we get here.
                val onlyStyleSetting = editorSession.userStyleSchema.userStyleSettings.first()
                fragmentController.showStyleConfigFragment(
                    onlyStyleSetting.id,
                    editorSession.userStyleSchema,
                    editorSession.userStyle
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        setWatchRequestResult(editorSession)
    }
}