package de.adito.picoservice.test;

import de.adito.picoservice.IPicoRegistry;
import org.junit.jupiter.api.*;

import java.util.Map;

/**
 * @author j.boesl, 24.03.15
 */
class TestAnnoTest
{
  private Map<Class<? extends ITestAnnotated>, TestAnno> testAnnotatedInstances;

  @BeforeEach
  void setUp()
  {
    // act
    testAnnotatedInstances = IPicoRegistry.INSTANCE.find(ITestAnnotated.class, TestAnno.class);
  }

  @Test
  void shouldFindAllInstances()
  {
    Assertions.assertEquals(6, testAnnotatedInstances.size());
  }

  @Test
  void shouldFindClassAnnotations()
  {
    Assertions.assertEquals(TestAnno.class, testAnnotatedInstances.get(TestAnnotated1.class).annotationType());
    Assertions.assertEquals(1, testAnnotatedInstances.get(TestAnnotated1.class).value());

    Assertions.assertEquals(TestAnno.class, testAnnotatedInstances.get(TestAnnotated2.class).annotationType());
    Assertions.assertEquals(2, testAnnotatedInstances.get(TestAnnotated2.class).value());
  }

  @Test
  void shouldFindEnumAnnotations()
  {
    Assertions.assertEquals(TestAnno.class, testAnnotatedInstances.get(TestAnnotated1.InnerTest2.class).annotationType());
    Assertions.assertEquals(3, testAnnotatedInstances.get(TestAnnotated1.InnerTest2.class).value());
  }

  @Test
  void shouldFindNestedClassInEnumAnnotations()
  {
    Assertions.assertEquals(TestAnno.class, testAnnotatedInstances.get(TestAnnotated1.InnerInterface.InnerEnum.InnerTest1.class).annotationType());
    Assertions.assertEquals(4, testAnnotatedInstances.get(TestAnnotated1.InnerInterface.InnerEnum.InnerTest1.class).value());
  }

  @Test
  void shouldFindRecordAnnotations()
  {
    Assertions.assertEquals(TestAnno.class, testAnnotatedInstances.get(TestAnnotatedRecord.class).annotationType());
    Assertions.assertEquals(5, testAnnotatedInstances.get(TestAnnotatedRecord.class).value());
  }

  @Test
  void shouldFindNestedClassInRecordAnnotations()
  {
    Assertions.assertEquals(TestAnno.class, testAnnotatedInstances.get(TestAnnotatedRecord.NestedAnnotated.class).annotationType());
    Assertions.assertEquals(6, testAnnotatedInstances.get(TestAnnotatedRecord.NestedAnnotated.class).value());
  }
}
