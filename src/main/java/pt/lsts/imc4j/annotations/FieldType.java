package pt.lsts.imc4j.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface FieldType {
	IMCField type();
	double min() default Double.NaN;
	double max() default Double.NaN;
	String units() default "";
}
