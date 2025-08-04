import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.io.File
import java.util.TimerTask

class D5700_System(
    private val registers: IRegisters,
    private val memoryController: IMemoryController,
    private val timerUnit: ITimerUnit,
    private val memoryDeviceFactory: IMemoryDeviceFactory,
    private val totalRamSize: UShort,
    private val romFilePath: String
) {
    private lateinit var cpu: CPU

    private var keyboardInputDevice: KeyboardInputDevice? = null
    private var asciiDisplayDevice: AsciiDisplayDevice? = null

    private val cpuScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var cpuExecutionTask: TimerTask? = null
    internal var isEmulationRunning: Boolean = false
    internal var isEmulationPaused: Boolean = false

    private val CPU_FREQUENCY_HZ = Constants.CPU_FREQUENCY_HZ
    private val CPU_PERIOD_MS = Constants.CPU_PERIOD_MS
    private val ROM_START_ADDRESS = Constants.ROM_START_ADDRESS
    private val ROM_SIZE_BYTES = Constants.ROM_SIZE_BYTES.toUShort()
    private val RAM_START_ADDRESS = Constants.RAM_START_ADDRESS
    private val KEYBOARD_ADDRESS = Constants.KEYBOARD_START_ADDRESS
    private val SCREEN_ADDRESS = Constants.ASCII_DISPLAY_START_ADDRESS
    private val SCREEN_BUFFER_SIZE = Constants.ASCII_DISPLAY_BUFFER_SIZE_BYTES.toUShort()
    private val KEYBOARD_BUFFER_SIZE = Constants.KEYBOARD_BUFFER_SIZE_BYTES.toUShort()

    fun initialize() {
        println("D5700 System: Initializing components...")
        val romData = loadRomFromFile(romFilePath)
        memoryController.initializeMemoryMap(ROM_START_ADDRESS, romData, RAM_START_ADDRESS, totalRamSize)
        println("D5700 System: Memory map initialized (ROM: ${romData.size} bytes, RAM: ${totalRamSize} bytes).")
        keyboardInputDevice = memoryDeviceFactory.createKeyboardInputDevice(KEYBOARD_ADDRESS, KEYBOARD_BUFFER_SIZE)
        memoryController.addDevice(keyboardInputDevice!!)
        asciiDisplayDevice = memoryDeviceFactory.createAsciiDisplayDevice(SCREEN_ADDRESS, SCREEN_BUFFER_SIZE)
        memoryController.addDevice(asciiDisplayDevice!!)
        println("D5700 System: I/O devices added to memory map.")
        cpu = CPU(registers, memoryController, timerUnit)
        println("D5700 System: CPU initialized.")
        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown()
        })
        println("D5700 System: Initialization complete.")
    }

    fun startEmulation() {
        if (isEmulationRunning) {
            println("D5700 System: Emulation is already running.")
            return
        }
        println("D5700 System: Starting emulation loop at ${CPU_FREQUENCY_HZ}Hz...")
        isEmulationRunning = true
        isEmulationPaused = false
        cpuExecutionTask = object : TimerTask() {
            override fun run() {
                if (!isEmulationPaused) {
                    try {
                        cpu.fetchDecodeExecuteCycle()
                    } catch (e: ProgramTerminationException) {
                        println("D5700 System Error: Program terminated: ${e.message}")
                        shutdown()
                    } catch (e: Exception) {
                        println("D5700 System Error: Unhandled emulation exception: ${e.message}")
                        e.printStackTrace()
                        shutdown()
                    }
                }
            }
        }
        cpuScheduler.scheduleAtFixedRate(cpuExecutionTask, 0, CPU_PERIOD_MS, TimeUnit.MILLISECONDS)
    }

    fun pauseEmulation() {
        if (!isEmulationRunning) {
            println("D5700 System: Emulation is not running, cannot pause.")
            return
        }
        if (isEmulationPaused) {
            println("D5700 System: Emulation is already paused.")
            return
        }
        isEmulationPaused = true
        println("D5700 System: Emulation Paused.")
    }

    fun resumeEmulation() {
        if (!isEmulationRunning) {
            println("D5700 System: Emulation is not running, cannot resume.")
            return
        }
        if (!isEmulationPaused) {
            println("D5700 System: Emulation is not paused, nothing to resume.")
            return
        }
        isEmulationPaused = false
        println("D5700 System: Emulation Resumed.")
    }

    fun shutdown() {
        if (!isEmulationRunning && !isEmulationPaused) {
            println("D5700 System: Already shut down or not running.")
            return
        }
        println("D5700 System: Shutting down...")
        isEmulationRunning = false
        isEmulationPaused = false
        cpuExecutionTask?.cancel()
        cpuScheduler.shutdownNow()
        timerUnit.shutdown()
        println("D5700 System: Shutdown complete.")
    }

    fun getDisplayContent(): List<UByte>? {
        return asciiDisplayDevice?.getScreenContent()
    }

    fun getRenderedDisplay(): String? {
        return asciiDisplayDevice?.getRenderedScreen()
    }

    private fun loadRomFromFile(path: String): List<UByte> {
        val file = File(path)
        val romSize = Constants.ROM_SIZE_BYTES
        if (!file.exists()) {
            println("D5700 System Error: ROM file not found at '$path'. Creating dummy ROM.")
            return List(romSize) { 0x00u }
        }
        val bytes = file.readBytes()
        val uByteList = bytes.map { it.toUByte() }
        return when {
            uByteList.size > romSize -> {
                println("D5700 System Warning: ROM file size (${uByteList.size} bytes) is too large. Data will be truncated to $romSize bytes.")
                uByteList.take(romSize)
            }
            uByteList.size < romSize -> {
                println("D5700 System Warning: ROM file size (${uByteList.size} bytes) is too small. Data will be padded with zeros to $romSize bytes.")
                uByteList + List(romSize - uByteList.size) { 0x00u }
            }
            else -> {
                uByteList
            }
        }
    }
}