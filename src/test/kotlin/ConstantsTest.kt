import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Constants Tests")
class ConstantsTest {

    @Test
    @DisplayName("CPU Frequency and Period are correct")
    fun testCpuFrequencyAndPeriod() {
        assertEquals(500, Constants.CPU_FREQUENCY_HZ)
        assertEquals(2L, Constants.CPU_PERIOD_MS) // 1000ms / 500Hz = 2ms
    }

    @Test
    @DisplayName("Instruction Bytes and PC Increment are correct")
    fun testInstructionBytesAndPcIncrement() {
        assertEquals(2, Constants.CPU_INSTRUCTION_BYTES)
        assertEquals(2, Constants.CPU_PC_INCREMENT_DEFAULT)
        assertEquals(16, Constants.CPU_INSTRUCTIONS_TOTAL)
    }

    @Test
    @DisplayName("RAM and ROM Sizes are correct")
    fun testMemorySizes() {
        assertEquals(4, Constants.RAM_SIZE_KB)
        assertEquals(4096, Constants.RAM_SIZE_BYTES)
        assertEquals(4, Constants.ROM_SIZE_KB)
        assertEquals(4096, Constants.ROM_SIZE_BYTES)
    }

    @Test
    @DisplayName("Program Counter Start Address is correct")
    fun testPcStartAddress() {
        assertEquals(0x0000u.toUShort(), Constants.PROGRAM_COUNTER_START_ADDRESS)
    }

    @Test
    @DisplayName("Timer Decrement Rate is correct")
    fun testTimerDecrementRate() {
        assertEquals(60, Constants.TIMER_DECREMENT_HZ)
        assertEquals(16L, Constants.TIMER_DECREMENT_MS)
    }

    @Test
    @DisplayName("Screen Dimensions and Buffer Size are correct")
    fun testScreenDimensions() {
        assertEquals(8, Constants.ASCII_DISPLAY_WIDTH_CHARS)
        assertEquals(8, Constants.ASCII_DISPLAY_HEIGHT_CHARS)
        assertEquals(64, Constants.ASCII_DISPLAY_BUFFER_SIZE_BYTES)
    }

    @Test
    @DisplayName("Keyboard Buffer Size is correct")
    fun testKeyboardDevice() {
        assertEquals(1, Constants.KEYBOARD_BUFFER_SIZE_BYTES)
    }

}