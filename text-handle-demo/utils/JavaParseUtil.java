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
    
    @SneakyThrows
    public static void main(String[] args) {
        File file = ResourceUtils.getFile("D:\\workspace\\dover-demo\\src\\main\\java\\com\\dover\\demo\\entity\\QueryTradeIntegrationInfoResp.java");
        Map<String, List<JavaFieldDto>> classAndFieldMap = parseJavaFile(file);
        log.info(JSON.toJSONString(classAndFieldMap));
    }

    private static Map<String, List<JavaFieldDto>> parseJavaFile(File file) {
        try {
            //获取待解析的文件流
            InputStream fis = Files.newInputStream(file.toPath());
            ASTParser parser = ASTParser.newParser(8);
            parser.setSource(IOUtils.toCharArray(fis, StandardCharsets.UTF_8.name()));
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            //解析后的类名和字段集合map
            HashMap<String, List<JavaFieldDto>> classAndFieldMap = new HashMap<>();
            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    String className = node.getName().toString();
                    log.debug("Class: " + className);
                    if (!classAndFieldMap.containsKey(className)) {
                        classAndFieldMap.put(className, new ArrayList<>());
                    }
                    return true;
                }

                @Override
                public boolean visit(FieldDeclaration node) {
                    String className = ((TypeDeclaration) node.getParent()).getName().toString();
                    log.debug("字段所属类名: " + className);
                    if (!classAndFieldMap.containsKey(className)) classAndFieldMap.put(className, new ArrayList<>());
                    for (Object fragment : node.fragments()) {
                        VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                        String fieldName = vdf.getName().toString();
                        // 获取字段注释
                        String javaDoc = getJavaDoc(node);
                        log.debug("  字段编码: " + fieldName);
                        log.debug("  字段类型: " + node.getType());
                        log.debug("  字段描述: " + javaDoc);
                        log.debug("============================");
                        // 获取字段上的注解
                        for (Object modifier : node.modifiers()) {
                            if (modifier instanceof Annotation) {
                                Annotation annotation = (Annotation) modifier;
                                log.debug("    Annotation: " + annotation.getTypeName());
                            }
                        }
                        //添加字段到集合中
                        classAndFieldMap.get(className).add(JavaFieldDto.builder().name(fieldName).type(node.getType().toString()).desc(javaDoc).build());
                    }
                    return super.visit(node);
                }

                @Override
                public boolean visit(MethodDeclaration node) {
                    log.debug("  Method: " + node.getName());
                    log.debug("    Return Type: " + node.getReturnType2());
                    log.debug("    Modifiers: " + node.getModifiers());
                    // 获取方法注释
                    log.debug("    Comment: " + node.getJavadoc());
                    // 获取方法上的注解
                    for (Object modifier : node.modifiers()) {
                        if (modifier instanceof Annotation) {
                            Annotation annotation = (Annotation) modifier;
                            log.debug("    Annotation: " + annotation.getTypeName());
                        }
                    }
                    return super.visit(node);
                }
            });
            fis.close();
            Pattern pattern = Pattern.compile("<(?<className>/w+)>");
            List<String> toRemoveClassNameList = new ArrayList<>();
            //组装类
            for (Map.Entry<String, List<JavaFieldDto>> classAndField : classAndFieldMap.entrySet()) {
                String key = classAndField.getKey();
                for (JavaFieldDto javaFieldDto : classAndField.getValue()) {
                    String type = javaFieldDto.getType();
                    if (type != null) {
                        Matcher matcher = pattern.matcher(type);
                        if (matcher.find()) {
                            String className = matcher.group("className");
                            if (!className.equals(key)) {
                                javaFieldDto.setChildren(classAndFieldMap.get(className));
                                toRemoveClassNameList.add(className);
                            }
                        }
                    }
                }
            }
            toRemoveClassNameList.forEach(classAndFieldMap::remove);
            classAndFieldMap.forEach((k, v) -> v.forEach(item -> setLevel(1, item)));
            return classAndFieldMap;
        } catch (IOException e) {
            log.error("解析java文件失败", e);
        }
        return new HashMap<>();
    }

    /**
     * 递归设置层级
     */
    private static void setLevel(int level, JavaFieldDto javaFieldDto) {
        javaFieldDto.setLevel(level);
        if (CollUtil.isNotEmpty(javaFieldDto.getChildren())) {
            for (JavaFieldDto child : javaFieldDto.getChildren()) {
                setLevel(level + 1, child);
            }
        }
    }

    private static String getJavaDoc(BodyDeclaration node) {
        return Optional.ofNullable(node.getJavadoc()).map(ASTNode::toString).map(x -> x.replaceAll("[*/\\s]", "")).orElse("");
    }

}
