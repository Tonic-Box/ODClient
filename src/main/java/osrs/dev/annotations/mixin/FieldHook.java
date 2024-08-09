package osrs.dev.annotations.mixin;

public @interface FieldHook {
    String value();
    boolean after() default false;
}
