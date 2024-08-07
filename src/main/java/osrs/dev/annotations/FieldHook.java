package osrs.dev.annotations;

public @interface FieldHook {
    String value();
    boolean after() default false;
}
