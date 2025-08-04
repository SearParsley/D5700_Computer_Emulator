object FakeRegisters : IRegisters {
    override val r = MutableList<UByte>(8) { 0u }
    override var P: UShort = 0u
    override var T: UByte = 0u
    override var A: UShort = 0u
    override var M: Boolean = false

    var timerControlStatus: UByte = 0u

    fun resetForTesting() {
        r.fill(0u)
        P = 0u
        T = 0u
        A = 0u
        M = false
        timerControlStatus = 0u
    }
}

object FakeMemoryController : IMemoryController {
    var initializeCalled = false
    var addedDevices = mutableListOf<MemoryDevice>()

    override fun initializeMemoryMap(romStart: UShort, romData: List<UByte>, ramStart: UShort, ramSize: UShort) {
        initializeCalled = true
        addedDevices.clear()

        // This is the new, crucial logic. We create and add the devices just like the real MemoryController.
        val rom = MemoryDeviceFactory.createROM(romStart, romData.size.toUShort(), romData)
        addDevice(rom)
        val ram = MemoryDeviceFactory.createRAM(ramStart, ramSize)
        addDevice(ram)
    }

    override fun addDevice(device: MemoryDevice) {
        addedDevices.add(device)
    }

    override fun readByte(address: UShort): UByte {
        val device = addedDevices.find { it.isInRange(address) }
        return device?.readByte(address) ?: 0xFFu
    }

    override fun writeByte(address: UShort, value: UByte) {
        val device = addedDevices.find { it.isInRange(address) }
        device?.writeByte(address, value)
    }

    fun resetForTesting() {
        initializeCalled = false
        addedDevices.clear()
    }
}

class FakeTimerUnit(private val registers: IRegisters) : ITimerUnit {
    var shutdownCalled = false
    var setTimerValueCalled = false

    override fun setTimerValue(value: UByte) { setTimerValueCalled = true }
    override fun shutdown() { shutdownCalled = true }
}

class FakeCPU(private val registers: IRegisters, private val memoryController: IMemoryController, private val timerUnit: ITimerUnit) {
    var initializeCalled = false
    var fetchDecodeExecuteCalled = false

    fun initialize() { initializeCalled = true }
    fun fetchDecodeExecuteCycle() { fetchDecodeExecuteCalled = true }
    fun getInstructionSet(): InstructionSet = InstructionSet.ADD // Return a dummy instruction set
}

// A fake MemoryDeviceFactory for testing
object FakeMemoryDeviceFactory : IMemoryDeviceFactory {
    var createKeyboardCalled = false
    var createDisplayCalled = false
    var createRamCalled = false
    var createRomCalled = false

    override fun createKeyboardInputDevice(startAddress: UShort, size: UShort): KeyboardInputDevice {
        createKeyboardCalled = true
        return KeyboardInputDevice(startAddress, size)
    }

    override fun createRAM(startAddress: UShort, size: UShort): RAM {
        createRamCalled = true
        return RAM(startAddress, size)
    }

    override fun createROM(startAddress: UShort, size: UShort, initialData: List<UByte>): ROM {
        createRomCalled = true
        return ROM(startAddress, size, initialData)
    }

    override fun createAsciiDisplayDevice(startAddress: UShort, size: UShort): AsciiDisplayDevice {
        createDisplayCalled = true
        return AsciiDisplayDevice(startAddress, size)
    }

    fun resetForTesting() {
        createKeyboardCalled = false
        createDisplayCalled = false
        createRamCalled = false
        createRomCalled = false
    }
}