package de.adito.picoservice.test;

/**
 * @author j.boesl, 24.03.15
 */
@TestAnno(1)
public class TestAnnotated1 implements ITestAnnotated
{

  @TestAnno(3)
  public enum InnerTest2 implements ITestAnnotated
  {
  }

  public interface InnerInterface
  {
    public enum InnerEnum
    {
      ;

      @TestAnno(4)
      public enum InnerTest1 implements ITestAnnotated
      {
      }
    }
  }

}
