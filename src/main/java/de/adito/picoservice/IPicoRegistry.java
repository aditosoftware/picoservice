package de.adito.picoservice;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author j.boesl, 25.03.15
 */
public interface IPicoRegistry
{

  IPicoRegistry INSTANCE = new InstanceLoader().load();

  <C, A extends Annotation> Map<Class<? extends C>, A> find(Class<C> pSearchedType, Class<A> pAnnotationClass);

}
