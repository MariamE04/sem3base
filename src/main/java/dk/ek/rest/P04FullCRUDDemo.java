package dk.ek.rest;

import dk.ek.rest.controllers.IController;
import dk.ek.rest.controllers.PersonController;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To demonstrate the use of unprotected routes
 * Author: Thomas Hartmann
 */
public class P04FullCRUDDemo {
    private static IController personController = new PersonController();
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .startServer(7007)
                .setRoute(new RestRoutes().getOpenRoutes())
                .setCORS()
                .setRoute(()->{
                    path("/test", () -> {
                        get("/", ctx->ctx.contentType("text/plain").result("Hello World"));
                        get("/{id}", ctx->ctx.contentType("text/plain").result("Hello World "+ctx.pathParam("id")));
//                        post("/", ctx->ctx.result("Hello World "+ctx.body()));
//                        put("/{id}", ctx->ctx.result("Url: "+ctx.fullUrl()+", Path parameter: "+ctx.pathParam("id")+", Body: "+ctx.body()));
//                        delete("/{id}", ctx->ctx.result("Hello World "+ctx.pathParam("id")));
                    });
                });
    }
}
