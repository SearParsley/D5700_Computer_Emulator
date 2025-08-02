class ROM(startAddress: UShort, size: UShort, initialData: List<UByte>) : MemoryDevice(startAddress, size) {
    private val memoryArray = MutableList<UByte>(size.toInt()) { 0u }

    init {
        for (i in initialData.indices) {
            if (i < size.toInt()) {
                memoryArray[i] = initialData[i]
            } else {
                println("ROM Initialization Warning: Initial data for ROM at 0x${startAddress.toString(16).uppercase().padStart(4, '0')} exceeds its defined size (${size}). Data truncated.")
                break
            }
        }
    }

    override fun readByte(address: UShort): UByte {
        val offset = getLocalOffset(address)
        return memoryArray[offset.toInt()]
    }

    override fun writeByte(address: UShort, value: UByte) {
        throw RomWriteAttemptException("Attempted to write to ROM at address 0x${address.toString(16).uppercase().padStart(4, '0')}.")
    }
}

class RomWriteAttemptException(message: String) : RuntimeException(message)