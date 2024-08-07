package osrs.dev.util.modding;

import javassist.*;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.javassist.CodeBlock;
import osrs.dev.modder.model.javassist.ConstructorDefinition;
import osrs.dev.modder.model.javassist.instructions.FieldLine;
import osrs.dev.modder.model.javassist.instructions.InstructionLine;
import osrs.dev.modder.model.javassist.instructions.ValueLine;

import javax.net.ssl.SSLSession;
import java.applet.Applet;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

public class CodeUtil
{
    /**
     * scans a method for all of its refferances to other methods and
     * throws them into a queue for us to continue scanning and also
     * adds each of them to a set<> for us.
     * @param method method to scan
     */
    public static void scanForMethodRefs(CtMethod method, Collection<CtMethod> methodQueue) {
        MethodInfo methodInfo = method.getMethodInfo();
        ConstPool constPool = methodInfo.getConstPool();
        CtClass mystery;
        CtClass extender;
        try {
            CodeIterator codeIterator = methodInfo.getCodeAttribute().iterator();
            int pos;

            while (codeIterator.hasNext()) {
                pos = codeIterator.next();
                int opcode = codeIterator.byteAt(pos);
                if (opcode != Opcode.INVOKESTATIC && opcode != Opcode.INVOKEVIRTUAL) {
                    continue;
                }

                int ref = codeIterator.u16bitAt(pos + 1);
                String methodRefClassName = constPool.getMethodrefClassName(ref);
                if (methodRefClassName.length() > 2 && !methodRefClassName.equals("client"))
                    continue;

                String methodName = constPool.getMethodrefName(ref);
                String methodDescriptor = constPool.getMethodrefType(ref);
                mystery = Mappings.getClazz(methodRefClassName);

                CtMethod methodRef = null;
                try {
                    methodRef = mystery.getMethod(methodName, methodDescriptor);
                } catch (Exception ex) {
                    continue;
                }

                if (methodRef == null) {
                    continue;
                }

                // Check if the method is abstract and belongs to gameEngine
                if (Modifier.isAbstract(methodRef.getModifiers())) {
                    try {
                        extender = findExtenderOf(mystery);
                        if(extender == null)
                            continue;
                        methodRef = extender.getMethod(methodRef.getName(), methodDescriptor);
                    } catch (NotFoundException e) {
                        continue;
                    }
                }

                if (!Modifier.isAbstract(methodRef.getModifiers())) {
                    methodQueue.add(methodRef);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Checks if the supplied method is from a super class outside
     * the game pack. We use these as starting points for recursively
     * scanning through methods to ensure we don't hit any of the unused methods
     * @param method method
     * @return boolean
     */
    public static boolean fromSuper(CtMethod method)
    {
        return (
                Arrays.stream(Applet.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(Runnable.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(FocusListener.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(WindowListener.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(MouseAdapter.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(MouseListener.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(MouseMotionListener.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(Comparator.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(Callable.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(Iterable.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(Collection.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(KeyListener.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(MouseWheelListener.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(ThreadFactory.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(SSLSession.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName())) ||
                Arrays.stream(Iterator.class.getDeclaredMethods()).anyMatch(m -> m.getName().equals(method.getName()))
        );
    }

    /**
     * for when we come across an abstract method in our recursive scan, this method is used to
     * find the implementation of that method or rather the class with its implementation.
     * @param clazz class with abstract method
     * @return class implementing or extending supplied class
     */
    public static CtClass findExtenderOf(CtClass clazz)
    {
        return Mappings.getClasses().stream()
                .filter(c -> extendsOrImplements(c, clazz))
                .findFirst()
                .orElse(null);
    }

    /**
     * checks if the first class extends or impliments the 2nd class
     * @param classToCheck classToCheck
     * @param classOrInterface classOrInterface
     * @return boolean
     */
    public static boolean extendsOrImplements(CtClass classToCheck, CtClass classOrInterface) {
        try {
            if (classToCheck.getName().equals(classOrInterface.getName())) {
                return false;
            }

            CtClass currentClass = classToCheck;
            while (currentClass != null) {
                if (currentClass.getName().equals(classOrInterface.getName())) {
                    return true;
                }
                currentClass = currentClass.getSuperclass();
            }

            CtClass[] interfaces = classToCheck.getInterfaces();
            for (CtClass iface : interfaces) {
                if (iface.getName().equals(classOrInterface.getName())) {
                    return true;
                }
            }
        } catch (NotFoundException ignored) {
        }
        return false;
    }

    public static <T> T inspectFieldInit(boolean isStatic, String fieldClassName, String fieldName) {
        CtClass clazz = Mappings.getClazz(fieldClassName);
        ConstructorDefinition definition;
        CtConstructor classInitializer;
        if(isStatic)
        {
            classInitializer = clazz.getClassInitializer();
            definition = new ConstructorDefinition(classInitializer);
            for(CodeBlock block : definition.getBody())
            {
                if(!block.contains(Opcode.PUTSTATIC))
                    continue;

                InstructionLine line;
                for(int i = 0; i < block.getInstructions().size(); i++)
                {
                    if(i == 0)
                        continue;

                    line = block.getInstructions().get(i);
                    if(!(line instanceof FieldLine))
                        continue;

                    FieldLine fieldLine = line.transpose();
                    if(!fieldLine.getName().equals(fieldName) || !fieldLine.getClazz().equals(fieldClassName))
                        continue;

                    line = block.getInstructions().get(i - 1);
                    if(!(line instanceof ValueLine))
                        continue;

                    ValueLine valueLine = line.transpose();
                    if(valueLine.getValue() == null)
                        continue;

                    return valueLine.getValue();
                }
            }
        }
        else
        {
            for (CtConstructor constructor : clazz.getDeclaredConstructors())
            {
                definition = new ConstructorDefinition(constructor);
                for(CodeBlock block : definition.getBody())
                {
                    if(!block.contains(Opcode.PUTFIELD))
                        continue;

                    InstructionLine line;
                    for(int i = 0; i < block.getInstructions().size(); i++)
                    {
                        if(i == 0)
                            continue;

                        line = block.getInstructions().get(i);
                        if(!(line instanceof FieldLine))
                            continue;

                        FieldLine fieldLine = line.transpose();
                        if(!fieldLine.getName().equals(fieldName) || !fieldLine.getClazz().equals(fieldClassName))
                            continue;

                        line = block.getInstructions().get(i - 1);
                        if(!(line instanceof ValueLine))
                            continue;

                        ValueLine valueLine = line.transpose();
                        if(valueLine.getValue() == null)
                            continue;

                        return valueLine.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}
