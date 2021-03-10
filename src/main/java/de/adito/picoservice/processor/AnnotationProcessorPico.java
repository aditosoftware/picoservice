package de.adito.picoservice.processor;

import de.adito.picoservice.PicoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.*;
import java.lang.annotation.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import java.util.*;

/**
 * Generates instances of {@link de.adito.picoservice.IPicoRegistration} and the corresponding service entries in the
 * META-INF directory for classes annotated with annotations that are annotated with {@link de.adito.picoservice.PicoService}.
 *
 * @author j.boesl, 23.03.15
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class AnnotationProcessorPico extends AbstractProcessor
{
  private static final String PICO_POSTFIX = "PicoService";
  private static final String REGISTRATION_TEMPLATE = "package {0};\n" +
      "\n" +
      "import de.adito.picoservice.IPicoRegistration;\n" +
      "\n" +
      "import {4};\n" +
      "\n" +
      "@Generated(value = \"de.adito.picoservice.processor.AnnotationProcessorPico\", date = \"{3}\")\n" +
      "public class {1} implements IPicoRegistration\n" +
      "'{'\n" +
      "  @Override\n" +
      "  public Class<?> getAnnotatedClass()\n" +
      "  '{'\n" +
      "    return {2}.class;\n" +
      "  '}'\n" +
      "'}'";
  private static final String SERVICE_REGISTRATION_PATH = "META-INF/services/de.adito.picoservice.IPicoRegistration";
  private static final List<ElementKind> ENCLOSING_TYPES =
      Arrays.asList(ElementKind.PACKAGE, ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM);

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
  {
    if (roundEnv.processingOver())
      return false;

    Set<TypeElement> annotatedElements = new LinkedHashSet<>();
    for (TypeElement annotation : annotations)
    {
      if (annotation.getAnnotation(PicoService.class) != null)
      {
        if (_isValidElement(annotation))
          for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation))
            annotatedElements.add((TypeElement) annotatedElement);
      }
    }
    if (!annotatedElements.isEmpty())
      _generateRegistration(annotatedElements);
    return false;
  }

  private void _generateRegistration(Set<TypeElement> pAnnotatedElements)
  {
    Set<String> serviceSet = new LinkedHashSet<>();
    Filer filer = processingEnv.getFiler();
    for (TypeElement typeElement : pAnnotatedElements)
    {
      try
      {
        _ElementInfo eI = new _ElementInfo(typeElement);
        eI.write(filer);
        serviceSet.add(eI.fqn);
      }
      catch (IOException e)
      {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage(), typeElement);
      }
    }

    try
    {
      FileObject serviceFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_REGISTRATION_PATH);
      if (Files.isRegularFile(Paths.get(serviceFile.toUri())))
      {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(serviceFile.openInputStream(), StandardCharsets.UTF_8)))
        {
          String line;
          while ((line = reader.readLine()) != null)
            serviceSet.add(line);
        }
      }
    }
    catch (IOException e)
    {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't load existing serviceSet: " + e);
    }

    try
    {
      List<String> services = new ArrayList<>(serviceSet);
      Collections.sort(services);
      FileObject serviceFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_REGISTRATION_PATH);
      try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(serviceFile.openOutputStream(), StandardCharsets.UTF_8)))
      {
        for (String service : services)
          writer.println(service);
      }
    }
    catch (IOException x)
    {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write service definition files: " + x);
    }
  }

  private boolean _isValidElement(Element pElement)
  {
    Retention retention = pElement.getAnnotation(Retention.class);
    if (retention == null || retention.value() != RetentionPolicy.RUNTIME)
    {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Retention should be RUNTIME", pElement);
      return false;
    }
    Target target = pElement.getAnnotation(Target.class);
    if (target == null || target.value().length == 0)
    {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Target has to be defined", pElement);
      return false;
    }
    else
    {
      for (ElementType elementType : target.value())
      {
        if (elementType != ElementType.TYPE)
        {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Unsupported type: " + elementType, pElement);
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Bag for element info.
   */
  private class _ElementInfo
  {
    String pckg;
    String annotatedClsName;
    String clsName;
    String fqn;

    _ElementInfo(TypeElement pTypeElement)
    {
      pckg = _getPackage(pTypeElement);
      annotatedClsName = _getAnnotatedClassName(pTypeElement);
      clsName = annotatedClsName.replaceAll("\\.", "\\$") + PICO_POSTFIX;
      fqn = pckg + "." + clsName;
    }

    void write(Filer pFiler) throws IOException
    {
      FileObject sourceFile = pFiler.getResource(StandardLocation.SOURCE_OUTPUT, pckg, clsName + ".java");
      sourceFile.delete();
      try (OutputStream outputstream = pFiler.createSourceFile(fqn).openOutputStream())
      {
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date());
        String importString = _getJavaVersion() >= 9 ?
            "javax.annotation.processing.Generated" :
            "javax.annotation.Generated";
        String content = MessageFormat.format(REGISTRATION_TEMPLATE, pckg, clsName, annotatedClsName, date, importString);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputstream, StandardCharsets.UTF_8)))
        {
          writer.print(content);
        }
      }
    }

    private int _getJavaVersion()
    {
      try {
        return Integer.parseInt(System.getProperty("java.specification.version"));
      }
      catch (NumberFormatException pE) {
        return 8;
      }
    }

    private String _getPackage(Element pElement)
    {
      Element element = pElement;
      while (element != null)
      {
        if (element.getKind() == ElementKind.PACKAGE)
          return element.toString();
        Element enclosingElement = element.getEnclosingElement();
        if (ENCLOSING_TYPES.contains(enclosingElement.getKind()))
          element = enclosingElement;
        else
        {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Element type not supported", pElement);
          break;
        }
      }
      return "";
    }

    private String _getAnnotatedClassName(Element pElement)
    {
      StringBuilder name = new StringBuilder();
      Element element = pElement;
      while (element != null && element.getKind() != ElementKind.PACKAGE)
      {
        if (name.length() > 0)
          name.insert(0, ".");
        name.insert(0, element.getSimpleName());
        Element enclosingElement = element.getEnclosingElement();
        if (ENCLOSING_TYPES.contains(enclosingElement.getKind()))
          element = enclosingElement;
        else
        {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Element type not supported", pElement);
          break;
        }
      }
      return name.toString();
    }
  }

}
