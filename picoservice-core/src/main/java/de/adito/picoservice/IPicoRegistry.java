package de.adito.picoservice;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This interface defines a way to lookup service classes. A default instance can be accessed at
 * {@link de.adito.picoservice.IPicoRegistry#INSTANCE}.
 *
 * @author j.boesl, 25.03.15
 */
public interface IPicoRegistry
{

  /**
   * Default static instance for looking up services.
   */
  IPicoRegistry INSTANCE = new InstanceLoader().load();

  /**
   * Finds all services of given type annotated with given annotation.
   *
   * @param pSearchedType    services of this type are searched.
   * @param pAnnotationClass the class has to be annotated with this annotation.
   * @param <C>              the searched type.
   * @param <A>              the annotation type which must be available.
   * @return a map with the service classes as keys and the searched annotations as values.
   */
  @Nonnull
  <C, A extends Annotation> Map<Class<? extends C>, A> find(@Nonnull Class<C> pSearchedType, @Nonnull Class<A> pAnnotationClass);

  /**
   * Finds all services of given type transformed by given function.
   *
   * @param pSearchedType     services of this type are searched.
   * @param pResolverFunction function for transforming resolved service classes. Returns <tt>nul</tt> to filter a result.
   * @param <T>               target transformation type.
   * @param <C>               the searched type.
   * @return a stream of transformation results.
   */
  @Nonnull
  <T, C> Stream<T> find(@Nonnull Class<C> pSearchedType, @Nonnull Function<Class<? extends C>, T> pResolverFunction);

}
