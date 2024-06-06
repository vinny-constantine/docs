
/**
 * 动态切换 mybatis 的 ExecutorType
 *
 * @author dover
 * @since 2022/3/18
 */
@Configuration
public class MybatisExecutorMarkedConfiguration {

    private static SqlSessionTemplate sqlSessionTemplate;


    public MybatisExecutorMarkedConfiguration(SqlSessionTemplate sqlSessionTemplate) {
        SqlSession sqlSessionProxy = (SqlSession) newProxyInstance(SqlSessionFactory.class.getClassLoader(),
            new Class[]{SqlSession.class}, new ExecutorMarkedSqlSessionInterceptor(sqlSessionTemplate));
        // 替换 sqlSessionTemplate 里原有的 sqlSessionProxy
        DrpReflectUtil.setEntityAttribute(sqlSessionTemplate, "sqlSessionProxy", sqlSessionProxy);
        MybatisExecutorMarkedConfiguration.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 用于切换Executor，必须在事务开始处使用，避免mybatis使用默认的 SimpleExecutor 后再切换，出现异常“事务中切换executorType”
     */
    public static final class DrpMybatisExecutorUtils {

        public static void setBatchExecutor() {
            SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(), ExecutorType.BATCH,
                sqlSessionTemplate.getPersistenceExceptionTranslator());
        }
    }

    /**
     * 用于切换Executor的拦截器，替换 mybatis 的 自带拦截器
     * <p>
     * 见 org.mybatis.spring.SqlSessionTemplate.SqlSessionInterceptor
     */
    private class ExecutorMarkedSqlSessionInterceptor implements InvocationHandler {

        private SqlSessionTemplate sqlSessionTemplate;

        public ExecutorMarkedSqlSessionInterceptor(SqlSessionTemplate sqlSessionTemplate) {
            this.sqlSessionTemplate = sqlSessionTemplate;
        }

        @Override
        @SuppressWarnings("all")
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            /*
             * 1. 优先根据事务同步器里包含的 SqlSession 来获取 ExecutorType
             * 2. 其次根据 mapper 方法参数中是否包含 ExecutorType 来获取
             * 3. 最后根据 SqlSessionFactory 的 configuration 来获取
             */
            SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(
                sqlSessionTemplate.getSqlSessionFactory());
            ExecutorType executorType = Optional.ofNullable(holder)
                .filter(x -> holder.isSynchronizedWithTransaction())
                .map(SqlSessionHolder::getExecutorType)
                .orElse((ExecutorType) Arrays.stream(args)
                    .filter(x -> x instanceof Map)
                    .findFirst()
                    .map(x -> ((Map<String, Object>) x).values()
                        .stream()
                        .filter(y -> y instanceof ExecutorType)
                        .findFirst()
                        .orElse(sqlSessionTemplate.getExecutorType()))
                    .orElse(sqlSessionTemplate.getExecutorType()));
            // 以下代码无改动，均 copy 自 org.mybatis.spring.SqlSessionTemplate.SqlSessionInterceptor
            SqlSession sqlSession = getSqlSession(sqlSessionTemplate.getSqlSessionFactory(), executorType,
                sqlSessionTemplate.getPersistenceExceptionTranslator());
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, sqlSessionTemplate.getSqlSessionFactory())) {
                    // force commit even on non-dirty sessions because some databases require
                    // a commit/rollback before calling close()
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                Throwable unwrapped = unwrapThrowable(t);
                if (sqlSessionTemplate.getPersistenceExceptionTranslator() != null
                    && unwrapped instanceof PersistenceException) {
                    // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
                    closeSqlSession(sqlSession, sqlSessionTemplate.getSqlSessionFactory());
                    sqlSession = null;
                    Throwable translated = sqlSessionTemplate.getPersistenceExceptionTranslator()
                        .translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throw unwrapped;
            } finally {
                if (sqlSession != null) {
                    closeSqlSession(sqlSession, sqlSessionTemplate.getSqlSessionFactory());
                }
            }
        }
    }

}
