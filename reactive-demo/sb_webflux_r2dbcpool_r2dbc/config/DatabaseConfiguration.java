package com.example.reactive.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.transaction.ReactiveTransactionManager;

import javax.annotation.Resource;
import java.util.Optional;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.MAX_SIZE;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
@EnableR2dbcRepositories
class DatabaseConfiguration extends AbstractR2dbcConfiguration {

    @Resource
    private R2dbcProperties r2dbcProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactoryBuilder.withOptions(
            ConnectionFactoryOptions.builder()
                .from(ConnectionFactoryOptions.parse(r2dbcProperties.getUrl()))
//            .option(DRIVER, DB_DRIVER)
//            .option(HOST, r2dbcProperties.getProperties().get("host"))
//            .option(PROTOCOL, DB_PROTOCOL)
//            .option(PORT, Integer.parseInt(r2dbcProperties.getProperties().get("port")))
//            .option(DATABASE, r2dbcProperties.getName())
                .option(MAX_SIZE, r2dbcProperties.getPool().getMaxSize())
                .option(USER, r2dbcProperties.getUsername())
                .option(PASSWORD, r2dbcProperties.getPassword())
                .option(SSL, Optional.ofNullable(r2dbcProperties.getProperties().get("ssl"))
                    .map(Boolean::parseBoolean)
                    .orElse(false))
                .build()
                .mutate()).build();
//        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
//                .option(DRIVER, DB_DRIVER)
//                .option(PROTOCOL, DB_PROTOCOL)
//                .option(MAX_SIZE, Integer.valueOf(maxSize))
//                .option(HOST, host)
//                .option(PORT, port)
//                .option(USER, username)
//                .option(PASSWORD, password)
//                .option(DATABASE, database)
//                .build());
        //return new PostgresqlConnectionFactory(
        //        PostgresqlConnectionConfiguration.builder()
        //                .host(host)
        //                .port(port)
        //                .database(database)
        //                .username(username)
        //                .password(password)
        //                .build()
        //);
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        ClassPathResource cpr = new ClassPathResource("schema.sql");
        if (cpr.exists()) {
            populator.addPopulators(new ResourceDatabasePopulator(cpr));
        }
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
