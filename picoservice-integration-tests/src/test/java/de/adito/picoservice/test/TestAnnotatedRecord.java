package de.adito.picoservice.test;

/**
 * Verifies that annotated types nested in records can be registered.
 */
@TestAnno(5)
public record TestAnnotatedRecord() implements ITestAnnotated
{
  @TestAnno(6)
  public static class NestedAnnotated implements ITestAnnotated
  {
  }
}
