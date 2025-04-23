
@Configuration
public class FilterConfig {


    @Bean
    public FilterRegistrationBean<HeaderFilter> filterRegistrationBean() {
        FilterRegistrationBean<HeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HeaderFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }


    public static class HeaderFilter implements Filter {

        @Override
        public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {

        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            String userId = req.getHeader("userId");
            chain.doFilter(req, response);
        }

        @Override
        public void destroy() {
        }
    }
}
