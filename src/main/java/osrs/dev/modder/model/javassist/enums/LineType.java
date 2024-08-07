package osrs.dev.modder.model.javassist.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LineType {
    OTHER(-1),
    FIELD(0),
    METHOD(1),
    LDC(2);

    @Getter
    private final int id;
}