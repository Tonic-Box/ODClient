package osrs.dev.annotations;

public @interface Shadow {
    String value();
    boolean method() default false;
}
