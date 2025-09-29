package dk.ek.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.*;
import dk.ek.persistence.HibernateConfig;
import dk.ek.exceptions.ApiException;
import dk.ek.exceptions.NotAuthorizedException;
import dk.ek.exceptions.ValidationException;
import dk.ek.utils.Utils;
import io.javalin.http.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityController implements ISecurityController {
    private ObjectMapper objectMapper = new ObjectMapper();
    private ITokenSecurity tokenSecurity = new TokenSecurity();
    private Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;

    private SecurityController() {
    }


    public static SecurityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new SecurityController();
        }
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        return instance;
    }

    @Override
    public void login(Context ctx) {
        ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
        try {
            UserDTO user = ctx.bodyAsClass(UserDTO.class);
            UserDTO verifiedUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
            String token = createToken(verifiedUser);

            ctx.status(200).json(returnObject
                    .put("token", token)
                    .put("username", verifiedUser.getUsername()));

        } catch (EntityNotFoundException | ValidationException e) {
            ctx.status(401);
            System.out.println(e.getMessage());
            ctx.json(returnObject.put("msg", e.getMessage()));
        }
    }

    @Override
    public void register(Context ctx) {
        ObjectNode returnObject = objectMapper.createObjectNode();
        try {
            UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
            UserDTO created = securityDAO.createUser(userInput.getUsername(), userInput.getPassword());

            String token = createToken(new UserDTO(created.getUsername(), Set.of("USER")));
            ctx.status(HttpStatus.CREATED).json(returnObject
                    .put("token", token)
                    .put("username", created.getUsername()));
        } catch (EntityExistsException e) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
            ctx.json(returnObject.put("msg", "User already exists"));
        }
    }

    /**
     * Purpose: For a user to prove who they are with a valid token
     *
     * @return
     */
    @Override
    public void authenticate(Context ctx) {
        // This is a preflight request => no need for authentication
        if (ctx.method().toString().equals("OPTIONS")) {
            ctx.status(200);
            return;
        }
        // If the endpoint is not protected with roles or is open to ANYONE role, then skip
        Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
        if (isOpenEndpoint(allowedRoles))
            return;

        // If there is no token we do not allow entry
        UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
        ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
    }


    /**
     * Purpose: To check if the Authenticated user has the rights to access a protected endpoint
     *
     * @return
     */
    @Override
    public void authorize(Context ctx) {
        Set<String> allowedRoles = ctx.routeRoles()
                .stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
        if (isOpenEndpoint(allowedRoles))
            return;
        // 2. Get user and ensure it is not null
        UserDTO user = ctx.attribute("user");
        if (user == null) {
            throw new ForbiddenResponse("No user was added from the token");
        }
        // 3. See if any role matches
        if (!userHasAllowedRole(user, allowedRoles))
            throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
    }

    @Override
    public void verify(Context ctx) {
        validateAndGetUserFromToken(ctx);
        ctx.status(200).json(objectMapper.createObjectNode().put("msg", "Token is valid"));
    }


    private @NotNull UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dk.cphbusiness.exceptions.ApiException(401, "Invalid user or token");
        }
        return verifiedTokenUser;
    }

    private static @NotNull String getToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dk.cphbusiness.exceptions.ApiException(401, "Authorization header is missing");
        }

        // If the Authorization Header was malformed, then no entry
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dk.cphbusiness.exceptions.ApiException(401, "Authorization header is malformed");

        }
        return token;
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }


    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }

    private String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            logger.error("Could not create token", e);
            throw new ApiException(500, "Could not create token");
        }
    }

    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new NotAuthorizedException(403, "Token is not valid");
            }
        } catch (ParseException | NotAuthorizedException | TokenVerificationException e) {
            logger.error("Could not create token", e);
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }
}