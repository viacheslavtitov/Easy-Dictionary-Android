package my.dictionary.free.domain.utils

import android.os.CountDownTimer

open class QuizTimer(millisInFuture: Long, countDownInterval: Long = INTERVAL_MILLISECONDS): CountDownTimer(millisInFuture, countDownInterval) {

    private var isRunning = false

    companion object {
        private const val INTERVAL_MILLISECONDS = 1000L
    }

    override fun onTick(time: Long) {
        isRunning = true
    }

    override fun onFinish() {
        isRunning = false
    }

    fun isRunning() = isRunning
}