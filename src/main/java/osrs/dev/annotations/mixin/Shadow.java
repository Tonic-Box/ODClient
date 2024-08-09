package osrs.dev.annotations.mixin;

public @interface Shadow {
    String value();
    boolean method() default false;
}
