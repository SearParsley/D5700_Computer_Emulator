import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("MemoryDevice (RAM and ROM) Tests")
class MemoryDeviceTest {

    // Define test memory ranges
    private val ROM_TEST_START = 0x0000u.toUShort()
    private val ROM_TEST_SIZE = 4096u.toUShort()
    private val RAM_TEST_START = (ROM_TEST_START + ROM_TEST_SIZE).toUShort()
    private val RAM_TEST_SIZE = 4096u.toUShort()

    // --- Tests for MemoryDevice's isInRange method ---

    @ParameterizedTest(name = "isInRange: Address 0x{0} should be in ROM range (0x{1} to 0x{2}) -> {3}")
    @CsvSource(
        "0000, 0000, 0FFF, true",  // Start address
        "0001, 0000, 0FFF, true",  // Just after start
        "07FF, 0000, 0FFF, true",  // Middle
        "0FFF, 0000, 0FFF, true",  // End address
        "1000, 0000, 0FFF, false", // Just outside end
        "FFFF, 0000, 0FFF, false", // Far outside
        "FFFE, 0000, 0FFF, false"  // Far outside
    )
    fun testIsInRangeRom(addressHex: String, rangeStartHex: String, rangeEndHex: String, expected: Boolean) {
        val address = addressHex.toUShort(16)
        val testRom = ROM(rangeStartHex.toUShort(16), (rangeEndHex.toUShort(16) - rangeStartHex.toUShort(16) + 1u).toUShort(), listOf<UByte>())
        assertEquals(expected, testRom.isInRange(address), "Address 0x${addressHex} should be ${if (expected) "in" else "outside"} ROM range")
    }

    @ParameterizedTest(name = "isInRange: Address 0x{0} should be in RAM range (0x{1} to 0x{2}) -> {3}")
    @CsvSource(
        "1000, 1000, 1FFF, true",  // Start address
        "1001, 1000, 1FFF, true",  // Just after start
        "17FF, 1000, 1FFF, true",  // Middle
        "1FFF, 1000, 1FFF, true",  // End address
        "0FFF, 1000, 1FFF, false", // Just outside start
        "2000, 1000, 1FFF, false", // Just outside end
        "FFFF, 1000, 1FFF, false"  // Far outside
    )
    fun testIsInRangeRam(addressHex: String, rangeStartHex: String, rangeEndHex: String, expected: Boolean) {
        val address = addressHex.toUShort(16)
        val testRam = RAM(rangeStartHex.toUShort(16), (rangeEndHex.toUShort(16) - rangeStartHex.toUShort(16) + 1u).toUShort())
        assertEquals(expected, testRam.isInRange(address), "Address 0x${addressHex} should be ${if (expected) "in" else "outside"} RAM range")
    }

    // --- Tests for MemoryDevice's getLocalOffset method ---

    @ParameterizedTest(name = "getLocalOffset: Global 0x{0} with start 0x{1} should be local 0x{2}")
    @CsvSource(
        "0000, 0000, 0000", // ROM start
        "0001, 0000, 0001", // ROM + 1
        "07FF, 0000, 07FF", // ROM middle
        "0FFF, 0000, 0FFF", // ROM end
        "1000, 1000, 0000", // RAM start
        "1001, 1000, 0001", // RAM + 1
        "17FF, 1000, 07FF", // RAM middle
        "1FFF, 1000, 0FFF"  // RAM end
    )
    fun testGetLocalOffset(globalAddressHex: String, deviceStartAddressHex: String, expectedLocalOffsetHex: String) {
        val globalAddress = globalAddressHex.toUShort(16)
        val deviceStartAddress = deviceStartAddressHex.toUShort(16)
        val expectedLocalOffset = expectedLocalOffsetHex.toUShort(16)

        val dummyDevice = RAM(deviceStartAddress, 1u)
        assertEquals(expectedLocalOffset, dummyDevice.getLocalOffset(globalAddress))
    }

    // --- Tests for RAM (Concrete Implementation) ---

    @Test
    @DisplayName("RAM initializes with zeros and can write/read bytes")
    fun testRamReadWrite() {
        val ram = RAM(RAM_TEST_START, RAM_TEST_SIZE)

        // Test initial state (all zeros)
        assertEquals(0x00u.toUByte(), ram.readByte(RAM_TEST_START))
        assertEquals(0x00u.toUByte(), ram.readByte((RAM_TEST_START + 100u).toUShort()))

        // Test writing and reading a byte
        val testAddress1 = (RAM_TEST_START + 0x123u).toUShort()
        val testValue1 = 0xABu.toUByte()
        ram.writeByte(testAddress1, testValue1)
        assertEquals(testValue1, ram.readByte(testAddress1))

        // Test writing and reading another byte
        val testAddress2 = (RAM_TEST_START + RAM_TEST_SIZE - 1u).toUShort()
        val testValue2 = 0xCDu.toUByte()
        ram.writeByte(testAddress2, testValue2)
        assertEquals(testValue2, ram.readByte(testAddress2))

        // Ensure writing to one address doesn't affect another
        assertEquals(testValue1, ram.readByte(testAddress1))
    }

    // --- Tests for ROM (Concrete Implementation) ---

    @Test
    @DisplayName("ROM initializes with provided data and can read bytes")
    fun testRomRead() {
        val initialData = listOf<UByte>(0x11u, 0x22u, 0x33u, 0x44u, 0x55u)
        val rom = ROM(ROM_TEST_START, ROM_TEST_SIZE, initialData)

        // Test reading initial data
        assertEquals(0x11u.toUByte(), rom.readByte(ROM_TEST_START))
        assertEquals(0x22u.toUByte(), rom.readByte((ROM_TEST_START + 1u).toUShort()))
        assertEquals(0x55u.toUByte(), rom.readByte((ROM_TEST_START + 4u).toUShort()))

        // Test reading beyond initial data (should be 0 because MutableList is initialized with 0s)
        assertEquals(0x00u.toUByte(), rom.readByte((ROM_TEST_START + initialData.size.toUShort()).toUShort()))
    }

    @Test
    @DisplayName("ROM throws RomWriteAttemptException on writeByte attempt")
    fun testRomWriteAttemptThrowsException() {
        val initialData = listOf<UByte>(0x00u)
        val rom = ROM(ROM_TEST_START, ROM_TEST_SIZE, initialData)
        val addressToAttemptWrite = ROM_TEST_START

        val exception = assertThrows(RomWriteAttemptException::class.java) {
            rom.writeByte(addressToAttemptWrite, 0xFFu)
        }

        assertTrue(exception.message!!.contains("Attempted to write to ROM at address"))
        assertTrue(exception.message!!.contains(addressToAttemptWrite.toString(16).padStart(4, '0')))

        // Verify that the data in ROM did NOT change
        assertEquals(initialData[0], rom.readByte(addressToAttemptWrite))
    }
}