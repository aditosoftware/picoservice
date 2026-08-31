# picoservice

Picoservice is a java library for service registration and service lookup. With it, you can find classes that have special meaning in your project. Internally it uses java's `ServiceLoader` so it integrates nicely with you build tools. Nothing magical is happening here.

Picoservice is focused on its main purpose: registration and lookup. For registration, you have to annotate a custom annotation with `@PicoService`. Each class annotated with that custom annotation can afterwards be found by using `IPicoRegistry.INSTANCE.find(Class<C> pSearchedType, Class<A> pAnnotationClass)`.

Get started
-----------

Picoservice 2 separates the runtime library from its annotation processor. Applications need
`picoservice-core` at compile time and runtime. Builds additionally configure
`picoservice-processor` as an annotation processor; it is not a runtime dependency.

```xml
<dependency>
  <groupId>de.adito.picoservice</groupId>
  <artifactId>picoservice-core</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

Configure the processor with the Maven Compiler Plugin:

```xml
<build>
  <plugins>
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <proc>full</proc>
        <annotationProcessorPaths>
          <path>
            <groupId>de.adito.picoservice</groupId>
            <artifactId>picoservice-processor</artifactId>
            <version>2.0.0-SNAPSHOT</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

Migrating from 1.x
------------------

Replace the former `de.adito.picoservice:picoservice` dependency with
`de.adito.picoservice:picoservice-core` and add the compiler-plugin configuration above. The
Java packages and APIs remain the same; the required change is Maven dependency and processor
configuration. Do not add `picoservice-processor` as a normal application dependency: it is only
needed while compiling.

Example of usage
----------------

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
