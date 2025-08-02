object MemoryDeviceFactory {

    fun createRAM(startAddress: UShort, size: UShort): RAM {
        return RAM(startAddress, size)
    }

    fun createROM(startAddress: UShort, size: UShort, initialData: List<UByte>): ROM {
        return ROM(startAddress, size, initialData)
    }

    fun createAsciiDisplayDevice(startAddress: UShort, size: UShort): AsciiDisplayDevice {
        return AsciiDisplayDevice(startAddress, size)
    }

    fun createKeyboardInputDevice(startAddress: UShort, size: UShort): KeyboardInputDevice {
        return KeyboardInputDevice(startAddress, size)
    }
}