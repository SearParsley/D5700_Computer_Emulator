object Constants {

    // CPU Core Details
    const val CPU_FREQUENCY_HZ = 500
    const val CPU_PERIOD_MS = 2L
    const val CPU_INSTRUCTION_BYTES = 2
    const val CPU_PC_INCREMENT_DEFAULT = 2
    const val CPU_INSTRUCTIONS_TOTAL = 16

    // Memory Sizes
    const val ROM_SIZE_KB = 4
    const val ROM_SIZE_BYTES = 4096
    const val RAM_SIZE_KB = 4
    const val RAM_SIZE_BYTES = 4096

    // Timer Details
    const val TIMER_DECREMENT_HZ = 60
    const val TIMER_DECREMENT_MS = 16L

    // ASCII Display Details
    const val ASCII_DISPLAY_WIDTH_CHARS = 8
    const val ASCII_DISPLAY_HEIGHT_CHARS = 8
    const val ASCII_DISPLAY_BUFFER_SIZE_BYTES = 64
    const val ASCII_DISPLAY_DEFAULT_CHAR_CODE = 0x20

    // Keyboard Details
    const val KEYBOARD_BUFFER_SIZE_BYTES = 1

    // Memory Map Addresses
    const val PROGRAM_COUNTER_START_ADDRESS: UShort = 0x0000u
    val ROM_START_ADDRESS: UShort = 0x0000u
    val RAM_START_ADDRESS: UShort = (ROM_START_ADDRESS + ROM_SIZE_BYTES.toUShort()).toUShort()
    val KEYBOARD_START_ADDRESS: UShort = (RAM_START_ADDRESS + RAM_SIZE_BYTES.toUShort()).toUShort()
    val ASCII_DISPLAY_START_ADDRESS: UShort = (KEYBOARD_START_ADDRESS + KEYBOARD_BUFFER_SIZE_BYTES.toUShort()).toUShort()
}