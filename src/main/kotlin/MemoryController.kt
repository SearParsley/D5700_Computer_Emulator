object MemoryController {

    private val memoryDevices = mutableListOf<MemoryDevice>()

    fun initializeMemoryMap(romData: List<UByte>, ramSize: UShort) {
        memoryDevices.clear()

        val rom = ROM(Constants.ROM_START_ADDRESS, Constants.ROM_SIZE_BYTES.toUShort(), romData)
        addDevice(rom)

        val ram = RAM(Constants.RAM_START_ADDRESS, ramSize)
        addDevice(ram)

        println("MemoryController: Memory map initialized.")
        println("  ROM: 0x${Constants.ROM_START_ADDRESS.toString(16).uppercase().padStart(4, '0')} - 0x${(Constants.ROM_START_ADDRESS + Constants.ROM_SIZE_BYTES.toUByte() - 1u).toString(16).uppercase().padStart(4, '0')} (${Constants.ROM_SIZE_BYTES} bytes)")
        println("  RAM: 0x${Constants.RAM_START_ADDRESS.toString(16).uppercase().padStart(4, '0')} - 0x${(Constants.RAM_START_ADDRESS + ramSize - 1u).toString(16).uppercase().padStart(4, '0')} (${ramSize} bytes)")
    }

    fun addDevice(device: MemoryDevice) {
        memoryDevices.add(device)
    }

    fun readByte(address: UShort): UByte {
        val device = findDevice(address)
        return device?.readByte(address) ?: run {
            println("Memory Access Error: No device mapped to address 0x${address.toString(16).uppercase().padStart(4, '0')}")
            0xFFu
        }
    }

    fun writeByte(address: UShort, value: UByte) {
        val device = findDevice(address)
        device?.writeByte(address, value) ?: run {
            println("Memory Access Error: No device mapped to address 0x${address.toString(16).uppercase().padStart(4, '0')}")
        }
    }

    private fun findDevice(address: UShort): MemoryDevice? {
        return memoryDevices.find { it.isInRange(address) }
    }

    internal fun resetForTesting() {
        memoryDevices.clear()
    }
}