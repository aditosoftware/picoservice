# picoservice

Picoservice is a Java library for service registration and service lookup. With it, you can find classes that have special meaning in your project.
Internally it uses Java's `ServiceLoader` so it integrates nicely with your build tools. Nothing magical is happening here.

Picoservice is focused on its main purpose: registration and lookup. For registration, you have to annotate a custom annotation with `@PicoService`.
Each class annotated with that custom annotation can afterwards be found by using
`IPicoRegistry.INSTANCE.find(Class<C> pSearchedType, Class<A> pAnnotationClass)`.

## Building the project

The runtime and processor are compiled for Java 8. 
The integration tests compile with Java 25 to verify annotation-processor auto-discovery on current JDKs, so a full reactor build requires a JDK 25 Maven toolchain.

The `release` profile uses the same JDK 25 toolchain to create Javadocs.

Configure JDK 25 in your Maven `toolchains.xml`.

Example: `~/.m2/toolchains.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>25</version>
    </provides>
    <configuration>
      <jdkHome>/path/to/jdk-25</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

## Get started

As of version 1.2.0 of the Picoservice the runtime library is separated from its annotation processor. Applications need `picoservice-core` at compile time and runtime. 
Builds additionally configure `picoservice-processor` as an annotation processor; it is not a runtime dependency.

For compatibility reasons, a single-artifact setup `picoservice` is also available. It bundles both modules and retains the annotation processor's standard auto-discovery. 
New applications should prefer the separate modules and explicit processor configuration below.

```xml
<dependency>
  <groupId>de.adito.picoservice</groupId>
  <artifactId>picoservice-core</artifactId>
  <version>1.2.0</version>
</dependency>
```

### Compatibility artifact

Existing applications can continue to use the original artifact name. Enable annotation processing
in the build as shown in [auto-discovery](#auto-discovery):

```xml
<dependency>
  <groupId>de.adito.picoservice</groupId>
  <artifactId>picoservice</artifactId>
  <version>1.2.0</version>
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
            <version>1.2.0</version>
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
  <version>1.2.0</version>
  <scope>provided</scope>
</dependency>

<build>
  <plugins>
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.16.0</version>
      <configuration>
        <proc>full</proc>
      </configuration>
    </plugin>
  </plugins>
</build>
```

This lets `javac` find Picoservice and any other processors already on the compile class path.
It is convenient for existing projects, but explicit processor configuration is preferred: auto-discovery
can run processors introduced unintentionally by a dependency. 

Since JDK 23, annotation processing must be enabled explicitly; see the [javac documentation](https://docs.oracle.com/en/java/javase/25/docs/specs/man/javac.html).

## Example of usage

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
    var map = IPicoRegistry.INSTANCE.find(Object.class, TestAnno.class);
  }
}
```

## Migrating from 1.1.x to 1.2.0

This is completely **optional** - the `de.adito.picoservice:picoservice` dependency continues to work through the compatibility artifact.

To migrate to the split setup, replace it with
`de.adito.picoservice:picoservice-core` and configure the
processor using the [explicit configuration](#configure-the-annotation-processor) above. The Java
packages and APIs remain the same. Do not add `picoservice-processor` as a normal application
dependency: it is only needed while compiling.
