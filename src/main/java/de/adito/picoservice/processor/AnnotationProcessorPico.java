package de.adito.picoservice.processor;

import de.adito.picoservice.PicoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.*;
import java.lang.annotation.*;
import java.text.*;
import java.util.*;

/**
 * @author j.boesl, 23.03.15
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class AnnotationProcessorPico extends AbstractProcessor
{
  private static final String PICO_POSTFIX = "PicoService";
  private static final String REGISTRATION_TEMPLATE = "package {0};\n" +
      "\n" +
      "import de.adito.picoservice.IPicoRegistration;\n" +
      "\n" +
      "import javax.annotation.Generated;\n" +
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

    Set<TypeElement> annotatedElements = new LinkedHashSet<TypeElement>();
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
    Set<String> serviceSet = new LinkedHashSet<String>();
    Filer filer = processingEnv.getFiler();
    for (TypeElement typeElement : pAnnotatedElements)
    {
      try
      {
        _ElementInfo eI = new _ElementInfo(typeElement);
        JavaFileObject sourceFile = filer.createSourceFile(eI.fqn);
        if (sourceFile.getLastModified() == 0) // doesn't exist, yet.
        {
          String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date());
          String content = MessageFormat.format(REGISTRATION_TEMPLATE, eI.pckg, eI.clsName, eI.annotatedClsName, date);
          PrintWriter writer = new PrintWriter(new OutputStreamWriter(sourceFile.openOutputStream(), "UTF-8"));
          writer.print(content);
          writer.close();
        }
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
      BufferedReader reader = new BufferedReader(new InputStreamReader(serviceFile.openInputStream(), "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null)
        serviceSet.add(line);
      reader.close();
    }
    catch (FileNotFoundException e)
    {
      // ignore
    }
    catch (IOException e)
    {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't load existing serviceSet: " + e);
    }

    try
    {
      List<String> services = new ArrayList<String>(serviceSet);
      Collections.sort(services);
      FileObject serviceFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_REGISTRATION_PATH);
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(serviceFile.openOutputStream(), "UTF-8"));
      for (String service : services)
        writer.println(service);
      writer.close();
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
    if (target == null || target.value() == null || target.value().length == 0)
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

    public _ElementInfo(TypeElement pTypeElement)
    {
      pckg = _getPackage(pTypeElement);
      annotatedClsName = _getAnnotatedClassName(pTypeElement);
      clsName = annotatedClsName.replaceAll("\\.", "\\$") + PICO_POSTFIX;
      fqn = pckg + "." + clsName;
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
      String name = "";
      Element element = pElement;
      while (element != null && element.getKind() != ElementKind.PACKAGE)
      {
        if (!name.isEmpty())
          name = "." + name;
        name = element.getSimpleName() + name;
        Element enclosingElement = element.getEnclosingElement();
        if (ENCLOSING_TYPES.contains(enclosingElement.getKind()))
          element = enclosingElement;
        else
        {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Element type not supported", pElement);
          break;
        }
      }
      return name;
    }
  }

}
