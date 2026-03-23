package lumi.insert.app.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lumi.insert.app.core.entity.nondatabase.ActivityAction;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityLogger {
    String entityName();

    ActivityAction action();

    String actionMessage(); 

    boolean entityIdFromSingleParam() default false;
}
