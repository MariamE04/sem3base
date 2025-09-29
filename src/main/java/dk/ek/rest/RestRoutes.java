package dk.ek.rest;

import dk.ek.rest.controllers.PersonController;
import dk.ek.rest.controllers.PersonEntityController;
import dk.ek.security.SecurityController;
import dk.ek.security.SecurityRoutes.Role;
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
    SecurityController securityController = SecurityController.getInstance();

    public EndpointGroup getOpenRoutes() {
        return () -> {
            path("open", () -> {
                path("person", () -> {
                    get("/", personController.getAll(), Role.ANYONE);
                    get("/{id}", personController.getById(), Role.ANYONE);
                    get("/email/{email}", personController.getByEmail(), Role.ANYONE);
                    post("/", personController.create(), Role.ANYONE);
                    put("/{id}", personController.update(), Role.ANYONE);
                    delete("/{id}", personController.delete(), Role.ANYONE);
                });
            });
        };
    }

    // Show a different way of getting an EndpointGroup with a lambda expression
    public EndpointGroup personEntityRoutes = ()->{
      path("/person",()-> {
          before(securityController::authenticate);
          get("/", personEntityController.getAll(), Role.ANYONE);
          get("/resetdata", personEntityController.resetData(), Role.ADMIN);
          get("/{id}", personEntityController.getById(), Role.ANYONE);

          post("/", personEntityController.create(), Role.ADMIN);
          put("/{id}", personEntityController.update(), Role.ADMIN);
          delete("/{id}", personEntityController.delete(), Role.ADMIN);
      });
    };
}