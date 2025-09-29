package dk.ek.security;

import io.javalin.http.Context;

/**
 * Purpose: To handle security in the API by making javalin handlers to use in the API routes
 * Author: Thomas Hartmann
 *
 */
public interface ISecurityController {
    void login(Context ctx); // to get a token
    void register(Context ctx); // to get a user
    void authenticate(Context ctx); // to verify roles inside token
    void authorize(Context ctx);
    void verify(Context ctx); // to verify a token
}
