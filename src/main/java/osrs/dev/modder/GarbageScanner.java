package osrs.dev.modder;

import javassist.*;
import javassist.bytecode.*;
import osrs.dev.modder.model.GarbageValue;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import java.util.*;

public class GarbageScanner
{

    /**
     * scan the gamepack and extract the garbage values for gamepack elements we map
     */
    public static void scan()
    {
        for(CtClass clazz : Mappings.getClasses())
        {
            for(CtMethod method : clazz.getDeclaredMethods())
            {
                if(!Mappings.getUsedMethods().contains(method.getLongName()))
                    continue;

                scanForMethodGarbs(method);

                if(isDone())
                    return;
            }
        }
    }

    /**
     * Scans a CtMethod for any references to another method. This is
     * namely used by the mapper to assist in finding methods garbage
     * values and types.
     * @param method method to scan
     */
    private static void scanForMethodGarbs(CtMethod method) {
        MethodInfo methodInfo = method.getMethodInfo();
        ConstPool constPool = methodInfo.getConstPool();
        CtClass mystery;
        CtClass extender;
        List<Integer> previousPositions = new LinkedList<>();
        try {
            CodeIterator codeIterator = methodInfo.getCodeAttribute().iterator();
            int pos;

            while (codeIterator.hasNext()) {
                pos = codeIterator.next();
                int opcode = codeIterator.byteAt(pos);
                if (opcode != Opcode.INVOKESTATIC && opcode != Opcode.INVOKEVIRTUAL) {
                    previousPositions.add(pos);
                    continue;
                }

                int ref = codeIterator.u16bitAt(pos + 1);
                String methodRefClassName = constPool.getMethodrefClassName(ref);
                if (methodRefClassName.length() > 2 && !methodRefClassName.equals("client"))
                    continue;

                String methodName = constPool.getMethodrefName(ref);
                String methodDescriptor = constPool.getMethodrefType(ref);
                mystery = Mappings.getClazz(methodRefClassName);

                Mapping mapping = findMapping(mystery, methodName, methodDescriptor);
                if (mapping == null || mapping.isDone()) {
                    previousPositions.add(pos);
                    continue;
                }

                int lastPosition = previousPositions.get(previousPositions.size() - 1);
                GarbageValue loaded = findLoadedValue(codeIterator, constPool, lastPosition);

                // found it
                assert loaded != null;
                System.out.println("[ScannedMethodGarb] M=" + mapping.getName() + " / v(" + loaded.getType() + ")=" + loaded.getValue() + " / Src: " + method.getLongName());
                mapping.setGarbage(loaded);
                mapping.setDone(true);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Extracts the garbage value type and value from the supplied position in the
     * methodcode block
     * @param iterator iterator
     * @param constPool constant pool
     * @param pos current position
     * @return garbage value
     */
    private static GarbageValue findLoadedValue(CodeIterator iterator, ConstPool constPool, int pos) {
        if (!iterator.hasNext()) {
            return null;
        } else {
            int opcode = iterator.byteAt(pos);
            int value;
            switch (opcode) {
                case Opcode.ICONST_0:
                    return new GarbageValue('I', 0);
                case Opcode.ICONST_1:
                    return new GarbageValue('I', 1);
                case Opcode.ICONST_2:
                    return new GarbageValue('I', 2);
                case Opcode.ICONST_3:
                    return new GarbageValue('I', 3);
                case Opcode.ICONST_4:
                    return new GarbageValue('I', 4);
                case Opcode.ICONST_5:
                    return new GarbageValue('I', 5);
                case Opcode.BIPUSH:
                    return new GarbageValue('B', iterator.signedByteAt(pos + 1));

                case Opcode.SIPUSH:
                    return new GarbageValue('S', iterator.s16bitAt(pos + 1));

                case Opcode.LDC:
                    value = iterator.byteAt(pos + 1);
                    if (constPool.getTag(value) != 3) {
                        return null;
                    }
                    return new GarbageValue('I', constPool.getIntegerInfo(value));

                case Opcode.LDC_W:
                    value = iterator.u16bitAt(pos + 1);
                    if (constPool.getTag(value) != 3) {
                        return null;
                    }
                    return new GarbageValue('I', constPool.getIntegerInfo(value));

                case Opcode.LDC2_W:
                    value = iterator.u16bitAt(pos + 1);
                    if (constPool.getTag(value) != 5) {
                        return null;
                    }
                    return new GarbageValue('J', constPool.getLongInfo(value));

                default:
                    return null;
            }
        }
    }

    /**
     * searches our mappings for any canidates for use as we scan method code bodies
     * and come accross method calls to know if we've located a garbage value we still need to extract.
     * @param mystery the class
     * @param name method name
     * @param descriptor method descriptor
     * @return Mapping
     */
    private static Mapping findMapping(CtClass mystery, String name, String descriptor)
    {
        return Mappings.getMappings().stream()
                .filter(m -> {

                    if(m.isDone() || m.getGarbage() != null)
                        return false;

                    if(!m.getObfuscatedName().equals(name))
                        return false;

                    if(!m.getDescriptor().equals(descriptor))
                        return false;

                    CtClass clazz = Mappings.getClazz(m.getObfuscatedClass());
                    return mystery != null && mystery.subclassOf(clazz);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * check for if we're done finding all our garbage mappings
     * to save time scanning when we don't need to
     * @return boolean
     */
    private static boolean isDone()
    {
        return Mappings.getMappings()
                .stream()
                .allMatch(Mapping::isDone);
    }
}
