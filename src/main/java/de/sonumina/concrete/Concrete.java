package de.sonumina.concrete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to annotate generic class to generate
 * code with a concrete datatype. This makes especially sense for native
 * datatypes if you want to avoid boxing. Note that this is currently
 * works only for classes with a single type parameter and it works
 * only for one single concrete type as we don't use Java8 for now.
 *
 * @author Sebastian Bauer
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value={ElementType.TYPE})
public @interface Concrete
{
	/**
	 * Define the name of the generated class.
	 */
	String name();

	/**
	 * Define the actual type realization.
	 */
	String type();
}
