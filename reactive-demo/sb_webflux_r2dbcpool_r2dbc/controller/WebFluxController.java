package com.example.reactive.controller;

import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.TomcatHttpHandlerAdapter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import javax.naming.Context;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author dover
 * @since 2023/9/1
 */
public class WebFluxController {


    public void helloHandler() {
        // handler 用于处理请求内容并响应
        HandlerFunction<ServerResponse> helloHandler = request -> {
            Optional<String> name = request.queryParam("name");
            return ServerResponse.ok().body(BodyInserters.fromValue("Hello to " + name.orElse("the world.")));
        };


        // router 用于匹配请求，类似于 @RequestMapping
        // RequestPredicates 可以组合匹配条件
        RouterFunction<ServerResponse> helloRoute = RouterFunctions.route(RequestPredicates.path("/hello"),
            helloHandler);


        // handlerFilter，类似于 servletFilter，在 handler处理前执行，可以拦截请求
        helloRoute.filter((request, next) -> {
            if (request.headers().acceptCharset().contains(StandardCharsets.UTF_8)) {
                return next.handle(request);
            } else {
                return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
            }
        });
        HttpHandler httpHandler = RouterFunctions.toHttpHandler(helloRoute);

        // netty 作为服务器
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer nettyServer = HttpServer.create();
        DisposableServer disposableServer = nettyServer.handle(adapter).host("localhost").port(8080).bindNow();


//        Servlet servlet = new TomcatHttpHandlerAdapter(httpHandler);
//        Tomcat server = new Tomcat();
//        File root = new File(System.getProperty("java.io.tmpdir"));
//        Context rootContext = server.addContext("", root.getAbsolutePath());
//        server.(rootContext, "ctx", servlet);
//        rootContext.addServletMappingDecoded("/", "ctx");
//        server.setHost(host);
//        server.setPort(port);
//        server.start();


    }


}
