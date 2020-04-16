package pt.lsts.imc4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Periodic {
	/**
	 * @return Running periodicity, in milliseconds
	 */
    int value() default 1000;
}