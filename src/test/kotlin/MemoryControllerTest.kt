import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@DisplayName("MemoryController Object Tests")
class MemoryControllerTest {

    // Define test memory ranges
    private val ROM_TEST_START = 0x0000u.toUShort()
    private val ROM_TEST_SIZE = 4096u.toUShort()
    private val RAM_TEST_START = (ROM_TEST_START + ROM_TEST_SIZE).toUShort()
    private val RAM_TEST_SIZE = 4096u.toUShort()

    // Define test I/O addresses
    private val KEYBOARD_TEST_START = 0xFE00u.toUShort()
    private val DISPLAY_TEST_START = 0xF000u.toUShort()

    // --- System I/O Redirection ---
    private val standardOut = System.out
    private val standardIn = System.`in`
    private val outputStreamCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setup() {
        MemoryController.resetForTesting()
        System.setOut(PrintStream(outputStreamCaptor)) // Redirect System.out
    }

    @AfterEach
    fun restoreSystemIo() {
        System.setOut(standardOut) // Restore original System.out
        System.setIn(standardIn)   // Restore original System.in
    }

    @Test
    @DisplayName("initializeMemoryMap correctly sets up ROM and RAM ranges")
    fun testInitializeMemoryMap() {
        val testRomData = listOf<UByte>(0xAAu, 0xBBu, 0xCCu)
        val testRomSize = testRomData.size.toUShort()
        val testRamSize = 256u.toUShort()
        MemoryController.initializeMemoryMap(ROM_TEST_START, testRomData, RAM_TEST_START, testRamSize)
        assertEquals(0xAAu.toUByte(), MemoryController.readByte(ROM_TEST_START))
        assertEquals(0xCCu.toUByte(), MemoryController.readByte((ROM_TEST_START + 2u).toUShort()))
        val ramTestAddress = RAM_TEST_START
        MemoryController.writeByte(ramTestAddress, 0x12u)
        assertEquals(0x12u.toUByte(), MemoryController.readByte(ramTestAddress))
        val unmappedAddress = (ROM_TEST_START + testRomSize).toUShort()
        outputStreamCaptor.reset()
        assertEquals(0xFFu.toUByte(), MemoryController.readByte(unmappedAddress))
        assertTrue(outputStreamCaptor.toString().contains("Memory Access Error: No device mapped to address"))
    }

    @Test
    @DisplayName("readByte returns 0xFF and logs error for unmapped addresses")
    fun testReadByteUnmappedAddress() {
        MemoryController.initializeMemoryMap(ROM_TEST_START, listOf<UByte>(0x01u), RAM_TEST_START, 10u)
        val unmappedAddress = 0x2000u.toUShort()
        outputStreamCaptor.reset()
        val result = MemoryController.readByte(unmappedAddress)
        assertEquals(0xFFu.toUByte(), result)
        val capturedOutput = outputStreamCaptor.toString().trim()
        assertTrue(capturedOutput.contains("Memory Access Error: No device mapped to address 0x${unmappedAddress.toString(16).uppercase().padStart(4, '0')}"))
    }

    @Test
    @DisplayName("writeByte logs error for unmapped addresses")
    fun testWriteByteUnmappedAddress() {
        MemoryController.initializeMemoryMap(ROM_TEST_START, listOf<UByte>(0x01u), RAM_TEST_START, 10u)
        val unmappedAddress = 0x2000u.toUShort()
        outputStreamCaptor.reset()
        MemoryController.writeByte(unmappedAddress, 0xAAu)
        val capturedOutput = outputStreamCaptor.toString().trim()
        assertTrue(capturedOutput.contains("Memory Access Error: No device mapped to address 0x${unmappedAddress.toString(16).uppercase().padStart(4, '0')}"))
    }

    @Test
    @DisplayName("readByte correctly routes to ROM")
    fun testReadByteRoutesToRom() {
        val romData = listOf<UByte>(0x12u, 0x34u)
        MemoryController.initializeMemoryMap(ROM_TEST_START, romData, RAM_TEST_START, 10u)
        val value = MemoryController.readByte(ROM_TEST_START)
        assertEquals(0x12u.toUByte(), value)
        val value2 = MemoryController.readByte((ROM_TEST_START + 1u).toUShort())
        assertEquals(0x34u.toUByte(), value2)
    }

    @Test
    @DisplayName("readByte correctly routes to RAM")
    fun testReadByteRoutesToRam() {
        MemoryController.initializeMemoryMap(ROM_TEST_START, listOf<UByte>(0x01u), RAM_TEST_START, 10u)
        MemoryController.writeByte(RAM_TEST_START, 0x55u)
        val value = MemoryController.readByte(RAM_TEST_START)
        assertEquals(0x55u.toUByte(), value)
    }

    @Test
    @DisplayName("writeByte correctly routes to RAM")
    fun testWriteByteRoutesToRam() {
        MemoryController.initializeMemoryMap(ROM_TEST_START, listOf<UByte>(0x01u), RAM_TEST_START, 10u)
        MemoryController.writeByte(RAM_TEST_START, 0xAAu)
        assertEquals(0xAAu.toUByte(), MemoryController.readByte(RAM_TEST_START))
    }

    @Test
    @DisplayName("writeByte to ROM throws RomWriteAttemptException")
    fun testWriteByteToRomThrowsException() {
        val romData = listOf<UByte>(0x12u)
        MemoryController.initializeMemoryMap(ROM_TEST_START, romData, RAM_TEST_START, 10u)
        val romAddress = ROM_TEST_START
        assertThrows(RomWriteAttemptException::class.java) {
            MemoryController.writeByte(romAddress, 0xFFu)
        }
    }

    @Test
    @DisplayName("addDevice correctly adds and routes to concrete I/O devices")
    fun testAddDeviceAndRouting() {
        val romData = listOf<UByte>(0x01u)
        val ramSize = 10u.toUShort()
        MemoryController.initializeMemoryMap(ROM_TEST_START, romData, RAM_TEST_START, ramSize)
        val keyboardDevice = MemoryDeviceFactory.createKeyboardInputDevice(KEYBOARD_TEST_START, 1u)
        val displayDevice = MemoryDeviceFactory.createAsciiDisplayDevice(DISPLAY_TEST_START, 64u)
        MemoryController.addDevice(keyboardDevice)
        MemoryController.addDevice(displayDevice)
        val keyboardInput = "AB"
        val expectedValue = 0xABu.toUByte()
        val inputStream = ByteArrayInputStream(keyboardInput.toByteArray())
        System.setIn(inputStream)
        val readValueFromKeyboard = MemoryController.readByte(KEYBOARD_TEST_START)
        assertEquals(expectedValue, readValueFromKeyboard)
        val displayTestAddress = (DISPLAY_TEST_START + 5u).toUShort()
        val writeValue = 'Z'.code.toUByte()
        MemoryController.writeByte(displayTestAddress, writeValue)
        val readValueFromDisplay = MemoryController.readByte(displayTestAddress)
        assertEquals(writeValue, readValueFromDisplay)
        assertEquals(0x01u.toUByte(), MemoryController.readByte(ROM_TEST_START))
    }
}