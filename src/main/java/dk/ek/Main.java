package dk.ek;

import dk.ek.rest.ApplicationConfig;
import dk.ek.rest.RestRoutes;
import dk.security.rest.SecurtiyRoutes;
//import dk.ek.security.SecurityRoutes;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Main {
    public static void main(String[] args) {

        ApplicationConfig
                .getInstance()
                .initiateServer()
                  .checkSecurityRoles() // check for role when route is called
//                .setRoute(SecurityRoutes.getSecurityRoutes())
                .setRoute(new SecurtiyRoutes().getSecuredRoutes())
                .setRoute(new RestRoutes().getOpenRoutes())
                .setRoute(new RestRoutes().personEntityRoutes) // A different way to get the EndpointGroup.
                .setRoute(new SecurtiyRoutes().getSecurityRoute)
                .setRoute(()->{
                    path("/index",()->{
                        get("/",ctx->ctx.render("index.html"));
                    });
                })
                .startServer(7007)
                .setCORS()
                .setGeneralExceptionHandling();
//            .setErrorHandling()
//                .setApiExceptionHandling();
    }
}