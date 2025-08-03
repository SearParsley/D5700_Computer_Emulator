import java.io.File

fun main() {
    println("--- Starting D5700 Emulator Application ---")

    // --- 1. Prompt the user for a ROM file path ---
    print("Please enter the path to the D5700 ROM file: ")
    val romFileName = readLine()

    // --- 2. Validate the file path ---
    if (romFileName.isNullOrBlank()) {
        println("Error: No ROM file path provided. Exiting.")
        return
    }

    val romFile = File(romFileName)
    if (!romFile.exists()) {
        println("Error: ROM file not found at '$romFileName'. Exiting.")
        return
    }

    // --- 3. Instantiate the D5700_System facade ---
    val system = D5700_System(
        totalRamSize = Constants.RAM_SIZE_BYTES.toUShort(), // 4KB of RAM
        romFilePath = romFileName
    )

    // --- 4. Initialize the system ---
    system.initialize()

    // --- 5. Start the emulation loop in the background ---
    println("Emulation loop starting at ${Constants.CPU_FREQUENCY_HZ}Hz.")
    println("The emulator will automatically pause when it encounters a READ_KEYBOARD instruction.")
    system.startEmulation()
}