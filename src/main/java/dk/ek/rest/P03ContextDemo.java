package dk.ek.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.ek.dtos.SimplePersonDTO;
import dk.ek.utils.Utils;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Handler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.path;

/**
 * Purpose: To demonstrate the use of the context object.
 * Author: Thomas Hartmann
 */
public class P03ContextDemo {
    private static ContextDemoController cdc = new ContextDemoController();
    public static void main(String[] args) {
        Javalin app = Javalin.create(config-> { // create method takes a Consumer<JavalinConfig> as argument to configure the server
            config.bundledPlugins.enableDevLogging();
            config.http.defaultContentType = "application/json";
            config.router.contextPath = "/demoapp/api";
            config.router.apiBuilder(getRequestDemoRoutes());
            config.router.apiBuilder(getResponseDemoRoutes());
            // For wednesday:
//          config.plugins.register(new RouteOverviewPlugin("/routes")); // overview of all registered routes at /routes for api documentation
//          config.accessManager(new AccessManager() { @Override public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<? extends RouteRole> set) throws Exception { throw new UnsupportedOperationException("Not implemented yet"); } });
        }).start(7007);
    }
    private static EndpointGroup getRequestDemoRoutes() {
        return () -> {
            path("/context_request_demo", () -> {
                get("/", cdc.getDemo());
                // pathParam("name")                     // path parameter by name as string
                get("/pathparam/{name}", cdc.getPathParamDemo());
                // header("name")                        // request header by name (can be used with Header.HEADERNAME)
                get("/header", cdc.getHeaderDemo());
                // queryParam("name")                    // query parameter by name as string
                get("/queryparam", cdc.getQueryParamDemo());
                // req()                                 // get the underlying HttpServletRequest
                get("/req", cdc.getRequestDemo());
                // WEDNESDAY:
                // bodyasclass
                post("/bodyasclass", cdc.getBodyAsClassDemo());
                // bodyValidator
                post("/bodyvalidator", cdc.getBodyValidatorDemo());
                });
        };
    }
    private static EndpointGroup getResponseDemoRoutes() {
        // Things to do with the http result before we send it of to the client:
        return () -> {
            path("/context_response_demo", () -> {
                // contentType("type")                   // set the response content type
                get("/contenttype", cdc.getContentTypeDemo());
                //header("name", "value")               // set response header by name (can be used with Header.HEADERNAME)
                get("/header", cdc.setHeaderDemo());
                //status(code)                          // set the response status code
                get("/status", cdc.setStatusDemo());
                //json(obj)                             // calls result(jsonString), and also sets content type to json
                get("/json", cdc.setJsonDemo());
                //html("html")                          // calls result(string), and also sets content type to html
                get("/html", cdc.setHtmlDemo());
                //render("/template.tmpl", model)       // calls html(renderedTemplate)
                get("/render", cdc.setRenderDemo());
            });
        };
    }

    // Controller class to provide Handlers
    private static class ContextDemoController {
        private ObjectMapper objectMapper = new Utils().getObjectMapper();
        public Handler getDemo(){
            return ctx -> {
                ObjectNode json = objectMapper.createObjectNode();
                json.put("message", "Hello World");
                ctx.json(json);
            };
        }

        public Handler getPathParamDemo(){
            return ctx -> {
                String name = ctx.pathParam("name");
                ObjectNode json = objectMapper.createObjectNode();
                json.put("name", name);
                json.put("message","GetPathParamDemo");
                ctx.json(json);
            };
        }

        public Handler getHeaderDemo(){
            return ctx -> {
                String header = ctx.header("X-EXAMPLE-HEADER");
                Map<String, String> headers =  ctx.headerMap();
                System.out.println("GET HEADER DEMO: "+headers);
                ObjectNode json = objectMapper.createObjectNode();
                json.put("header", header);
                json.put("message","GetHeaderDemo");
                ctx.json(json);
            };
        }

        public Handler getQueryParamDemo(){
            return ctx -> {
                String queryParam = ctx.queryParam("qp");
                ObjectNode json = objectMapper.createObjectNode();
                json.put("queryParam", queryParam);
                json.put("message","GetQueryParamDemo");
                ctx.json(json);
            };
        }

        public Handler getRequestDemo(){
            return ctx -> {
                HttpServletRequest request = ctx.req();
                System.out.println("REQUEST: "+request);
                ObjectNode json = objectMapper.createObjectNode();
                json.put("message","GetRequestDemo");
                ctx.json(json);
            };
        }

        public Handler getBodyAsClassDemo(){
            return ctx -> {
                SimplePersonDTO person = ctx.bodyAsClass(SimplePersonDTO.class);
                ObjectNode json = objectMapper.createObjectNode();
                json.put("person", objectMapper.valueToTree(person));
                json.put("message","GetBodyAsClassDemo");
                ctx.json(person);
            };
        }

        public Handler getBodyValidatorDemo(){
            return ctx -> {
                SimplePersonDTO person = ctx.bodyValidator(SimplePersonDTO.class)
                        .check(p -> p.getLastName() != null && p.getLastName().length() > 0, "Name cannot be null or empty")
                        .check(p -> p.getBirthday().isBefore(LocalDate.now()) , "Age must be a positive number")
                        .get();
                ObjectNode json = objectMapper.createObjectNode();
                json.put("person", json);
                json.put("message","GetBodyValidatorDemo");
                ctx.json(person);
            };
        }

        // RESPONSE DEMO
        public Handler getContentTypeDemo(){
            return ctx -> {
                HttpServletResponse response = ctx.res();
                response.setContentType("application/json");
                System.out.println("CONTENT TYPE: "+response.getContentType());
                ObjectNode json = objectMapper.createObjectNode();
                json.put("contentType", response.getContentType());
                json.put("message","GetContentTypeDemo");
                ctx.json(json);
            };
        }
        public Handler setHeaderDemo(){
            return ctx -> {
                HttpServletResponse response = ctx.res();
                response.setHeader("X-EXAMPLE-HEADER", "Hello World");
                System.out.println(response.getHeader("X-EXAMPLE-HEADER"));
                ObjectNode json = objectMapper.createObjectNode();
                json.put("message","SetHeaderDemo");
                ctx.json(json);
            };
        }
        public Handler setStatusDemo(){
            return ctx -> {
                HttpServletResponse response = ctx.res();
                response.setStatus(418);
                System.out.println(response.getStatus());
                ObjectNode json = objectMapper.createObjectNode();
                json.put("message","SetStatusDemo");
                ctx.json(json);
            };
        }
        public Handler setJsonDemo(){
            return ctx -> {
                ObjectNode json = objectMapper.createObjectNode();
                json.put("message","SetJsonDemo");
                ctx.json(json);
            };
        }
        public Handler setHtmlDemo(){
            return ctx -> {
                ctx.html("<html><head><title>Hello World Page</title><style>body {background-color:black; color:white;}</style></head><body><h1>Hello to you World</h1><img src=\"https://images.pexels.com/photos/87651/earth-blue-planet-globe-planet-87651.jpeg\" width=\"1000\"></body></html>");
            };
        }
        public Handler setRenderDemo(){
            return ctx -> {
                // This file must be in folder /src/main/jte and 2 maven dependencies are necessary: jte and javalin-rendering. See pom.xml
                ctx.render("template.jte");
            };
        }
    }

}
