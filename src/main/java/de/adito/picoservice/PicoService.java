package de.adito.picoservice;

import java.lang.annotation.*;

/**
 * Marker annotation for service annotations. Every class that is annotated with a service annotation can be found with
 * picoservice.<br>
 * The registration of the services is done at compile time by {@link de.adito.picoservice.processor.AnnotationProcessorPico}.
 * For lookup {@link de.adito.picoservice.IPicoRegistry} is used. A default implementation is available at
 * {@link de.adito.picoservice.IPicoRegistry#INSTANCE}. Alternatively lookup can be done with java's
 * {@link java.util.ServiceLoader} by loading {@link de.adito.picoservice.IPicoRegistration}. The default implementation
 * of <tt>IPicoRegistry</tt> uses <tt>ServiceLoader</tt> internally, too.
 *
 * @author j.boesl, 23.03.15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface PicoService
{
}
