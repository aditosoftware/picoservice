# picoservice
[![Build Status](https://travis-ci.org/jboesl/picoservice.svg?branch=master)](https://travis-ci.org/jboesl/picoservice)


Picoservice is a java library for service registration and service lookup. With it you can find classes that have special meaning in your project. Internally it uses java's `ServiceLoader` so it integrates nicely with you build tools. Nothing magic is happening here.

Picoservice is focused on it's main purpose: registration and lookup. For registration you have to annotate a custom annotation with `@PicoService`. Each class annotated with that custom annotation can afterwards be found by using `IPicoRegistry.INSTANCE.find(Class<C> pSearchedType, Class<A> pAnnotationClass)`.


Example of usage:
-----------------

Custom annotation which is put on classes for registration:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@PicoService
public @interface TestAnno
{
  int value();
}
```

Annotated class 1:
```java
@TestAnno(10)
public class TestAnnotated
{
}
```

Annotated class 2:
```java
@TestAnno(20)
public class TestAnnotated2
{
}
```

Find those classes:
```java
public class Test
{
  public static void main(String[] args)
  {
    Map<Class<?>, TestAnno> map = IPicoRegistry.INSTANCE.find(Object.class, TestAnno.class);
  }
}
```