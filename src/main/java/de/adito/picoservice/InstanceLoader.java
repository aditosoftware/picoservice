package de.adito.picoservice;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Default implementation for {@link de.adito.picoservice.IPicoRegistry} available at
 * {@link de.adito.picoservice.IPicoRegistry#INSTANCE}.<br>
 * The implementation can be replaced by providing a service for {@link de.adito.picoservice.IPicoRegistry} using
 * java's {@link java.util.ServiceLoader}.
 *
 * @author j.boesl, 25.03.15
 */
class InstanceLoader
{
  IPicoRegistry load()
  {
    // try to load as service
    ServiceLoader<IPicoRegistry> serviceLoader = ServiceLoader.load(IPicoRegistry.class);
    Iterator<IPicoRegistry> iterator = serviceLoader.iterator();
    if (iterator.hasNext())
      return iterator.next();

    // create default
    return new IPicoRegistry()
    {
      private final ServiceLoader<IPicoRegistration> serviceLoader = ServiceLoader.load(IPicoRegistration.class);
      private final Collection<IPicoRegistration> loadedServices = _loadServices();

      @Nonnull
      @Override
      public <C, A extends Annotation> Map<Class<? extends C>, A> find(@Nonnull Class<C> pSearchedType,
                                                                       @Nonnull Class<A> pAnnotationClass)
      {
        Map<Class<? extends C>, A> map = new HashMap<>();
        for (IPicoRegistration registration : loadedServices)
        {
          Class<?> annotatedClass = registration.getAnnotatedClass();
          if (pSearchedType.isAssignableFrom(annotatedClass))
          {
            Annotation annotation = annotatedClass.getAnnotation(pAnnotationClass);
            if (annotation != null)
              //noinspection unchecked
              map.put((Class<C>) annotatedClass, (A) annotation);
          }
        }
        return map;
      }

      @Nonnull
      @Override
      public <T, C> Stream<T> find(@Nonnull Class<C> pSearchedType,
                                   @Nonnull Function<Class<? extends C>, T> pResolverFunction)
      {
        Stream.Builder<T> streamBuilder = Stream.builder();
        for (IPicoRegistration registration : loadedServices)
        {
          Class<?> annotatedClass = registration.getAnnotatedClass();
          if (pSearchedType.isAssignableFrom(annotatedClass))
          {
            @SuppressWarnings("unchecked")
            T result = pResolverFunction.apply((Class<? extends C>) annotatedClass);
            if (result != null)
              streamBuilder.add(result);
          }
        }
        return streamBuilder.build();
      }

      /**
       * We have to load all of our PicoServices into a separate collection, because
       * the Java-ServiceLoader throws a ConcurrentModificationException if >1 iterators
       * are iterated at the same time.
       * Reloading of cached Registrations is currently not supported yet.
       *
       * @see <a href="https://anydoby.com/jblog/en/java/2128">https://anydoby.com/jblog/en/java/2128</a>
       * @see <a href="https://issues.apache.org/jira/browse/SIS-193">https://issues.apache.org/jira/browse/SIS-193</a>
       */
      @Nonnull
      private Collection<IPicoRegistration> _loadServices()
      {
        synchronized (serviceLoader)
        {
          HashSet<IPicoRegistration> foundServices = new HashSet<>();
          for (IPicoRegistration registration : serviceLoader)
            foundServices.add(registration);
          return Collections.unmodifiableSet(foundServices);
        }
      }
    };
  }
}
