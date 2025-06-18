
/**
 * @author dover
 * @date 2025-05-14
 */
@Configuration
public class I18nConfig {

    public static final String LANGUAGE_HEADER = "lang";

    public static final String ERROR_CODE_PREFIX = "DOVER";

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasenames("classpath:messages"); // base name 为 messages, 可加载如 messages_zh.properties, messages_en.properties
        source.setDefaultEncoding("UTF-8");
        source.setCacheSeconds(5); // 缓存时间，开发环境可设为 -1 表示不缓存
        return source;
    }

}