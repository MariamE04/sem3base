package dk.ek.rest;

import io.javalin.Javalin;

/**
 * Purpose: To demonstrate rest api with Javalin and a database.
 * Author: Thomas Hartmann
 */
public class P01SimpleDemo {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7007);
        app.get("/hello", ctx -> ctx.result("Hello World"));
        app.get("/hello/{name}", ctx -> ctx.result("Hello " + ctx.pathParam("name")));
    }
}
