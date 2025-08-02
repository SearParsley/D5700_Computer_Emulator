object MemoryController {

    private val memoryDevices = mutableListOf<MemoryDevice>()

    fun initializeMemoryMap(romStart: UShort, romData: List<UByte>, ramStart: UShort, ramSize: UShort) {
        memoryDevices.clear()

        val rom = MemoryDeviceFactory.createROM(romStart, romData.size.toUShort(), romData)
        addDevice(rom)

        val ram = MemoryDeviceFactory.createRAM(ramStart, ramSize)
        addDevice(ram)

        println("MemoryController: Memory map initialized.")
        println("  ROM: 0x${romStart.toString(16).uppercase().padStart(4, '0')} - 0x${(romStart + romData.size.toUShort() - 1u).toString(16).uppercase().padStart(4, '0')} (${romData.size} bytes)")
        println("  RAM: 0x${ramStart.toString(16).uppercase().padStart(4, '0')} - 0x${(ramStart + ramSize - 1u).toString(16).uppercase().padStart(4, '0')} (${ramSize} bytes)")
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