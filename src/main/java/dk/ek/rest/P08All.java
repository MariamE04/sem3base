package dk.ek.rest;

import dk.ek.security.SecurityRoutes;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

/**
 * Purpose: To demonstrate the use of unprotected routes
 * 1. Hashing of passwords in security.User
 * 2. Login and register in SecurityController
 * 3. Authenticate in SecurityController
 * 4. Authorize in SecurityController
 * 5. SecurityRoutes (auth and protected)
 * 6. SecurityTest with Login and token send to protected
 *
 * Author: Thomas Hartmann
 */
public class P08All {

//    private static IController personController = PersonEntityController.getInstance();
    public static void main(String[] args) {
        ApplicationConfig
            .getInstance()
            .initiateServer()
            .checkSecurityRoles() // check for role when route is called
            .setRoute(SecurityRoutes.getSecurityRoutes())
            .setRoute(SecurityRoutes.getSecuredRoutes())
            .setRoute(new RestRoutes().getOpenRoutes())
            .setRoute(new RestRoutes().personEntityRoutes) // A different way to get the EndpointGroup.
                .setRoute(()->{
                    path("/index",()->{
                        get("/",ctx->ctx.render("index.html"));
                    });
                })
            .startServer(7007)
            .setCORS()
            .setGeneralExceptionHandling();
//            .setErrorHandling()
//            .setApiExceptionHandling();
    }

}
