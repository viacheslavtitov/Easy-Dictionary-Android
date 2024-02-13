package my.dictionary.free.domain.utils

import android.os.CountDownTimer

open class QuizTimer(millisInFuture: Long, countDownInterval: Long = INTERVAL_MILLISECONDS): CountDownTimer(millisInFuture, countDownInterval) {

    private var isRunning = false
    private var paused = false

    companion object {
        private const val INTERVAL_MILLISECONDS = 1000L
    }

    override fun onTick(time: Long) {
        isRunning = true
    }

    override fun onFinish() {
        isRunning = false
    }

    fun pause() {
        if(isRunning()) {
            this.cancel()
            isRunning = false
            paused = true
        }
    }

    fun resume() {
        if(!isRunning() && paused) {
            start()
            paused = false
        }
    }

    fun isRunning() = isRunning
}