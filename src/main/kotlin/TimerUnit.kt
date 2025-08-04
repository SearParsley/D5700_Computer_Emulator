import java.util.Timer
import java.util.TimerTask

class TimerUnit(
    private val registers: IRegisters,
    private val timer: Timer = Timer("D5700_Timer", true)
) : ITimerUnit {
    internal var timerTask: TimerTask? = null

    init {
        println("TimerUnit initialized.")
    }

    override fun setTimerValue(value: UByte) {
        registers.T = value
        if (value > 0u.toUByte()) startDecrementing() else stopDecrementing()
    }

    override fun startDecrementing() {
        stopDecrementing()
        timerTask = object : TimerTask() {
            override fun run() {
                if (registers.T > 0u.toUByte()) registers.T-- else stopDecrementing()
            }
        }
        timer.scheduleAtFixedRate(timerTask, 0, Constants.TIMER_DECREMENT_MS)
    }

    override fun stopDecrementing() {
        timerTask?.cancel()
        timerTask = null
    }

    override fun shutdown() {
        stopDecrementing()
        timer.cancel()
        println("TimerUnit: Shutting down.")
    }
}