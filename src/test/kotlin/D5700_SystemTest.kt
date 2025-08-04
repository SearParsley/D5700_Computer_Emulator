import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("D5700_System Facade Tests")
class D5700_SystemTest {

    private val TEST_RAM_SIZE = 4096u.toUShort()
    private val TEST_ROM_PATH = "roms/test_system_rom.bin"
    private val TEST_ROM_DATA = "0A0B0C0D".decodeHexToUByteList()

    val registers = FakeRegisters
    val memoryController = FakeMemoryController
    val timerUnit = FakeTimerUnit(registers)
    val memoryDeviceFactory = FakeMemoryDeviceFactory
    private lateinit var system: D5700_System

    // Helper to decode a hex string to List<UByte>
    private fun String.decodeHexToUByteList(): List<UByte> {
        return this.chunked(2).map { it.toUByte(16) }
    }

    @BeforeEach
    fun setUp() {
        // Use the fake singletons and factory
        FakeRegisters.resetForTesting()
        FakeMemoryController.resetForTesting()
        FakeMemoryDeviceFactory.resetForTesting()

        // Create a dummy ROM file for testing
        val romFile = File(TEST_ROM_PATH)
        romFile.parentFile?.mkdirs()
        romFile.writeBytes(TEST_ROM_DATA.map { it.toByte() }.toByteArray())

        system = D5700_System(
            registers = registers,
            memoryController = memoryController,
            timerUnit = timerUnit,
            memoryDeviceFactory = memoryDeviceFactory,
            totalRamSize = TEST_RAM_SIZE,
            romFilePath = TEST_ROM_PATH
        )
    }

    @AfterEach
    fun tearDown() {
        // Clean up the dummy ROM file
        Files.deleteIfExists(Paths.get(TEST_ROM_PATH))
    }

    @Test
    @DisplayName("initialize() correctly sets up memory map and devices")
    fun testInitializeCorrectlySetsUpSystem() {
        system.initialize()

        // Assert MemoryController was initialized with correct parameters
        assertTrue(FakeMemoryController.initializeCalled, "MemoryController.initializeMemoryMap should be called")

        // Assert that the romData was passed correctly
        val romDevice = FakeMemoryController.addedDevices.firstOrNull { it.startAddress == Constants.ROM_START_ADDRESS }
        assertNotNull(romDevice)

        // Assert I/O devices were created and added
        assertTrue(FakeMemoryDeviceFactory.createKeyboardCalled, "Keyboard device should be created by factory")
        assertTrue(FakeMemoryDeviceFactory.createDisplayCalled, "Display device should be created by factory")
        assertEquals(4, FakeMemoryController.addedDevices.size, "4 devices (ROM, RAM, Keyboard, Display) should be added")
    }

    @Test
    @DisplayName("startEmulation() sets running state and schedules CPU cycles")
    fun testStartEmulationSchedulesCpu() {
        system.initialize()
        system.startEmulation()

        // Use a small sleep to let the scheduler thread start
        Thread.sleep(10)

        // Assert the state flags are set correctly
        assertTrue(system.isEmulationRunning, "isRunning should be true")
        assertFalse(system.isEmulationPaused, "isPaused should be false")
    }

    @Test
    @DisplayName("pauseEmulation() and resumeEmulation() correctly toggle pause state")
    fun testPauseAndResumeEmulation() {
        system.initialize()
        system.startEmulation()
        assertTrue(system.isEmulationRunning)
        assertFalse(system.isEmulationPaused)

        system.pauseEmulation()
        assertTrue(system.isEmulationPaused, "pauseEmulation should set isPaused to true")

        system.resumeEmulation()
        assertFalse(system.isEmulationPaused, "resumeEmulation should set isPaused to false")
        assertTrue(system.isEmulationRunning, "resumeEmulation should set isRunning to true")
    }

    @Test
    @DisplayName("shutdown() cancels tasks and sets running state to false")
    fun testShutdown() {
        system.initialize()
        system.startEmulation()

        system.shutdown()

        assertFalse(system.isEmulationRunning, "shutdown should set isRunning to false")
    }

    private fun List<UByte>.toByteArray(): ByteArray {
        val byteArray = ByteArray(this.size)
        this.forEachIndexed { index, uByte ->
            byteArray[index] = uByte.toByte()
        }
        return byteArray
    }
}