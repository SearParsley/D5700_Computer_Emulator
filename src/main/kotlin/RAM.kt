class RAM(startAddress: UShort, size: UShort) : MemoryDevice(startAddress, size) {
    private val memoryArray = MutableList<UByte>(size.toInt()) { 0u }

    override fun readByte(address: UShort): UByte {
        val offset = getLocalOffset(address)
        return memoryArray[offset.toInt()]
    }

    override fun writeByte(address: UShort, value: UByte) {
        val offset = getLocalOffset(address)
        memoryArray[offset.toInt()] = value
    }
}