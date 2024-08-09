package osrs.dev.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SignatureUtil
{
    private static final Map<Class<?>, String> typeDescriptorMap = new HashMap<>();

    static {
        typeDescriptorMap.put(void.class, "V");
        typeDescriptorMap.put(boolean.class, "Z");
        typeDescriptorMap.put(byte.class, "B");
        typeDescriptorMap.put(char.class, "C");
        typeDescriptorMap.put(short.class, "S");
        typeDescriptorMap.put(int.class, "I");
        typeDescriptorMap.put(long.class, "J");
        typeDescriptorMap.put(float.class, "F");
        typeDescriptorMap.put(double.class, "D");
    }

    /**
     * Returns the JVM internal method signature for a given method.
     *
     * @param method The method for which the signature is to be generated.
     * @return A string representing the JVM internal method signature.
     */
    public static String getMethodSignature(Method method) {
        StringBuilder signature = new StringBuilder();

        // Start with the parameter types
        signature.append("(");
        for (Class<?> paramType : method.getParameterTypes()) {
            signature.append(getTypeDescriptor(paramType));
        }
        signature.append(")");

        // Append the return type
        signature.append(getTypeDescriptor(method.getReturnType()));

        return signature.toString();
    }

    /**
     * Returns the JVM internal type descriptor for a given class.
     *
     * @param clazz The class for which the type descriptor is to be generated.
     * @return A string representing the JVM internal type descriptor.
     */
    private static String getTypeDescriptor(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return typeDescriptorMap.get(clazz);
        } else if (clazz.isArray()) {
            return clazz.getName().replace('.', '/');
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
    }
}
