package dk.ek.rest;

import dk.ek.rest.controllers.PersonController;
import dk.ek.rest.controllers.PersonEntityController;
//import dk.ek.security.SecurityController;
//import dk.ek.security.SecurityRoutes.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To demonstrate the use of unprotected routes and protected ones
 *
 * Author: Thomas Hartmann
 */
public class RestRoutes {
    PersonController personController = new PersonController(); // IN memory person collection
    PersonEntityController personEntityController = PersonEntityController.getInstance(); // Person collection in DB

    public EndpointGroup getOpenRoutes() {
        return () -> {
            path("open", () -> {
                path("person", () -> {
                    get("/", personController.getAll());
                    get("/{id}", personController.getById());
                    get("/email/{email}", personController.getByEmail());
                    post("/", personController.create());
                    put("/{id}", personController.update());
                    delete("/{id}", personController.delete());
                });
            });
        };
    }

    // Show a different way of getting an EndpointGroup with a lambda expression
    public EndpointGroup personEntityRoutes = ()->{
      path("/person",()-> {
//          before(securityController::authenticate);
//          get("/", personEntityController.getAll(), Role.ANYONE);
          get("/", personEntityController.getAll());
          get("/resetdata", personEntityController.resetData());
          get("/{id}", personEntityController.getById());

          post("/", personEntityController.create());
          put("/{id}", personEntityController.update());
          delete("/{id}", personEntityController.delete());
      });
    };
}