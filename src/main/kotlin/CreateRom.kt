import java.io.File

// This object holds all the fixed constant values for the D5700 CPU.
// (You should have this in your main project, but duplicated here for standalone execution)
fun main() {
    println("--- D5700 ROM Program Generator ---")

    val romFileName = "roms/test_program.bin"
    val romFile = File(romFileName)
    romFile.parentFile?.mkdirs() // Ensure 'roms' directory exists

    // Define the program instructions as a List<UByte>
    val programInstructions = listOf(
        // Address 0x0000
        (0x0 shl 4 or 0x0).toUByte(), 0x41u, // STORE r0, 0x41 ('A')
        // Address 0x0002
        (0xF shl 4 or 0x0).toUByte(), (0x6 shl 4 or 0x7).toUByte(), // DRAW r0, r6, r7. Assumes r6=0, r7=0
        // Address 0x0004
        (0x0 shl 4 or 0x1).toUByte(), 0x42u, // STORE r1, 0x42 ('B')
        // Address 0x0006
        (0xF shl 4 or 0x1).toUByte(), (0x6 shl 4 or 0x7).toUByte(), // DRAW r1, r6, r7. Assumes r6=0, r7=1
        // Address 0x0008
        (0x0 shl 4 or 0x2).toUByte(), 0x43u, // STORE r2, 0x43 ('C')
        // Address 0x000A
        (0xF shl 4 or 0x2).toUByte(), (0x6 shl 4 or 0x7).toUByte(), // DRAW r2, r6, r7. Assumes r6=0, r7=2
        // Address 0x000C
        (0x6 shl 4 or 0x0).toUByte(), 0x00u, // READ_KEYBOARD r0, 00 (Read into r0)
        // Address 0x000E
        (0x0 shl 4 or 0x3).toUByte(), 0x00u, // STORE r3, 0x00 (Store row 0 in r3)
        // Address 0x0010
        (0x0 shl 4 or 0x4).toUByte(), 0x01u, // STORE r4, 0x01 (Store col 1 in r4)
        // Address 0x0012
        (0xF shl 4 or 0x0).toUByte(), (0x3 shl 4 or 0x4).toUByte(), // DRAW r0, r3, r4 (Draw char from r0 at row from r3 and col from r4)
        // Address 0x0014
        (0x5 shl 4 or 0x00).toUByte(), 0x0Cu.toUByte() // JUMP 0x000C
    )

    // Create a full 4KB ROM, padded with NOPs (0x00)
    val fullRomContent = MutableList<UByte>(Constants.ROM_SIZE_BYTES) { 0x00u }
    programInstructions.forEachIndexed { index, byte ->
        if (index < fullRomContent.size) {
            fullRomContent[index] = byte
        }
    }

    // Write the List<UByte> to a file as a ByteArray
    romFile.writeBytes(fullRomContent.map { it.toByte() }.toByteArray())

    println("Successfully created D5700 test ROM: ${romFile.absolutePath}")
    println("ROM size: ${fullRomContent.size} bytes")
    println("Program length: ${programInstructions.size} bytes")
}