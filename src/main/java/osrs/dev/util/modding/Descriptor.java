package osrs.dev.util.modding;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Descriptor {
    private final String returnValue;
    private final Map<Integer, String> parameters;

    public Descriptor(String descriptor) {
        String[] parts = descriptor.split("\\)");
        returnValue = parts[1];

        parameters = new HashMap<>();
        String paramSection = parts[0].substring(1); // remove the leading '('

        int index = 0;
        int paramIndex = 0;
        while (index < paramSection.length()) {
            char c = paramSection.charAt(index);
            if (c == 'L') { // Object type
                int semiColonIndex = paramSection.indexOf(';', index);
                parameters.put(paramIndex++, paramSection.substring(index, semiColonIndex + 1));
                index = semiColonIndex + 1;
            } else { // Primitive type
                parameters.put(paramIndex++, String.valueOf(c));
                index++;
            }
        }
    }
}
