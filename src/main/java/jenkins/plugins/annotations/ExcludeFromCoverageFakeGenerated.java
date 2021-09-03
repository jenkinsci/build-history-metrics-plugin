/**
 * ***************************************************************************\
 * This annotation allows methods to be excluded from Coverage Calculations by
 * pretending they are generated.
 *
 * <p>Main use is in static utility classes where the private constructor should
 * not be called so if not excluded would cause coverage to drop below 100% even
 * if everything else was covered.
 *
 * @author nick
 */
package jenkins.plugins.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** @author nick */
@Documented
@Retention(RUNTIME)
@Target({CONSTRUCTOR, TYPE, METHOD})
public @interface ExcludeFromCoverageFakeGenerated {
  /*NOP*/
}
