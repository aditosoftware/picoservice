package de.adito.picoservice.testprocessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.Set;

/**
 * Test fixture that introduces a {@code @TestAnno}-annotated class (source code of the class).
 */
@SupportedAnnotationTypes("*")
public class MySecondAnnotatedTypeProcessor extends AbstractProcessor
{
  public static final String GENERATED_CLASS_SOURCE = "package de.adito.picoservice.test;\n" +
      "\n" +
      "@TestAnno(0)\n" +
      "public class GeneratedInSecondRound implements ITestAnnotated\n" +
      "{" +
      "}";

  private boolean generated;

  @Override
  public boolean process(Set<? extends TypeElement> pAnnotations, RoundEnvironment pRoundEnvironment)
  {
    if (!generated && !pRoundEnvironment.processingOver())
    {
      generated = true;
      try
      {
        Filer filer = processingEnv.getFiler();
        JavaFileObject sourceFile = filer.createSourceFile("de.adito.picoservice.test.GeneratedInSecondRound");
        try (Writer writer = sourceFile.openWriter())
        {
          writer.write(GENERATED_CLASS_SOURCE);
        }
      }
      catch (IOException e)
      {
        throw new IllegalStateException("Could not generate the second-round test type", e);
      }
    }
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion()
  {
    return SourceVersion.latestSupported();
  }
}
