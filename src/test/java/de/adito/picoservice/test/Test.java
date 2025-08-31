package de.adito.picoservice.test;

import de.adito.picoservice.IPicoRegistry;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

/**
 * @author j.boesl, 24.03.15
 */
public class Test
{

  @org.junit.jupiter.api.Test
  public void checkRegistration()
  {
    Map<Class<? extends ITestAnnotated>, TestAnno> map = IPicoRegistry.INSTANCE.find(ITestAnnotated.class, TestAnno.class);
    Assertions.assertEquals(3, map.size());
    Assertions.assertEquals(TestAnno.class, map.get(TestAnnotated1.class).annotationType());
    Assertions.assertEquals(1, map.get(TestAnnotated1.class).value());
    Assertions.assertEquals(TestAnno.class, map.get(TestAnnotated2.class).annotationType());
    Assertions.assertEquals(2, map.get(TestAnnotated2.class).value());
    Assertions.assertEquals(TestAnno.class, map.get(TestAnnotated1.InnerInterface.InnerEnum.InnerTest1.class).annotationType());
    Assertions.assertEquals(3, map.get(TestAnnotated1.InnerInterface.InnerEnum.InnerTest1.class).value());
  }

}
