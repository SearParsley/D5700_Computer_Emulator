class StoreInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode: Int = 0x0
    override val mnemonic: String = "STORE"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.STORE
        val rx = instructionSet.parseRx(byte1)
        val bb = instructionSet.parseBb(byte2)
        registers.setGeneralPurposeRegister(rx, bb)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class AddInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x1
    override val mnemonic = "ADD"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.ADD
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rz = instructionSet.parseRz(byte2)
        val valX = registers.getGeneralPurposeRegister(rx)
        val valY = registers.getGeneralPurposeRegister(ry)
        val result = (valX + valY).toUByte()
        registers.setGeneralPurposeRegister(rz, result)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SubInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x2
    override val mnemonic = "SUB"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SUB
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rz = instructionSet.parseRz(byte2)
        val valX = registers.getGeneralPurposeRegister(rx)
        val valY = registers.getGeneralPurposeRegister(ry)
        val result = (valX - valY).toUByte()
        registers.setGeneralPurposeRegister(rz, result)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ReadInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x3
    override val mnemonic = "READ"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.READ
        val rx = instructionSet.parseRx(byte1)
        val result = MemoryController.readByte(registers.A)
        registers.setGeneralPurposeRegister(rx, result)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class WriteInstruction(override val registers: IRegisters) : InstructionStrategy { // TODO: Figure out memory switching in terms of memory addresses. Put each device in its own memory array? rather than having it all sequential?
    override val opcode = 0x4
    override val mnemonic = "WRITE"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.WRITE
        val rx = instructionSet.parseRx(byte1)
        val data = registers.getGeneralPurposeRegister(rx)
        MemoryController.writeByte(registers.A, data)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class JumpInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x5
    override val mnemonic = "JUMP"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.JUMP
        val aaa = instructionSet.parseAaa(byte1, byte2)
        if (aaa % 2u != 0u) throw JumpToInvalidInstructionException("Attempted to jump to odd-numbered address 0x${aaa.toString(16).uppercase().padStart(4, '0')}")
        registers.P = aaa
    }
}

class ReadKeyboardInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x6
    override val mnemonic = "READ_KEYBOARD"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.READ_KEYBOARD
        val rx = instructionSet.parseRx(byte1)
        val result = MemoryController.readByte(Constants.KEYBOARD_START_ADDRESS)
        registers.setGeneralPurposeRegister(rx, result)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SwitchMemoryInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x7
    override val mnemonic = "SWITCH_MEMORY"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SWITCH_MEMORY
        registers.M = !registers.M
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SkipEqualInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x8
    override val mnemonic = "SKIP_EQUAL"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SKIP_EQUAL
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rxData = registers.getGeneralPurposeRegister(rx)
        val ryData = registers.getGeneralPurposeRegister(ry)
        if  (rxData == ryData) {
            registers.P = (registers.P + (2 * instructionSet.pcIncrement).toUShort()).toUShort()
        } else {
            registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
        }
    }
}

class SkipNotEqualInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0x9
    override val mnemonic = "SKIP_NOT_EQUAL"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SKIP_NOT_EQUAL
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rxData = registers.getGeneralPurposeRegister(rx)
        val ryData = registers.getGeneralPurposeRegister(ry)
        if  (rxData != ryData) {
            registers.P = (registers.P + (2 * instructionSet.pcIncrement).toUShort()).toUShort()
        } else {
            registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
        }
    }
}

class SetAInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0xA
    override val mnemonic = "SET_A"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SET_A
        val aaa = instructionSet.parseAaa(byte1, byte2)
        registers.A = aaa
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SetTInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0xB
    override val mnemonic = "SET_T"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SET_T
        val bb = instructionSet.parseBb(byte2)
        cpu.timerUnit.setTimerValue(bb)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ReadTInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0xC
    override val mnemonic = "READ_T"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.READ_T
        val rx = instructionSet.parseRx(byte1)
        registers.setGeneralPurposeRegister(rx, registers.T)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ConvertToBase10Instruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0xD
    override val mnemonic = "CONVERT_TO_BASE_10"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.CONVERT_TO_BASE_10
        val rx = instructionSet.parseRx(byte1)
        val data = registers.getGeneralPurposeRegister(rx)
        val hundreds = (data / 100u).toUByte()
        val tens = ((data % 100u) / 10u).toUByte()
        val ones = (data % 10u).toUByte()
        MemoryController.writeByte(registers.A, hundreds)
        MemoryController.writeByte((registers.A + 1u).toUShort(), tens)
        MemoryController.writeByte((registers.A + 2u).toUShort(), ones)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ConvertByteToAsciiInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0xE
    override val mnemonic = "CONVERT_BYTE_TO_ASCII"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.CONVERT_BYTE_TO_ASCII
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val data = registers.getGeneralPurposeRegister(rx)
        val result = when (data) {
            in 0x0u..0x9u -> (0x30u + data).toUByte()
            in 0xAu..0xFu -> (0x37u + data).toUByte()
            else -> throw ConvertInvalidByteToAsciiException("Attempted to convert byte greater than 0xF to ASCII (0x${rx.toString(16).uppercase()}).")
        }
        registers.setGeneralPurposeRegister(ry, result)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class DrawInstruction(override val registers: IRegisters) : InstructionStrategy {
    override val opcode = 0xF
    override val mnemonic = "DRAW"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.DRAW
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rz = instructionSet.parseRz(byte2)
        val data = registers.getGeneralPurposeRegister(rx)
        val row = registers.getGeneralPurposeRegister(ry)
        val column = registers.getGeneralPurposeRegister(rz)
        if (data > 0x7Fu) throw DrawInvalidByteToScreenException("Attempted to draw an invalid byte to the screen (0x${rx.toString(16).uppercase()}).")
        if (column >= Constants.ASCII_DISPLAY_WIDTH_CHARS.toUShort() || row >= Constants.ASCII_DISPLAY_HEIGHT_CHARS.toUShort()) throw DrawToInvalidScreenLocationException("Attempted to draw to an invalid screen location: (${row}, ${column})")
        val index = (row * Constants.ASCII_DISPLAY_WIDTH_CHARS.toUShort()) + column
        MemoryController.writeByte((Constants.ASCII_DISPLAY_START_ADDRESS + index.toUShort()).toUShort(), data)
        val screenContent = MemoryController.getRenderedScreen()
        println(screenContent)
        registers.P = (registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class JumpToInvalidInstructionException(message: String) : RuntimeException(message)
class ConvertInvalidByteToAsciiException(message: String) : RuntimeException(message)
class DrawInvalidByteToScreenException(message: String) : RuntimeException(message)
class DrawToInvalidScreenLocationException(message: String) : RuntimeException(message)