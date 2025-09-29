package dk.ek.rest;

import dk.ek.security.SecurityRoutes;

/**
 * Purpose: To demonstrate the use of security
 * Author: Thomas Hartmann
 */
public class P07SecurityDemo {
    // 1. Hashing of passwords in security.User
    // 2. Login and register in SecurityController
    // 3. Authenticate in SecurityController
    // 4. Authorize in SecurityController
    // 5. SecurityRoutes (auth and protected)
    // 6. SecurityTest with Login and token send to protected
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(SecurityRoutes.getSecurityRoutes())
                .setRoute(SecurityRoutes.getSecuredRoutes())
                .startServer(7007)
                .checkSecurityRoles()
                .setGeneralExceptionHandling();

    }
}
