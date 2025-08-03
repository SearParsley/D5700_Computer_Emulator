class CPU(
    private val timerUnit: TimerUnit
) {

    private val instructionMap: Map<Int, InstructionStrategy> = mapOf(
        0x0 to StoreInstruction(),
        0x1 to AddInstruction(),
        0x2 to SubInstruction(),
        0x3 to ReadInstruction(),
        0x4 to WriteInstruction(),
        0x5 to JumpInstruction(),
        0x6 to ReadKeyboardInstruction(),
        0x7 to SwitchMemoryInstruction(),
        0x8 to SkipEqualInstruction(),
        0x9 to SkipNotEqualInstruction(),
        0xA to SetAInstruction(),
        0xB to SetTInstruction(),
        0xC to ReadTInstruction(),
        0xD to ConvertToBase10Instruction(),
        0xE to ConvertByteToAsciiInstruction(),
        0xF to DrawInstruction(),
    )

    fun fetchDecodeExecuteCycle() {
        val programCounter = Registers.P

        val byte1 = MemoryController.readByte(programCounter)
        val byte2 = MemoryController.readByte((programCounter + 1u).toUShort())

        val opcodeNibble = (byte1.toInt() ushr 4)

        val instruction = instructionMap[opcodeNibble]
            ?: throw ProgramTerminationException("Unknown opcode 0x${opcodeNibble.toString(16).uppercase()} at address 0x${programCounter.toString(16).uppercase().padStart(4, '0')}")

        instruction.execute(this, byte1, byte2)
    }

    fun run(cycles: Int) {
        var iter = 0
        while (iter < cycles) {
            fetchDecodeExecuteCycle()
            iter--
        }
    }
}

class ProgramTerminationException(message: String) : RuntimeException(message)