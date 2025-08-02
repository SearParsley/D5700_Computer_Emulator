import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach // Ensure this is imported
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger // For counting calls in mock

@DisplayName("MemoryController Object Tests")
class MemoryControllerTest {

    // --- System I/O Redirection ---
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setup() {
        // Reset the singleton before each test to ensure isolation
        MemoryController.resetForTesting()
        System.setOut(PrintStream(outputStreamCaptor)) // Redirect System.out
    }

    @AfterEach
    fun restoreSystemIo() {
        System.setOut(standardOut) // Restore original System.out
    }

    @Test
    @DisplayName("MemoryController is a singleton (object)")
    fun testSingletonBehavior() {
        // Kotlin objects are singletons by default, so getInstance() is not needed.
        // We just ensure we're testing the same global object.
        val controller1 = MemoryController
        val controller2 = MemoryController
        assertSame(controller1, controller2)
    }

    @Test
    @DisplayName("initializeMemoryMap correctly sets up ROM and RAM ranges")
    fun testInitializeMemoryMap() {
        val testRomData = listOf<UByte>(0xAAu, 0xBBu, 0xCCu) // Small ROM data
        val testRamSize = 256u.toUShort() // Small RAM size

        MemoryController.initializeMemoryMap(testRomData, testRamSize)

        // Verify ROM range
        assertTrue(MemoryController.readByte(Constants.ROM_START_ADDRESS) == 0xAAu.toUByte())
        assertTrue(MemoryController.readByte((Constants.ROM_START_ADDRESS + 1u).toUShort()) == 0xBBu.toUByte())
        assertTrue(MemoryController.readByte((Constants.ROM_START_ADDRESS + 2u).toUShort()) == 0xCCu.toUByte())
        // Verify a byte just outside ROM, which should be RAM
        assertTrue(MemoryController.readByte((Constants.ROM_START_ADDRESS + testRomData.size.toUShort()).toUShort()) == 0x00u.toUByte()) // Should be 0 in RAM

        // Verify RAM range
        val ramTestAddress = Constants.RAM_START_ADDRESS
        MemoryController.writeByte(ramTestAddress, 0x12u)
        assertEquals(0x12u.toUByte(), MemoryController.readByte(ramTestAddress))

        // Verify console output from initialization
        val capturedOutput = outputStreamCaptor.toString()
        assertTrue(capturedOutput.contains("MemoryController: Memory map initialized."))
        assertTrue(capturedOutput.contains("ROM: 0x0000"))
        assertTrue(capturedOutput.contains("RAM: 0x1000")) // RAM starts at 0x1000 based on 4KB ROM
    }

    @Test
    @DisplayName("readByte returns 0xFF and logs error for unmapped addresses")
    fun testReadByteUnmappedAddress() {
        // Initialize with minimal ROM/RAM to create unmapped space
        MemoryController.initializeMemoryMap(listOf<UByte>(0x01u), 10u) // ROM 1 byte, RAM 10 bytes

        val unmappedAddress = 0x2000u.toUShort() // Address far outside the mapped ROM/RAM
        outputStreamCaptor.reset() // Clear previous init output

        val result = MemoryController.readByte(unmappedAddress)

        assertEquals(0xFFu.toUByte(), result) // Default return for unmapped
        val capturedOutput = outputStreamCaptor.toString().trim()
        assertTrue(capturedOutput.contains("Memory Access Error: No device mapped to address 0x${unmappedAddress.toString(16).uppercase().padStart(4, '0')}"))
    }

    @Test
    @DisplayName("writeByte logs error for unmapped addresses")
    fun testWriteByteUnmappedAddress() {
        MemoryController.initializeMemoryMap(listOf<UByte>(0x01u), 10u)
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
        MemoryController.initializeMemoryMap(romData, 10u) // Small RAM

        val value = MemoryController.readByte(Constants.ROM_START_ADDRESS)
        assertEquals(0x12u.toUByte(), value)
        val value2 = MemoryController.readByte((Constants.ROM_START_ADDRESS + 1u).toUShort())
        assertEquals(0x34u.toUByte(), value2)
    }

    @Test
    @DisplayName("readByte correctly routes to RAM")
    fun testReadByteRoutesToRam() {
        MemoryController.initializeMemoryMap(listOf<UByte>(0x01u), 10u) // Small ROM
        val ramAddress = Constants.RAM_START_ADDRESS
        MemoryController.writeByte(ramAddress, 0x55u) // Write to RAM
        val value = MemoryController.readByte(ramAddress)
        assertEquals(0x55u.toUByte(), value)
    }

    @Test
    @DisplayName("writeByte correctly routes to RAM")
    fun testWriteByteRoutesToRam() {
        MemoryController.initializeMemoryMap(listOf<UByte>(0x01u), 10u)
        val ramAddress = Constants.RAM_START_ADDRESS
        MemoryController.writeByte(ramAddress, 0xAAu)
        assertEquals(0xAAu.toUByte(), MemoryController.readByte(ramAddress))
    }

    @Test
    @DisplayName("writeByte to ROM throws RomWriteAttemptException")
    fun testWriteByteToRomThrowsException() {
        val romData = listOf<UByte>(0x12u)
        MemoryController.initializeMemoryMap(romData, 10u)

        val romAddress = Constants.ROM_START_ADDRESS
        val exception = assertThrows(RomWriteAttemptException::class.java) {
            MemoryController.writeByte(romAddress, 0xFFu)
        }
        assertTrue(exception.message!!.contains("Attempted to write to ROM at address 0x0000"))
        // Verify ROM content remains unchanged
        assertEquals(0x12u.toUByte(), MemoryController.readByte(romAddress))
    }

    @Test
    @DisplayName("addDevice correctly adds and routes to custom MemoryDevice")
    fun testAddDeviceAndRouting() {
        // Create a mock MemoryDevice for testing addDevice
        class MockMemoryDevice(start: UShort, size: UShort) : MemoryDevice(start, size) {
            var readCalls = AtomicInteger(0)
            var writeCalls = AtomicInteger(0)
            var lastWrittenValue: UByte = 0u

            override fun readByte(address: UShort): UByte {
                readCalls.incrementAndGet()
                return 0xEEu // Return a distinct value
            }

            override fun writeByte(address: UShort, value: UByte) {
                writeCalls.incrementAndGet()
                lastWrittenValue = value
            }
        }

        val customDeviceAddress = 0x3000u.toUShort()
        val customDeviceSize = 10u.toUShort()
        val mockDevice = MockMemoryDevice(customDeviceAddress, customDeviceSize)

        // Initialize MemoryController without the custom device first
        MemoryController.initializeMemoryMap(listOf<UByte>(0x01u), 10u)

        // Add the custom device
        MemoryController.addDevice(mockDevice)

        // Test reading from the custom device
        val readValue = MemoryController.readByte(customDeviceAddress)
        assertEquals(0xEEu.toUByte(), readValue)
        assertEquals(1, mockDevice.readCalls.get())
        assertEquals(0, mockDevice.writeCalls.get())

        // Test writing to the custom device
        val writeValue = 0x77u.toUByte()
        MemoryController.writeByte((customDeviceAddress + 5u).toUShort(), writeValue)
        assertEquals(1, mockDevice.readCalls.get())
        assertEquals(1, mockDevice.writeCalls.get())
        assertEquals(writeValue, mockDevice.lastWrittenValue)

        // Ensure ROM/RAM still work
        assertEquals(0x01u.toUByte(), MemoryController.readByte(Constants.ROM_START_ADDRESS))
    }
}