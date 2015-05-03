package de.adito.picoservice;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Default implementation for {@link de.adito.picoservice.IPicoRegistry} available at
 * {@link de.adito.picoservice.IPicoRegistry#INSTANCE}.<br/>
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
      @Nonnull
      @Override
      public <C, A extends Annotation> Map<Class<? extends C>, A> find(@Nonnull Class<C> pSearchedType,
                                                                       @Nonnull Class<A> pAnnotationClass)
      {
        Map<Class<? extends C>, A> map = new HashMap<Class<? extends C>, A>();
        ServiceLoader<IPicoRegistration> serviceLoader = ServiceLoader.load(IPicoRegistration.class);
        for (IPicoRegistration registration : serviceLoader)
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
    };
  }
}
