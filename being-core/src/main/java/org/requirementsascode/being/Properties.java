package org.requirementsascode.being;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the class for properties based serialization,
 * instead of relying on getters and setters.
 * 
 * @author b_muth
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {
}