import java.util.Timer
import java.util.TimerTask // Import TimerTask

class FakeTimer(name: String, isDaemon: Boolean): Timer(name, isDaemon) {
    var scheduledTask: TimerTask? = null
    var scheduledDelay: Long = -1
    var scheduledPeriod: Long = -1

    // Simulate Timer.scheduleAtFixedRate
    override fun scheduleAtFixedRate(task: TimerTask, delay: Long, period: Long) {
        this.scheduledTask = task
        this.scheduledDelay = delay
        this.scheduledPeriod = period
    }

    // Simulate Timer.cancel()
    override fun cancel() {
        scheduledTask = null
        scheduledDelay = -1
        scheduledPeriod = -1
    }

    // --- Helper methods for testing ---

    // Manually run the scheduled task's action (simulating a tick)
    fun tick() {
        // Execute the scheduled task's run() method directly.
        scheduledTask?.run()
    }

    // Check if a task is currently scheduled
    fun isTaskScheduled(): Boolean = scheduledTask != null
}