package lumi.insert.app.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;

/**
 * Annotation that used as logger after a function finished.
 * Only can be placed to a method    
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityLogger {

    /**
     *  Entity name in database, example: employees NOT Employee.
     */
    String entityName();

    /** 
     * ActivityAction that will assigned to {@link ActivityLog#setAction(ActivityAction)}
     */
    ActivityAction action();

    /** 
     * Message that will assigned to {@link ActivityLog#setActionMessage(String)}
     */
    String actionMessage(); 

    /** 
     * Set to true if Method have only one param which is entity ID.
     */
    boolean entityIdFromSingleParam() default false;
}
