package de.adito.picoservice;

import java.util.*;

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
    return new DefaultPicoRegistry();
  }
}
