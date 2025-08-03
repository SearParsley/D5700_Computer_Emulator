import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@DisplayName("TimerUnit Tests (Deterministic)")
class TimerUnitTest {

    private lateinit var timerUnit: TimerUnit
    private lateinit var registers: Registers
    private lateinit var fakeTimer: FakeTimer

    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setup() {
        Registers.resetForTesting()
        registers = Registers
        fakeTimer = FakeTimer("TestTimer", true)
        timerUnit = TimerUnit(fakeTimer)
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @AfterEach
    fun teardown() {
        fakeTimer.cancel()
        System.setOut(standardOut)
    }

    @Test
    @DisplayName("setTimerValue schedules a task with correct parameters")
    fun testSetTimerValueStartsDecrementing() {
        timerUnit.setTimerValue(10u)
        assertTrue(fakeTimer.isTaskScheduled(), "Timer task should be scheduled")
        assertEquals(0L, fakeTimer.scheduledDelay, "Timer delay should be 0")
        assertEquals(Constants.TIMER_DECREMENT_MS, fakeTimer.scheduledPeriod, "Timer period should be 16ms")
    }

    @Test
    @DisplayName("setTimerValue to 0 cancels the task")
    fun testSetTimerValueToZeroStopsDecrementing() {
        timerUnit.setTimerValue(5u)
        assertTrue(fakeTimer.isTaskScheduled())
        timerUnit.setTimerValue(0u)
        assertNull(timerUnit.timerTask, "Task should be cancelled when T is 0")
        assertEquals(0u.toUByte(), registers.T, "T should be set to 0")
    }

    @Test
    @DisplayName("stopDecrementing cancels the task")
    fun testStopDecrementing() {
        timerUnit.setTimerValue(3u)
        assertTrue(fakeTimer.isTaskScheduled())
        timerUnit.stopDecrementing()
        assertNull(timerUnit.timerTask, "Task should be cancelled")
    }

    @Test
    @DisplayName("Timer decrements T correctly on manual tick")
    fun testTimerDecrementsT() {
        registers.T = 3u
        timerUnit.setTimerValue(3u)
        assertEquals(3u.toUByte(), registers.T)
        fakeTimer.tick()
        assertEquals(2u.toUByte(), registers.T)
        fakeTimer.tick()
        assertEquals(1u.toUByte(), registers.T)
    }

    @Test
    @DisplayName("Timer stops when T reaches 0")
    fun testTimerStopsAtZero() {
        registers.T = 1u
        timerUnit.setTimerValue(1u)
        fakeTimer.tick()
        assertEquals(0u.toUByte(), registers.T, "T should be 0")
        fakeTimer.tick()
        assertNull(timerUnit.timerTask, "Task should be cancelled when T reaches 0")
    }

    @Test
    @DisplayName("shutdown cancels the task and the timer")
    fun testShutdown() {
        timerUnit.setTimerValue(0x05u.toUByte())
        assertNotNull(timerUnit.timerTask, "A timer task should be running initially")
        assertTrue(fakeTimer.isTaskScheduled(), "The fake timer should have a scheduled task")
        outputStreamCaptor.reset()
        timerUnit.shutdown()
        assertNull(timerUnit.timerTask, "The TimerUnit's task reference should be null after shutdown")
        assertFalse(fakeTimer.isTaskScheduled(), "The fake timer's task should be cancelled")
        val capturedOutput = outputStreamCaptor.toString().trim()
        assertTrue(capturedOutput.contains("TimerUnit: Shutting down."))
    }
}