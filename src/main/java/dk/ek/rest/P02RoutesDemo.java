package dk.ek.rest;

import dk.ek.rest.controllers.PersonController;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;


import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Purpose: To demonstrate the use of routes and EndpointGroup.
 * Author: Thomas Hartmann
 */
public class P02RoutesDemo {
    public static void main(String[] args) {
        // A more complex web server using routes and endpoint groups
        Javalin app = Javalin.create(config -> {
                    config.router.apiBuilder(getPersonRessource());
                })
                .start(7007);
    }

    private static EndpointGroup getPersonRessource() {
        PersonController personController = new PersonController();
        return () -> {
            path("/person", () -> {
                get("/", personController.getAll());
                get("/{id}", personController.getById());
//                TODO: Covered next time on REST:
                post("/", personController.create());
                put("/{id}", personController.update());
                delete("/{id}", personController.delete());
            });
        };
    }

    private static EndpointGroup alternative() {
        PersonController pc = new PersonController();
        return () -> {
            path("/person", () -> {
                get("/", pc.getAll());
//                post("/", pc.create());
                path("/{id}", () -> {
                    get("/", pc.getById());
                    get("/name/{email}", pc.getByEmail());
//                    put("/", pc.update());
//                    delete("/", pc.delete());
                });
            });
        };
    }
}
