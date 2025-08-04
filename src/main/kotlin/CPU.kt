class CPU(
    internal val registers: IRegisters,
    internal val memoryController: IMemoryController,
    internal val timerUnit: ITimerUnit
) {

    private val instructionMap: Map<Int, InstructionStrategy> = mapOf(
        0x0 to StoreInstruction(registers),
        0x1 to AddInstruction(registers),
        0x2 to SubInstruction(registers),
        0x3 to ReadInstruction(registers),
        0x4 to WriteInstruction(registers),
        0x5 to JumpInstruction(registers),
        0x6 to ReadKeyboardInstruction(registers),
        0x7 to SwitchMemoryInstruction(registers),
        0x8 to SkipEqualInstruction(registers),
        0x9 to SkipNotEqualInstruction(registers),
        0xA to SetAInstruction(registers),
        0xB to SetTInstruction(registers),
        0xC to ReadTInstruction(registers),
        0xD to ConvertToBase10Instruction(registers),
        0xE to ConvertByteToAsciiInstruction(registers),
        0xF to DrawInstruction(registers),
    )

    fun fetchDecodeExecuteCycle() {
        val programCounter = registers.P
        val byte1 = memoryController.readByte(programCounter)
        val byte2 = memoryController.readByte((programCounter + 1u).toUShort())
        val opcodeNibble = (byte1.toInt() ushr 4)
        val instruction = instructionMap[opcodeNibble]
            ?: throw ProgramTerminationException("Unknown opcode 0x${opcodeNibble.toString(16).uppercase()} at address 0x${programCounter.toString(16).uppercase().padStart(4, '0')}")
        try {
            instruction.execute(this, byte1, byte2)
        } catch (e: Exception) {
            throw ProgramTerminationException("${e.message}")
        }
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