package de.adito.picoservice;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author j.boesl, 25.03.15
 */
public interface IPicoRegistry
{

  IPicoRegistry INSTANCE = new IPicoRegistry()
  {
    @Override
    public <C, A extends Annotation> Map<Class<? extends C>, A> find(Class<C> pSearchedType, Class<A> pAnnotationClass)
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

  <C, A extends Annotation> Map<Class<? extends C>, A> find(Class<C> pSearchedType, Class<A> pAnnotationClass);

}
