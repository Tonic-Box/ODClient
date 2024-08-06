package osrs.dev.modder.model.ast;

import javassist.CtBehavior;
import javassist.bytecode.*;
import osrs.dev.modder.model.ast.enums.BlockType;
import osrs.dev.modder.model.ast.enums.LineType;
import osrs.dev.modder.model.ast.instructions.*;

import java.util.ArrayList;
import java.util.List;

public class Iterator
{
    public static <T extends CtBehavior> List<InstructionLine> run(T method)
    {
        return run(method.getMethodInfo(), method.getMethodInfo2().getCodeAttribute().iterator());
    }

    public static <T extends CtBehavior>  List<CodeBlock> getCleanFlow(T method)
    {
        List<InstructionLine> lines = AstUtils.smooshStrings(Iterator.run(method));
        return AstUtils.combineGarbs(AstUtils.getCodeBlocks(lines));
    }

    public static List<InstructionLine> run(MethodInfo methodInfo, CodeIterator codeIterator)
    {
        return new ArrayList<>()
        {{
            ConstPool constPool = methodInfo.getConstPool();
            int line = 0;
            int pos;
            while (codeIterator.hasNext()) {
                line++;
                try
                {
                    pos = codeIterator.next();
                    int opcode = codeIterator.byteAt(pos);
                    int length = codeIterator.hasNext() ? codeIterator.lookAhead() - pos : codeIterator.getCodeLength() - pos;

                    switch(opcode)
                    {
                        case Opcode.GETFIELD:
                        case Opcode.GETSTATIC:
                        case Opcode.PUTFIELD:
                        case Opcode.PUTSTATIC:
                            add(new FieldLine(codeIterator,constPool,pos, length));
                            break;

                        case Opcode.INVOKEINTERFACE:
                        case Opcode.INVOKESTATIC:
                        case Opcode.INVOKEVIRTUAL:
                        case Opcode.INVOKESPECIAL:
                            add(new MethodLine(codeIterator, constPool, pos, length));
                            break;

                        case Opcode.LDC:
                        case Opcode.LDC_W:
                        case Opcode.LDC2_W:
                        case Opcode.SIPUSH:
                        case Opcode.BIPUSH:
                        case Opcode.ICONST_M1:
                        case Opcode.ICONST_0:
                        case Opcode.ICONST_1:
                        case Opcode.ICONST_2:
                        case Opcode.ICONST_3:
                        case Opcode.ICONST_4:
                        case Opcode.ICONST_5:
                        case Opcode.LCONST_0:
                        case Opcode.LCONST_1:
                            try
                            {
                                add(new ValueLine(codeIterator, constPool, pos, length));
                            }
                            catch (Exception ignored)
                            {
                                add(new InstructionLine(codeIterator, codeIterator.byteAt(pos), Mnemonic.OPCODE[codeIterator.byteAt(pos)], LineType.OTHER, pos, constPool, length));
                            }
                            break;

                        case Opcode.GOTO:
                        case Opcode.GOTO_W:
                        case Opcode.JSR:
                        case Opcode.JSR_W:
                            add(new JumpLine(codeIterator, constPool, pos, length));
                            break;

                        case Opcode.IFEQ:
                        case Opcode.IFGE:
                        case Opcode.IFGT:
                        case Opcode.IFLE:
                        case Opcode.IFLT:
                        case Opcode.IFNE:
                        case Opcode.IFNONNULL:
                        case Opcode.IFNULL:
                        case Opcode.IF_ACMPEQ:
                        case Opcode.IF_ACMPNE:
                        case Opcode.IF_ICMPEQ:
                        case Opcode.IF_ICMPGE:
                        case Opcode.IF_ICMPGT:
                        case Opcode.IF_ICMPLE:
                        case Opcode.IF_ICMPLT:
                        case Opcode.IF_ICMPNE:
                            add(new IfLine(codeIterator, constPool, pos, length));
                            break;

                        case Opcode.ILOAD:
                        case Opcode.ILOAD_0:
                        case Opcode.ILOAD_1:
                        case Opcode.ILOAD_2:
                        case Opcode.ILOAD_3:
                        case Opcode.LLOAD:
                        case Opcode.LLOAD_0:
                        case Opcode.LLOAD_1:
                        case Opcode.LLOAD_2:
                        case Opcode.LLOAD_3:
                        case Opcode.FLOAD:
                        case Opcode.FLOAD_0:
                        case Opcode.FLOAD_1:
                        case Opcode.FLOAD_2:
                        case Opcode.FLOAD_3:
                        case Opcode.DLOAD:
                        case Opcode.DLOAD_0:
                        case Opcode.DLOAD_1:
                        case Opcode.DLOAD_2:
                        case Opcode.DLOAD_3:
                        case Opcode.ALOAD:
                        case Opcode.ALOAD_0:
                        case Opcode.ALOAD_1:
                        case Opcode.ALOAD_2:
                        case Opcode.ALOAD_3:
                        case Opcode.ISTORE:
                        case Opcode.ISTORE_0:
                        case Opcode.ISTORE_1:
                        case Opcode.ISTORE_2:
                        case Opcode.ISTORE_3:
                        case Opcode.LSTORE:
                        case Opcode.LSTORE_0:
                        case Opcode.LSTORE_1:
                        case Opcode.LSTORE_2:
                        case Opcode.LSTORE_3:
                        case Opcode.FSTORE:
                        case Opcode.FSTORE_0:
                        case Opcode.FSTORE_1:
                        case Opcode.FSTORE_2:
                        case Opcode.FSTORE_3:
                        case Opcode.DSTORE:
                        case Opcode.DSTORE_0:
                        case Opcode.DSTORE_1:
                        case Opcode.DSTORE_2:
                        case Opcode.DSTORE_3:
                        case Opcode.ASTORE:
                        case Opcode.ASTORE_0:
                        case Opcode.ASTORE_1:
                        case Opcode.ASTORE_2:
                        case Opcode.ASTORE_3:
                            add(new LocalLine(codeIterator, constPool, pos, length));
                            break;

                        case Opcode.NEW:
                        case Opcode.NEWARRAY:
                        case Opcode.ANEWARRAY:
                        case Opcode.MULTIANEWARRAY:
                            add(new InitLine(codeIterator, constPool, pos, length));
                            break;

                        case Opcode.NOP:
                        case Opcode.DUP:
                        case Opcode.DUP2:
                        case Opcode.DUP2_X1:
                        case Opcode.DUP2_X2:
                        case Opcode.DUP_X1:
                        case Opcode.DUP_X2:
                        case Opcode.POP:
                        case Opcode.POP2:
                        case Opcode.SWAP:
                            add(new StackLine(codeIterator, constPool, pos, length));
                            break;

                        case Opcode.IADD:
                        case Opcode.LADD:
                        case Opcode.FADD:
                        case Opcode.DADD:
                        case Opcode.ISUB:
                        case Opcode.LSUB:
                        case Opcode.FSUB:
                        case Opcode.DSUB:
                        case Opcode.IMUL:
                        case Opcode.LMUL:
                        case Opcode.FMUL:
                        case Opcode.DMUL:
                        case Opcode.IDIV:
                        case Opcode.LDIV:
                        case Opcode.FDIV:
                        case Opcode.DDIV:
                        case Opcode.IREM:
                        case Opcode.LREM:
                        case Opcode.FREM:
                        case Opcode.DREM:
                        case Opcode.ISHL:
                        case Opcode.LSHL:
                        case Opcode.ISHR:
                        case Opcode.LSHR:
                        case Opcode.IUSHR:
                        case Opcode.LUSHR:
                        case Opcode.IAND:
                        case Opcode.LAND:
                        case Opcode.IOR:
                        case Opcode.LOR:
                        case Opcode.IXOR:
                        case Opcode.LXOR:
                            add(new ArithmeticLine(codeIterator, constPool, pos, length));
                            break;

                        default:
                            add(new InstructionLine(codeIterator, codeIterator.byteAt(pos), Mnemonic.OPCODE[codeIterator.byteAt(pos)], LineType.OTHER, pos, constPool, length));
                    }
                }
                catch (Exception ex)
                {
                    System.out.println("[Line " + line + "]" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }};
    }

    public static BlockType isEndOfLine(InstructionLine line, int lastOpcode) {
        switch (line.getOpcode()) {
            // Return instructions
            case Opcode.RETURN:
            case Opcode.ARETURN:
            case Opcode.DRETURN:
            case Opcode.FRETURN:
            case Opcode.IRETURN:
            case Opcode.LRETURN:
                return BlockType.RETURN;

            // Throw instructions
            case Opcode.ATHROW:
                return BlockType.THROW;

            // Store instructions
            case Opcode.ASTORE:
            case Opcode.ASTORE_0:
            case Opcode.ASTORE_1:
            case Opcode.ASTORE_2:
            case Opcode.ASTORE_3:
            case Opcode.DSTORE:
            case Opcode.DSTORE_0:
            case Opcode.DSTORE_1:
            case Opcode.DSTORE_2:
            case Opcode.DSTORE_3:
            case Opcode.FSTORE:
            case Opcode.FSTORE_0:
            case Opcode.FSTORE_1:
            case Opcode.FSTORE_2:
            case Opcode.FSTORE_3:
            case Opcode.ISTORE:
            case Opcode.ISTORE_0:
            case Opcode.ISTORE_1:
            case Opcode.ISTORE_2:
            case Opcode.ISTORE_3:
            case Opcode.LSTORE:
            case Opcode.LSTORE_0:
            case Opcode.LSTORE_1:
            case Opcode.LSTORE_2:
            case Opcode.LSTORE_3:
                return BlockType.LOCAL_STORE;

            // Conditional branch instructions
            case Opcode.IF_ACMPEQ:
            case Opcode.IF_ACMPNE:
            case Opcode.IF_ICMPEQ:
            case Opcode.IF_ICMPGE:
            case Opcode.IF_ICMPGT:
            case Opcode.IF_ICMPLE:
            case Opcode.IF_ICMPLT:
            case Opcode.IF_ICMPNE:
            case Opcode.IFEQ:
            case Opcode.IFGE:
            case Opcode.IFGT:
            case Opcode.IFLE:
            case Opcode.IFLT:
            case Opcode.IFNE:
            case Opcode.IFNONNULL:
            case Opcode.IFNULL:
                return BlockType.CONDITION;

            // Unconditional branch instructions
            case Opcode.GOTO:
            case Opcode.GOTO_W:
                return BlockType.GOTO;

            // Switch instruction
            case Opcode.TABLESWITCH:
            case Opcode.LOOKUPSWITCH:
                return BlockType.SWITCH;

            // Pop instructions
            case Opcode.POP:
            case Opcode.POP2:
                // An invoke instruction followed by a POP instruction often marks the end of a line of code
                if (lastOpcode == Opcode.INVOKEVIRTUAL || lastOpcode == Opcode.INVOKESPECIAL || lastOpcode == Opcode.INVOKESTATIC || lastOpcode == Opcode.INVOKEINTERFACE) {
                    return BlockType.METHOD_CALL;
                }
                break;

            // Array store instructions
            case Opcode.IASTORE:
            case Opcode.LASTORE:
            case Opcode.FASTORE:
            case Opcode.DASTORE:
            case Opcode.AASTORE:
            case Opcode.BASTORE:
            case Opcode.CASTORE:
            case Opcode.SASTORE:
                return BlockType.ARRAY_STORE;

            case Opcode.INVOKEVIRTUAL:
            case Opcode.INVOKESTATIC:
            case Opcode.INVOKEINTERFACE:
            case Opcode.INVOKEDYNAMIC:
                if(line instanceof MethodLine)
                {
                    MethodLine methodLine = (MethodLine) line;
                    if(methodLine.getType().endsWith(")V"))
                    {
                        return BlockType.VOID_METHOD_CALL;
                    }
                }
                break;

            case Opcode.PUTFIELD:
            case Opcode.PUTSTATIC:
                return BlockType.FIELD_STORE;

            // Other instructions
            default:
                break;
        }

        // The instruction is not the end of a line
        return null;
    }
}
