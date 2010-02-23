package org.sonatype.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly mark which type is implemented by this class. Needed, when the class hieararchy makes it
 * impossible to deduce which type is implemented.
 * 
 * @author cstamas
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface Typed
{
    /**
     * List the types that this implementation implements.
     * 
     * @return
     */
    Class<?>[] value() default {};
}
