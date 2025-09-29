package dk.ek.rest;

import dk.ek.rest.controllers.PersonController;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To demonstrate the use of error handling
 * Author: Thomas Hartmann
 */
public class P05ErrorHandling {
    // ApplicationConfig.setApiExceptionHandling() is called in the ApplicationConfig constructor
    //
    private static PersonController personController = new PersonController();
    private static RestRoutes restRoutes = new RestRoutes();

    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7007)
                .setRoute(restRoutes.getOpenRoutes())
                .setRoute(() -> {
                    path("/test", () -> {
                        get("/", ctx -> ctx.contentType("text/plain").result("Hello World"));
                        get("/{id}", ctx -> ctx.contentType("text/plain").result("Hello World " + ctx.pathParam("id")));
                        post("/", ctx -> ctx.contentType("text/plain").result("Hello World " + ctx.body()));
                        put("/{id}", ctx -> ctx.contentType("text/plain").result("Url: " + ctx.fullUrl() + ", Path parameter: " + ctx.pathParam("id") + ", Body: " + ctx.body()));
                        delete("/{id}", ctx -> ctx.contentType("text/plain").result("Hello World " + ctx.pathParam("id")));
                    });
                });
    }
}
