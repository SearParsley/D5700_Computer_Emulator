interface IRegisters {
    val r: MutableList<UByte>
    var T: UByte
    var P: UShort
    var A: UShort
    var M: Boolean
    fun getGeneralPurposeRegister(index: Int): UByte {return 0u}
    fun setGeneralPurposeRegister(index: Int, value: UByte) {}
}

interface IMemoryController {
    fun initializeMemoryMap(romStart: UShort, romData: List<UByte>, ramStart: UShort, ramSize: UShort)
    fun addDevice(device: MemoryDevice)
    fun readByte(address: UShort): UByte
    fun writeByte(address: UShort, value: UByte)
}

interface ITimerUnit {
    fun setTimerValue(value: UByte)
    fun startDecrementing() {}
    fun stopDecrementing() {}
    fun shutdown()
}

interface IMemoryDeviceFactory {
    fun createRAM(startAddress: UShort, size: UShort): RAM
    fun createROM(startAddress: UShort, size: UShort, initialData: List<UByte>): ROM
    fun createAsciiDisplayDevice(startAddress: UShort, size: UShort): AsciiDisplayDevice
    fun createKeyboardInputDevice(startAddress: UShort, size: UShort): KeyboardInputDevice
}