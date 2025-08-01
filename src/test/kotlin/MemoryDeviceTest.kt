import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@DisplayName("MemoryDevice (RAM, ROM, and I/O) Tests")
class MemoryDeviceTest {

    private val ROM_TEST_START = 0x0000u.toUShort()
    private val ROM_TEST_SIZE = 4096u.toUShort()
    private val RAM_TEST_START = (ROM_TEST_START + ROM_TEST_SIZE).toUShort()
    private val RAM_TEST_SIZE = 4096u.toUShort()

    private val DISPLAY_TEST_START = 0xF000u.toUShort()
    private val DISPLAY_TEST_SIZE = 64u.toUShort()

    private val outputStreamCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setUpOutputCapture() {
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @ParameterizedTest(name = "isInRange: Address 0x{0} should be in ROM range (0x{1} to 0x{2}) -> {3}")
    @CsvSource(
        "0000, 0000, 0FFF, true",
        "0FFF, 0000, 0FFF, true",
        "1000, 0000, 0FFF, false"
    )
    fun testIsInRangeRom(addressHex: String, rangeStartHex: String, rangeEndHex: String, expected: Boolean) {
        val address = addressHex.toUShort(16)
        val testRom = ROM(rangeStartHex.toUShort(16), (rangeEndHex.toUShort(16) - rangeStartHex.toUShort(16) + 1u).toUShort(), listOf<UByte>())
        assertEquals(expected, testRom.isInRange(address), "Address 0x${addressHex} should be ${if (expected) "in" else "outside"} ROM range")
    }

    @ParameterizedTest(name = "isInRange: Address 0x{0} should be in RAM range (0x{1} to 0x{2}) -> {3}")
    @CsvSource(
        "1000, 1000, 1FFF, true",
        "1FFF, 1000, 1FFF, true",
        "0FFF, 1000, 1FFF, false"
    )
    fun testIsInRangeRam(addressHex: String, rangeStartHex: String, rangeEndHex: String, expected: Boolean) {
        val address = addressHex.toUShort(16)
        val testRam = RAM(rangeStartHex.toUShort(16), (rangeEndHex.toUShort(16) - rangeStartHex.toUShort(16) + 1u).toUShort())
        assertEquals(expected, testRam.isInRange(address), "Address 0x${addressHex} should be ${if (expected) "in" else "outside"} RAM range")
    }

    @ParameterizedTest(name = "isInRange: Address 0x{0} should be in Display range (0x{1} to 0x{2}) -> {3}")
    @CsvSource(
        "F000, F000, F03F, true", // Start address
        "F001, F000, F03F, true", // Just after start
        "F01F, F000, F03F, true", // Middle
        "F03F, F000, F03F, true", // End address (64 bytes from F000 is F03F)
        "EFFF, F000, F03F, false", // Just outside start
        "F040, F000, F03F, false", // Just outside end
        "FFFF, F000, F03F, false" // Far outside
    )
    fun testIsInRangeDisplay(addressHex: String, rangeStartHex: String, rangeEndHex: String, expected: Boolean) {
        val address = addressHex.toUShort(16)
        val testDisplay = AsciiDisplayDevice(rangeStartHex.toUShort(16), (rangeEndHex.toUShort(16) - rangeStartHex.toUShort(16) + 1u).toUShort())
        assertEquals(expected, testDisplay.isInRange(address), "Address 0x${addressHex} should be ${if (expected) "in" else "outside"} Display range")
    }

    @ParameterizedTest(name = "getLocalOffset: Global 0x{0} with start 0x{1} should be local 0x{2}")
    @CsvSource(
        "0000, 0000, 0000",
        "0001, 0000, 0001",
        "07FF, 0000, 07FF",
        "0FFF, 0000, 0FFF",
        "1000, 1000, 0000",
        "1001, 1000, 0001",
        "17FF, 1000, 07FF",
        "1FFF, 1000, 0FFF",
        "F000, F000, 0000", // Display start
        "F001, F000, 0001", // Display + 1
        "F03F, F000, 003F"  // Display end
    )
    fun testGetLocalOffset(globalAddressHex: String, deviceStartAddressHex: String, expectedLocalOffsetHex: String) {
        val globalAddress = globalAddressHex.toUShort(16)
        val deviceStartAddress = deviceStartAddressHex.toUShort(16)
        val expectedLocalOffset = expectedLocalOffsetHex.toUShort(16)
        val dummyDevice = RAM(deviceStartAddress, 1u)
        assertEquals(expectedLocalOffset, dummyDevice.getLocalOffset(globalAddress))
    }

    @Test
    @DisplayName("RAM initializes with zeros and can write/read bytes")
    fun testRamReadWrite() {
        val ram = RAM(RAM_TEST_START, RAM_TEST_SIZE)
        assertEquals(0x00u.toUByte(), ram.readByte(RAM_TEST_START))
        val testAddress1 = (RAM_TEST_START + 0x123u).toUShort()
        val testValue1 = 0xABu.toUByte()
        ram.writeByte(testAddress1, testValue1)
        assertEquals(testValue1, ram.readByte(testAddress1))
    }

    @Test
    @DisplayName("ROM initializes with provided data and can read bytes")
    fun testRomRead() {
        val initialData = listOf<UByte>(0x11u, 0x22u, 0x33u, 0x44u, 0x55u)
        val rom = ROM(ROM_TEST_START, ROM_TEST_SIZE, initialData)
        assertEquals(0x11u.toUByte(), rom.readByte(ROM_TEST_START))
        assertEquals(0x55u.toUByte(), rom.readByte((ROM_TEST_START + 4u).toUShort()))
    }

    @Test
    @DisplayName("ROM throws RomWriteAttemptException on writeByte attempt")
    fun testRomWriteAttemptThrowsException() {
        val initialData = listOf<UByte>(0x00u)
        val rom = ROM(ROM_TEST_START, ROM_TEST_SIZE, initialData)
        val addressToAttemptWrite = ROM_TEST_START
        assertThrows(RomWriteAttemptException::class.java) {
            rom.writeByte(addressToAttemptWrite, 0xFFu)
        }
    }

    @Test
    @DisplayName("ROM truncates initial data if it exceeds the defined size")
    fun testRomTruncatesInitialDataWhenTooLarge() {
        val largeInitialData = listOf<UByte>(0x11u, 0x22u, 0x33u, 0x44u, 0x55u)
        val smallRomSize = 2u.toUShort()
        val romTestStart = 0x0000u.toUShort()
        val rom = ROM(romTestStart, smallRomSize, largeInitialData)
        assertEquals(0x11u.toUByte(), rom.readByte(romTestStart))
        assertEquals(0x22u.toUByte(), rom.readByte((romTestStart + 1u).toUShort()))
        assertThrows(IndexOutOfBoundsException::class.java) {
            rom.readByte((romTestStart + 2u).toUShort())
        }
        val expectedWarning = "ROM Initialization Warning:"
        val capturedOutput = outputStreamCaptor.toString().trim()
        assertTrue(capturedOutput.contains(expectedWarning))
    }

    @Test
    @DisplayName("AsciiDisplayDevice initializes with spaces (0x20u)")
    fun testDisplayInitialState() {
        val display = AsciiDisplayDevice(DISPLAY_TEST_START, DISPLAY_TEST_SIZE)
        for (i in 0 until Constants.ASCII_DISPLAY_BUFFER_SIZE_BYTES) {
            assertEquals(0x20u.toUByte(), display.readByte((DISPLAY_TEST_START + i.toUShort()).toUShort()), "Display buffer at offset $i should be space (0x20)")
        }
    }

    @Test
    @DisplayName("AsciiDisplayDevice can write and read bytes")
    fun testDisplayReadWrite() {
        val display = AsciiDisplayDevice(DISPLAY_TEST_START, DISPLAY_TEST_SIZE)
        val testAddress = (DISPLAY_TEST_START + 0x05u).toUShort() // Address for row 0, col 5
        val testValue = 'A'.code.toUByte()
        display.writeByte(testAddress, testValue)
        assertEquals(testValue, display.readByte(testAddress))
        val testAddress2 = (DISPLAY_TEST_START + (Constants.ASCII_DISPLAY_WIDTH_CHARS * 4).toUShort() + 3u).toUShort() // Row 4, Col 3
        val testValue2 = 'Z'.code.toUByte()
        display.writeByte(testAddress2, testValue2)
        assertEquals(testValue2, display.readByte(testAddress2))
        assertEquals(testValue, display.readByte(testAddress))
    }

    @Test
    @DisplayName("AsciiDisplayDevice getScreenContent returns a copy of the buffer")
    fun testGetScreenContent() {
        val display = AsciiDisplayDevice(DISPLAY_TEST_START, DISPLAY_TEST_SIZE)
        val testAddress = (DISPLAY_TEST_START + 0x01u).toUShort()
        val testValue = 'X'.code.toUByte()
        display.writeByte(testAddress, testValue)
        val content = display.getScreenContent()
        assertEquals(Constants.ASCII_DISPLAY_BUFFER_SIZE_BYTES, content.size)
        assertEquals(testValue, content[1]) // Check the modified byte
        assertEquals(testValue, display.readByte(testAddress))
    }

    @Test
    @DisplayName("AsciiDisplayDevice getRenderedScreen formats output correctly with printable and non-printable chars")
    fun testGetRenderedScreenFormatsOutput() {
        val display = AsciiDisplayDevice(0xF000u, Constants.ASCII_DISPLAY_BUFFER_SIZE_BYTES.toUShort())
        display.writeByte((0xF000u + 0u).toUShort(), 'H'.code.toUByte())
        display.writeByte((0xF000u + 1u).toUShort(), 'E'.code.toUByte())
        display.writeByte((0xF000u + 2u).toUShort(), 0x07u) // Non-printable
        display.writeByte((0xF000u + 8u).toUShort(), 'L'.code.toUByte())
        display.writeByte((0xF000u + 9u).toUShort(), 'L'.code.toUByte())
        display.writeByte((0xF000u + 10u).toUShort(), 0x80u) // Non-printable
        display.writeByte((0xF000u + 16u).toUShort(), 'O'.code.toUByte())
        display.writeByte((0xF000u + 17u).toUShort(), '!'.code.toUByte())
        display.writeByte((0xF000u + 18u).toUShort(), 0x07u) // Non-printable
        val expectedOutput = """
            --- D5700 ASCII Display (0xF000) ---
            HE.     
            LL.     
            O!.     
                    
                    
                    
                    
                    
            ------------------------------------
            """.trimIndent()
        val actualOutput = display.getRenderedScreen().trimIndent()
        assertEquals(expectedOutput, actualOutput)
    }
}