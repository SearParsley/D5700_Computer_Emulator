class RAM(startAddress: UShort, size: UShort) : MemoryDevice(startAddress, size) {
    private val memoryArray = MutableList<UByte>(size.toInt()) { 0u }

    override fun doReadByte(offset: UShort): UByte {
        return memoryArray[offset.toInt()]
    }

    override fun doWriteByte(offset: UShort, value: UByte) {
        memoryArray[offset.toInt()] = value
    }
}