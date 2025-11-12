package org.easydictionary.app.view.anim

import androidx.compose.animation.core.keyframes

val fieldsKeyFramesForShakeAnim = keyframes {
    durationMillis = 1500
    -16f at 50
    16f at 100
    -12f at 150
    12f at 200
    -8f at 250
    8f at 300
    -4f at 350
    4f at 400
    0f at 450
}