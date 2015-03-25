package de.adito.picoservice.test;

import de.adito.picoservice.PicoService;

import java.lang.annotation.*;

/**
 * @author j.boesl, 24.03.15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@PicoService
public @interface TestAnno
{
  int value();
}
