
/**
 * 需要maven依赖 和 插件配置
 * 
 * <dependency>
 * <groupId>org.mapstruct</groupId>
 * <artifactId>mapstruct</artifactId>
 * <version>${org.mapstruct.version}</version>
 * </dependency>
 *
 * <plugin>
 * <groupId>org.apache.maven.plugins</groupId>
 * <artifactId>maven-compiler-plugin</artifactId>
 * <version>3.8.1</version>
 * <configuration>
 * <source>${java.version}</source>
 * <target>${java.version}</target>
 * <encoding>${project.build.sourceEncoding}</encoding>
 * <annotationProcessorPaths>
 * <path>
 * <groupId>org.mapstruct</groupId>
 * <artifactId>mapstruct-processor</artifactId>
 * <version>${org.mapstruct.version}</version>
 * </path>
 * <path>
 * <groupId>org.projectlombok</groupId>
 * <artifactId>lombok</artifactId>
 * <version>${lombok.version}</version>
 * </path>
 * </annotationProcessorPaths>
 * </configuration>
 * </plugin>
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BizConvertUtil {
    BizConvertUtil INSTANCE = Mappers.getMapper(BizConvertUtil.class);

    TemplateDefinitionAllMainContractDto.TemplateDefinitionCodeAndNameDto toTemplateDefinitionCodeAndNameDto(TemplateDefinitionBean templateDefinition);
}