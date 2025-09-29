package dk.ek.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.ek.exceptions.ApiException;
//import dk.ek.security.ISecurityController;
//import dk.ek.security.SecurityController;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import static io.javalin.apibuilder.ApiBuilder.path;

//import dk.ek.security.SecurityRoutes.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purpose: To configure the Javalin server
 * Author: Thomas Hartmann
 */
public class ApplicationConfig {
    private ObjectMapper jsonMapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static ApplicationConfig appConfig;
    private static JavalinConfig javalinConfig;
    private static Javalin app;
//    private static ISecurityController securityController = SecurityController.getInstance();

    private ApplicationConfig() {
    }

    public static ApplicationConfig getInstance() {
        if (appConfig == null) {
            appConfig = new ApplicationConfig();
        }
        return appConfig;
    }

    public ApplicationConfig initiateServer() {
        app = Javalin.create(config -> {
            javalinConfig = config;
            config.bundledPlugins.enableDevLogging(); // enables extensive development logging in terminal
            config.staticFiles.add("/public"); // enables serving of static files from the public folder in the classpath. PROs: easy to use, CONs: you have to restart the server every time you change a file
            config.http.defaultContentType = "application/json"; // default content type for requests
            config.router.contextPath = "/api"; // base path for all routes
            config.bundledPlugins.enableRouteOverview("/routes"); // html overview of all registered routes at /routes for api documentation: https://javalin.io/news/2019/08/11/javalin-3.4.1-released.html
        });
        return appConfig;
    }

    public ApplicationConfig setRoute(EndpointGroup route) {
        javalinConfig.router.apiBuilder(() -> {
            path("/", route);
        });
        return appConfig;
    }


    public ApplicationConfig setCORS() {
        app.before(ctx -> {
            setCorsHeaders(ctx);
        });
        app.options("/*", ctx -> { // Burde nok ikke være nødvendig?
            setCorsHeaders(ctx);
        });
        return appConfig;
    }

    private static void setCorsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    // Adding below methods to ApplicationConfig, means that EVERY ROUTE will be checked for security roles. So open routes must have a role of ANYONE
//    public ApplicationConfig checkSecurityRoles() {
//        app.beforeMatched(securityController::authenticate); // check if there is a valid token in the header
//        app.beforeMatched(securityController::authorize); // check if the user has the required role
//        return appConfig;
//    }

    public ApplicationConfig startServer(int port) {
        app.start(port);

        return appConfig;
    }

    public ApplicationConfig stopServer() {
        app.stop();
        return appConfig;
    }
//    public ApplicationConfig setApiExceptionHandling() {
//        app.exception(ApiException.class, (e, ctx) -> {
//            int statusCode = e.getStatusCode();
//            ObjectNode on = jsonMapper
//                    .createObjectNode()
//                    .put("status", statusCode)
//                    .put("msg", e.getMessage());
//            ctx.json(on);
//            ctx.status(statusCode);
//        });
//        return appConfig;
//    }



    public ApplicationConfig setGeneralExceptionHandling() {
        app.exception(Exception.class, (e, ctx) -> {
            int statusCode = (e instanceof ApiException apiEx)
                    ? apiEx.getStatusCode()
                    : 500;

            String message = (e instanceof ApiException)
                    ? e.getMessage()
                    : "Internal server error";

            logger.error("An exception occurred", e);

            ObjectNode on = jsonMapper
                    .createObjectNode()
                    .put("status", statusCode)
                    .put("msg", message);

            ctx.json(on);
            ctx.status(statusCode);
        });
        return appConfig;
    }



    public ApplicationConfig beforeFilter() {
        app.before(ctx -> {
            String pathInfo = ctx.req().getPathInfo();
            ctx.req().getHeaderNames().asIterator().forEachRemaining(el -> System.out.println(el));
        });
        return appConfig;
    }
}
