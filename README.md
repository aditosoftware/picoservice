# picoservice

Picoservice is a java library for service registration and service lookup. With it, you can find classes that have special meaning in your project.
Internally it uses java's `ServiceLoader` so it integrates nicely with you build tools. Nothing magical is happening here.

Picoservice is focused on its main purpose: registration and lookup. For registration, you have to annotate a custom annotation with `@PicoService`.
Each class annotated with that custom annotation can afterwards be found by using
`IPicoRegistry.INSTANCE.find(Class<C> pSearchedType, Class<A> pAnnotationClass)`.

Get started
-----------

Picoservice 2 separates the runtime library from its annotation processor. Applications need
`picoservice-core` at compile time and runtime. Builds additionally configure
`picoservice-processor` as an annotation processor; it is not a runtime dependency.

For compatibility with the 1.x single-artifact setup, `picoservice` is also available. It bundles
both modules and retains the annotation processor's standard auto-discovery. New applications
should prefer the separate modules and explicit processor configuration below.

```xml
<dependency>
  <groupId>de.adito.picoservice</groupId>
  <artifactId>picoservice-core</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### Compatibility artifact

Existing applications can continue to use the original artifact name. Enable annotation processing
in the build as shown in [auto-discovery](#auto-discovery):

```xml
<dependency>
  <groupId>de.adito.picoservice</groupId>
  <artifactId>picoservice</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### Configure the annotation processor

For Maven 3.x, configure the processor explicitly with the Maven Compiler Plugin. This is the
[recommended approach](https://maven.apache.org/components/plugins/maven-compiler-plugin-4.x/examples/annotation-processor.html)
because it limits compilation to the processors that the build declares:

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

### Auto-discovery

`picoservice-processor` also supports Java's standard annotation-processor auto-discovery.
Add it to the compile class path with `provided` scope and enable annotation processing:

```xml
<dependency>
  <groupId>de.adito.picoservice</groupId>
  <artifactId>picoservice-processor</artifactId>
  <version>2.0.0-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>

<build>
  <plugins>
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <proc>full</proc>
      </configuration>
    </plugin>
  </plugins>
</build>
```

This lets `javac` find Picoservice and any other processors already on the compile class path.
It is convenient for existing projects, but explicit processor configuration is preferred: auto-discovery
can run processors introduced unintentionally by a dependency. Since JDK 23, annotation processing must
be enabled explicitly; see the [javac documentation](https://docs.oracle.com/en/java/javase/25/docs/specs/man/javac.html).

Migrating from 1.x
------------------

The `de.adito.picoservice:picoservice` dependency continues to work through the compatibility
artifact; enable annotation processing as described in [auto-discovery](#auto-discovery). To migrate
to the split setup, replace it with `de.adito.picoservice:picoservice-core` and configure the
processor using the [explicit configuration](#configure-the-annotation-processor) above. The Java
packages and APIs remain the same. Do not add `picoservice-processor` as a normal application
dependency: it is only needed while compiling.

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
