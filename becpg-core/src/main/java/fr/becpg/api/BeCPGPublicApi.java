package fr.becpg.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to denote a class or method as part
 * of the public API. When a class or method is so designated then
 * we will not change it within a release in a way that would make
 * it no longer backwardly compatible with an earlier version within
 * the release.
 *
 * @author Matthieu Laborie
 * @version $Id: $Id
 */
@Target( {ElementType.TYPE,ElementType.METHOD} )
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeCPGPublicApi {

}
