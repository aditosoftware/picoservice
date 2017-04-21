package de.adito.picoservice;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author j.boesl, 21.04.17
 */
public class DefaultPicoRegistry implements IPicoRegistry
{

  private final Collection<IPicoRegistration> loadedServices;
  private final Map<Class<?>, Collection<Class<?>>> searchedTypeToAnnotatedClassesMap;


  protected DefaultPicoRegistry()
  {
    loadedServices = _loadServices();
    searchedTypeToAnnotatedClassesMap = new HashMap<>();
  }

  @Nonnull
  @Override
  public <C, A extends Annotation> Map<Class<? extends C>, A> find(@Nonnull Class<C> pSearchedType,
                                                                   @Nonnull Class<A> pAnnotationClass)
  {
    Map<Class<? extends C>, A> map = new HashMap<>();
    for (Class<? extends C> cls : _getSearchedTypes(pSearchedType)) {
      A annotation = cls.getAnnotation(pAnnotationClass);
      if (annotation != null)
        map.put(cls, annotation);
    }
    return map;
  }

  @Nonnull
  @Override
  public <T, C> Stream<T> find(@Nonnull Class<C> pSearchedType,
                               @Nonnull Function<Class<? extends C>, T> pResolverFunction)
  {
    Collection<Class<? extends C>> searchedTypes = _getSearchedTypes(pSearchedType);
    return searchedTypes.stream()
        .map(pResolverFunction)
        .filter(Objects::nonNull);
  }

  @SuppressWarnings("unchecked")
  private <T> Collection<Class<? extends T>> _getSearchedTypes(Class<T> pSearchedType)
  {
    return (Collection) searchedTypeToAnnotatedClassesMap.computeIfAbsent(pSearchedType, searchedType -> {
      ArrayList<Class<? extends T>> st = new ArrayList<>();
      for (IPicoRegistration registration : loadedServices) {
        Class<?> annotatedClass = registration.getAnnotatedClass();
        if (searchedType.isAssignableFrom(annotatedClass))
          st.add((Class<? extends T>) annotatedClass);
      }
      if (st.isEmpty())
        return Collections.emptySet();
      st.trimToSize();
      return Collections.unmodifiableCollection(st);
    });
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
    ServiceLoader<IPicoRegistration> serviceLoader = ServiceLoader.load(IPicoRegistration.class);
    Set<IPicoRegistration> foundServices = new HashSet<>();
    for (IPicoRegistration registration : serviceLoader)
      foundServices.add(registration);
    return Collections.unmodifiableSet(foundServices);
  }

}
