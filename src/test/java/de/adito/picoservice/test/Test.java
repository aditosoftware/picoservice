package de.adito.picoservice.test;

import de.adito.picoservice.IPicoRegistry;
import org.junit.Assert;

import java.util.Map;

/**
 * @author j.boesl, 24.03.15
 */
public class Test
{

  @org.junit.Test
  public void checkRegistration()
  {
    Map<Class<? extends ITestAnnotated>, TestAnno> map = IPicoRegistry.INSTANCE.find(ITestAnnotated.class, TestAnno.class);
    Assert.assertEquals(3, map.size());
    Assert.assertEquals(TestAnno.class, map.get(TestAnnotated1.class).annotationType());
    Assert.assertEquals(1, map.get(TestAnnotated1.class).value());
    Assert.assertEquals(TestAnno.class, map.get(TestAnnotated2.class).annotationType());
    Assert.assertEquals(2, map.get(TestAnnotated2.class).value());
    Assert.assertEquals(TestAnno.class, map.get(TestAnnotated1.InnerInterface.InnerEnum.InnerTest1.class).annotationType());
    Assert.assertEquals(3, map.get(TestAnnotated1.InnerInterface.InnerEnum.InnerTest1.class).value());
  }

}
