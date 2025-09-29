package dk.ek.rest;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purpose: To demonstrate logging with slf4j and logback
 * Author: Thomas Hartmann
 */
public class P06LoggingDemo {
    // https://mkyong.com/logging/slf4j-logback-tutorial/
    // Step 1: Add dependencies to pom.xml: slf4j-api, logback-classic
    // Step 2: Add logback.xml to src/main/resources
    // Step 3: Add logger to class
    // Step 4: Use logger
    private static final Logger logger = LoggerFactory.getLogger(P06LoggingDemo.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7007);
        app.before((ctx)->{ System.out.println("Before Hello 1"); });
        app.before((ctx)->{ System.out.println("Before Hello 2"); });
        app.get("/hello", ctx -> {
            logger.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Error Hello World");
            logger.debug("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYY Debug Hello World");
            logger.info("Just for Your Information: Hello World");
            System.out.println("Hello World");
            System.err.println("Hello ERROR World");
            ctx.result("Hello World");
        });

    }
}
