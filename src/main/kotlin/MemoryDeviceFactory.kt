object MemoryDeviceFactory : IMemoryDeviceFactory {

    override fun createRAM(startAddress: UShort, size: UShort): RAM {
        return RAM(startAddress, size)
    }

    override fun createROM(startAddress: UShort, size: UShort, initialData: List<UByte>): ROM {
        return ROM(startAddress, size, initialData)
    }

    override fun createAsciiDisplayDevice(startAddress: UShort, size: UShort): AsciiDisplayDevice {
        return AsciiDisplayDevice(startAddress, size)
    }

    override fun createKeyboardInputDevice(startAddress: UShort, size: UShort): KeyboardInputDevice {
        return KeyboardInputDevice(startAddress, size)
    }
}