package io.github.mrairing.interpolation.annotations.processors;

import com.google.auto.service.AutoService;
import io.github.mrairing.interpolation.annotations.Interpolated;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("io.github.mrairing.interpolation.annotations.Interpolated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class InterpolatedUsagesCheckProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Interpolated.class)
                .stream()
                .map(Element::getEnclosingElement)
                .forEach(this::check);

        return true;
    }

    private void check(Element element) {
        checkIsMethod(element);
        checkParameters(((ExecutableElement) element));
    }

    private void checkParameters(ExecutableElement element) {
        List<? extends VariableElement> parameters = element.getParameters();

        if (parameters.size() != 2
                || !isStringType(parameters.get(0))
                || !isObjectsVarArgType(parameters.get(1))
                || !element.isVarArgs()
                || !isStringType(element.getReturnType())) {
            logError("The string interpolation method signature should be of the form " +
                    "'String <methodName>(@Interpolated String <arg1>, Object... <arg2>)'.", element);
        }
    }

    private boolean isObjectsVarArgType(VariableElement parametr) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();

        TypeMirror objectType = elementUtils.getTypeElement(Object.class.getCanonicalName()).asType();
        ArrayType objectArrayType = typeUtils.getArrayType(objectType);

        return typeUtils.isSameType(objectArrayType, parametr.asType());
    }

    private boolean isStringType(VariableElement parameter) {
        return isStringType(parameter.asType());
    }

    private boolean isStringType(TypeMirror type) {
        Elements elementUtils = processingEnv.getElementUtils();

        TypeMirror stringType = elementUtils.getTypeElement(String.class.getCanonicalName()).asType();

        return processingEnv.getTypeUtils().isSameType(stringType, type);
    }

    private void checkIsMethod(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            logError("@Interpolated is applicable only in method declarations.", element);
        }
    }

    private <E extends Element> void logError(String msg, E e) {
        Messager messager = processingEnv.getMessager();

        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
