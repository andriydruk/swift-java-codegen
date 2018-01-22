package com.readdle.codegen;

import com.readdle.codegen.anotation.SwiftFunc;

import java.io.IOException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

class SwiftGetterDescriptor implements JavaSwiftProcessor.WritableElement {

    private String javaName;
    private String swiftName;

    private boolean isStatic;

    private SwiftEnvironment.Type returnSwiftType;
    private boolean isReturnTypeOptional;

    private String description;

    SwiftGetterDescriptor(ExecutableElement executableElement) {
        this.javaName = executableElement.getSimpleName().toString();
        this.isStatic = executableElement.getModifiers().contains(Modifier.STATIC);
        this.returnSwiftType = SwiftEnvironment.parseJavaType(executableElement.getReturnType().toString());
        this.isReturnTypeOptional = JavaSwiftProcessor.isNullable(executableElement);

        if (executableElement.getThrownTypes().size() != 0) {
            throw new IllegalArgumentException("Getter can't throw");
        }

        if (executableElement.getParameters().size() != 0) {
            throw new IllegalArgumentException("Getter can't has parameters");
        }

        SwiftFunc swiftFunc = executableElement.getAnnotation(SwiftFunc.class);
        if (swiftFunc != null && !swiftFunc.value().isEmpty()) {
            this.swiftName = swiftFunc.value();
        }
        else {
            this.swiftName = javaName;
            if (swiftName.startsWith("get")) {
                swiftName = swiftName.substring(3);
            }
            char first = Character.toLowerCase(swiftName.charAt(0));
            swiftName = first + swiftName.substring(1);
        }
    }

    @Override
    public void generateCode(SwiftWriter swiftWriter, String javaFullName, String swiftType) throws IOException {
        String swiftFuncName = "Java_" + javaFullName.replace("/", "_").replace("$", "_00024") + "_" + javaName;

        swiftWriter.emitEmptyLine();
        swiftWriter.emitStatement(String.format("@_silgen_name(\"%s\")", swiftFuncName));
        swiftWriter.emit(String.format("public func %s(env: UnsafeMutablePointer<JNIEnv?>, %s", swiftFuncName, isStatic ? "clazz: jclass" : "this: jobject"));
        swiftWriter.emit(String.format(")%s {\n", returnSwiftType != null ? " -> jobject?" : ""));
        swiftWriter.emitEmptyLine();

        if (!isStatic) {
            swiftWriter.emitStatement(String.format("let swiftSelf: %s", swiftType));
            swiftWriter.emitStatement("do {");
            swiftWriter.emitStatement(String.format("swiftSelf = try %s.from(javaObject: this)", swiftType));
            swiftWriter.emitStatement("}");
            swiftWriter.emitStatement("catch {");
            swiftWriter.emitStatement("let errorString = String(reflecting: type(of: error)) + String(describing: error)");
            swiftWriter.emitStatement("JNI.api.ThrowNew(JNI.env, SwiftRuntimeErrorClass, errorString)");
            swiftWriter.emitStatement(String.format("return%s", returnSwiftType != null ? " nil" : ""));
            swiftWriter.emitStatement("}");
        }

        swiftWriter.emitStatement(String.format("let result = %s.%s", isStatic ? swiftType : "swiftSelf", swiftName));

        if (returnSwiftType != null) {
            swiftWriter.emitStatement("do {");
            if (isReturnTypeOptional) {
                swiftWriter.emitStatement("return try result?.javaObject()");
            }
            else {
                swiftWriter.emitStatement("return try result.javaObject()");
            }
            swiftWriter.emitStatement("}");
            swiftWriter.emitStatement("catch {");
            swiftWriter.emitStatement("let errorString = String(reflecting: type(of: error)) + String(describing: error)");
            swiftWriter.emitStatement("JNI.api.ThrowNew(JNI.env, SwiftRuntimeErrorClass, errorString)");
            swiftWriter.emitStatement("return nil");
            swiftWriter.emitStatement("}");
        }

        swiftWriter.emitStatement("}");

        swiftWriter.emitEmptyLine();
    }

    @Override
    public String toString() {
        return "SwiftGetterDescriptor{" +
                "javaName='" + javaName + '\'' +
                ", swiftName='" + swiftName + '\'' +
                ", isStatic=" + isStatic +
                ", returnSwiftType=" + returnSwiftType +
                ", isReturnTypeOptional=" + isReturnTypeOptional +
                ", description='" + description + '\'' +
                '}';
    }
}