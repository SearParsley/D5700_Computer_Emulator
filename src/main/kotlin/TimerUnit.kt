import java.util.Timer
import java.util.TimerTask

class TimerUnit(
    private val timer: Timer = Timer("D5700_Timer", true)
) {
    internal var timerTask: TimerTask? = null

    init {
        println("TimerUnit initialized.")
    }

    fun setTimerValue(value: UByte) {
        Registers.T = value
        if (value > 0u.toUByte()) startDecrementing() else stopDecrementing()
    }

    internal fun startDecrementing() {
        stopDecrementing()
        timerTask = object : TimerTask() {
            override fun run() {
                if (Registers.T > 0u.toUByte()) Registers.T-- else stopDecrementing()
            }
        }
        timer.scheduleAtFixedRate(timerTask, 0, Constants.TIMER_DECREMENT_MS)
    }

    internal fun stopDecrementing() {
        timerTask?.cancel()
        timerTask = null
    }

    fun shutdown() {
        stopDecrementing()
        timer.cancel()
        println("TimerUnit: Shutting down.")
    }
}