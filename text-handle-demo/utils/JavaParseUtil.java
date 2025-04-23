package com.dover.demo.util;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author dover
 * @date 2025-03-17
 */
public class JavaParseUtil {
    /**
     * @param args
     */
    public static void main(String[] args) {
        parseJavaFile();
    }

    private static void parseJavaFile() {
        try {
            File file = new File("D:\\workspace\\dover-demo\\src\\main\\java\\com\\dover\\demo\\entity\\ImportTemplateDetailBean.java");
            FileInputStream fis = new FileInputStream(file);

            ASTParser parser = ASTParser.newParser(8);
            parser.setSource(IOUtils.toCharArray(fis, StandardCharsets.UTF_8));
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);

            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    System.out.println("Class: " + node.getName());
                    return true;
                }
                @Override
                public boolean visit(FieldDeclaration node) {
                    for (Object fragment : node.fragments()) {
                        VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                        System.out.println("  字段编码: " + vdf.getName());
                        System.out.println("  字段类型: " + node.getType());
                        // 获取字段注释
                        String javaDoc = Optional.ofNullable(node.getJavadoc()).map(ASTNode::toString).map(x -> x.replaceAll("[*/\\s]", "")).orElse("");
                        System.out.println("  字段描述: " + javaDoc);
                        System.out.println("============================");
                        System.out.println();

                        // 获取字段上的注解
                        for (Object modifier : node.modifiers()) {
                            if (modifier instanceof Annotation) {
                                Annotation annotation = (Annotation) modifier;
                                System.out.println("    Annotation: " + annotation.getTypeName());
                            }
                        }
                    }
                    return super.visit(node);
                }
                @Override
                public boolean visit(MethodDeclaration node) {
                    System.out.println("  Method: " + node.getName());
                    System.out.println("    Return Type: " + node.getReturnType2());
                    System.out.println("    Modifiers: " + node.getModifiers());

                    // 获取方法注释
                    System.out.println("    Comment: " + String.valueOf(node.getJavadoc()));

                    // 获取方法上的注解
                    for (Object modifier : node.modifiers()) {
                        if (modifier instanceof Annotation) {
                            Annotation annotation = (Annotation) modifier;
                            System.out.println("    Annotation: " + annotation.getTypeName());
                        }
                    }
                    return super.visit(node);
                }
            });
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}