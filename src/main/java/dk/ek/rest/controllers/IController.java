package dk.ek.rest.controllers;

import io.javalin.http.Handler;

/**
 * Purpose: This interface is used to determine which methods a controller must implement
 * Author: Thomas Hartmann
 */
public interface IController {

    Handler getAll();
    Handler getById();
    Handler create();
    Handler update();
    Handler delete();
}
