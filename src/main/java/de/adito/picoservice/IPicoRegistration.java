package de.adito.picoservice;

import javax.annotation.Nonnull;

/**
 * @author j.boesl, 23.03.15
 */
public interface IPicoRegistration
{
  @Nonnull
  Class<?> getAnnotatedClass();
}
