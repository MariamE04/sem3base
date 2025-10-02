package dk.security.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.ek.utils.Utils;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.security.RouteRole;
import static io.javalin.apibuilder.ApiBuilder.*;

public class SecurtiyRoutes {
    ISecurityController securityController = new SecurityController();
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();

    public EndpointGroup getSecurityRoute = () -> {
        path("/auth",()->
            post("/login", securityController.login()));

        };

    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                get("/user_demo",(ctx)->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from USER Protected")),Role.USER);
                get("/admin_demo",(ctx)->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from ADMIN Protected")),Role.ADMIN);
            });
        };
    }
    public enum Role implements RouteRole { ANYONE, USER, ADMIN }
}


