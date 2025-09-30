package dk.security.rest;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.put;

public class SecurtiyRoutes {
    ISecurityController securityController = new SecurityController();

    public EndpointGroup getSecurityRoute = () -> {
        path("/auth",()-> {
//          before(securityController::authenticate);
//          get("/", personEntityController.getAll(), Role.ANYONE);
            //get("/", personEntityController.getAll());
            //get("/resetdata", personEntityController.resetData());
            //get("/{id}", personEntityController.getById());

            post("/login", securityController.login());
            //put("/{id}", personEntityController.update());
            //delete("/{id}", personEntityController.delete());
        });
    };
    }

