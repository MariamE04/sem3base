package rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.ek.dtos.PersonDTO;
import dk.ek.dtos.SimplePersonDTO;
import dk.ek.persistence.HibernateConfig;
import dk.ek.persistence.model.Person;
import dk.ek.rest.ApplicationConfig;
import dk.ek.rest.RestRoutes;
import dk.ek.rest.controllers.PersonController;
import dk.ek.security.SecurityRoutes;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@Disabled
class PersonHandlerTest {

    private static ApplicationConfig appConfig;
    private static final String BASE_URL = "http://localhost:7777/api";
    private static EntityManagerFactory emfTest;
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static String securityToken;
    private Map<String, Person> entities;
    private Map<UUID, SimplePersonDTO> simplePersons;


    @BeforeAll
    static void setUpAll() {
        RestAssured.baseURI = BASE_URL;
        jsonMapper.findAndRegisterModules(); // needed with pom dependency for jackson to use java 8 LocalDate: https://mkyong.com/java/jackson-java-8-date-time-type-java-time-localdate-not-supported-by-default/

        HibernateConfig.setTestMode(true); // IMPORTANT leave this at the very top of this method in order to use the test database
        RestRoutes restRoutes = new RestRoutes();

        // Setup test database using docker testcontainers
        emfTest = HibernateConfig.getEntityManagerFactory();

        // Start server
        appConfig = ApplicationConfig.
                getInstance()
                .initiateServer()
                .checkSecurityRoles()
//                .setErrorHandling() // This one overrules the setApiExeptionHandling
                .setGeneralExceptionHandling()
                .setRoute(restRoutes.getOpenRoutes())
                .setRoute(SecurityRoutes.getSecurityRoutes())
                .setRoute(SecurityRoutes.getSecuredRoutes())
                .setRoute(restRoutes.personEntityRoutes) // A different way to get the EndpointGroup. Getting data from DB
                .setCORS()
//                .setApiExceptionHandling()
                .startServer(7777)
        ;
    }

    @AfterAll
    static void afterAll() {
        HibernateConfig.setTestMode(false);
        appConfig.stopServer();
    }

    @BeforeEach
    void setUpEach() {
        // Setup test database for each test
        new TestUtils().createUsersAndRoles(emfTest);
        // Setup DB Persons and Addresses
        entities = new TestUtils().createPersonEntities(emfTest);
        simplePersons = PersonController.getCollection();
    }

    private static void login(String username, String password) {
        ObjectNode objectNode = jsonMapper.createObjectNode()
                .put("username", username)
                .put("password", password);
        String loginInput = objectNode.toString();
        securityToken = given()
                .contentType("application/json")
                .body(loginInput)
                //.when().post("/api/login")
                .when().post("/auth/login")
                .then()
                .extract().path("token");
        System.out.println("TOKEN ---> " + securityToken);
    }

    @Test
    @DisplayName("Hul igennem")
    public void testServerIsUp() {
        given().when().get("/open/person").peek().then().statusCode(200);
    }

    @Test
    @DisplayName("Get first person")
    void getOne() {

        given()
//                .header("Authorization", adminToken)
                .contentType("application/json")
                .when()
                .get("/person/" + entities.get("Person1").getId())
                .then()
                .assertThat()
                .statusCode(200)
                .body("firstName", equalTo("Hans"))
                .body("lastName", equalTo("Hansen"));
    }

    @Test
    @DisplayName("Get All Persons")
    void getAll() {

        given()
                .contentType("application/json")
                .when()
                .get("/person")
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(3));
    }

    @Test
    @DisplayName("Get all persons check first person")
    void testAllBody() {
        UUID uuid = PersonController.getCollection().keySet().iterator().next();
        SimplePersonDTO personDTO = PersonController.getCollection().get(uuid);
        // given, when, then
        given()
                .when()
                .get("/open/person")
                .prettyPeek()
                .then()
                .statusCode(200)
                .body(uuid + ".firstName", is(personDTO.getFirstName()));
    }

    @Test
    @DisplayName("Json PATH and DTOs")
    void testAllBody4() {
//        jsonMapper.findAndRegisterModules(); // needed with pom dependency for jackson to use java 8 LocalDate: https://mkyong.com/java/jackson-java-8-date-time-type-java-time-localdate-not-supported-by-default/
        Response response = given()
                .when()
                .get("/open/person");
        JsonPath jsonPath = response.jsonPath();

        // Get the map of persons from the outer json object
        Map<String, Map<String, Object>> personsMap = jsonPath.getMap("$");
        System.out.println("PERSON MAP: " + personsMap);

        // Convert the map of persons to an array of SimplePersonDTO
        SimplePersonDTO[] persons = personsMap.values()
                .stream()
                .map(personMap -> jsonMapper.convertValue(personMap, SimplePersonDTO.class))
                .toArray(SimplePersonDTO[]::new);

        assertEquals(6, persons.length);
    }

    @Test
    @DisplayName("Test logging request")
    public void testLogRequest() {
        System.out.println("Testing logging request details");
        given()
                .log().all()
                .when().get("/open/person")
                .then().statusCode(200);
    }

    @Test
    public void testLogResponse() {
        System.out.println("Testing logging response details");
        given()
                .when().get("/open/person")
                .then().log().body().statusCode(200);
    }

    @Test
    @DisplayName("Test get a peron by id")
    public void testGetById() {
        Person person = (Person) entities.get("Person3");
        given()
                .contentType(ContentType.JSON)
//                .pathParam("id", p1.getId()).when()
                .get("/person/{id}", person.getId())
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK_200)
                .body("id", equalTo(person.getId().intValue()))
                .body("firstName", equalTo(person.getFirstName()));
    }

    @Test
    @DisplayName("Test get a 404 status code when person not found")
    public void testError() {
        given()
                .contentType(ContentType.JSON)
//                .pathParam("id", p1.getId()).when()
                .get("/person/{id}", 999999999)
                .then()
                .log().body()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404);
//                .body("code", equalTo(404))
//                .body("message", equalTo("The Parent entity with ID: 999999999 Was not found"));
    }

    @Test
    @DisplayName("Test logging response")
    public void testPrintResponse() {
        Person person = (Person) entities.get("Person3");
        Response response = given().when().get("/person/" + person.getId());
        ResponseBody body = response.getBody();
        System.out.println(body.prettyPrint());

        response
                .then()
                .assertThat()
                .body("firstName", equalTo(person.getFirstName()));
    }

    @Test
    @DisplayName("Test using JsonPath to get a value from a response")
    public void exampleJsonPathTest() {
        Person person = (Person) entities.get("Person3");
        Response res = given().get("/person/" + person.getId());
        assertEquals(200, res.getStatusCode());
        String json = res.asString();
        JsonPath jsonPath = new JsonPath(json);
        assertEquals(person.getEmail(), jsonPath.get("email"));
    }

    @Test
    @DisplayName("Test converting json path to DTOs")
    public void getAllParents() throws Exception {
        Person p1 = (Person) entities.get("Person1");
        Person p2 = (Person) entities.get("Person2");
        List<PersonDTO> personsDTOs;

        personsDTOs = given()
                .contentType("application/json")
                .when()
                .get("/person")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("", PersonDTO.class);

        PersonDTO p1DTO = new PersonDTO(p1);
        PersonDTO p2DTO = new PersonDTO(p2);
        assertThat(personsDTOs, hasItems(p1DTO, p2DTO)); // remember to override equals in PersonDTO

    }


    @Test
    @DisplayName("Test POST of Entity Person to Database")
    public void postTest() throws JsonProcessingException {
        login("admin", "admin123");
        Person p1 = new Person("Helge", "Hansen","mail@mail.com", LocalDate.of(2002, 4, 23 ));
        PersonDTO pdto = new PersonDTO(p1);
//        jsonMapper.findAndRegisterModules(); // In order to use java 8 LocalDate
        String requestBody = jsonMapper.writeValueAsString(pdto);
        System.out.println("REQUEST BODY: " + requestBody);

        given()
                .accept("application/json")
                .header("Authorization", "Bearer "+securityToken)
                .header("Content-type", ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/person")
                .then()
                .assertThat()
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("Helge"))
                .body("email", equalTo("mail@mail.com"));
    }

    @Test
    @DisplayName("Test PUT of Person No Security")
    public void updateTest() throws JsonProcessingException {
        UUID key = simplePersons.keySet().iterator().next();
        SimplePersonDTO p1 = simplePersons.get(key);

        p1.setLastName("Habermas");
//        jsonMapper.findAndRegisterModules();
        String requestBody = jsonMapper.writeValueAsString(p1);

        given()
                .log().all()
                .header("Content-type", ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/open/person/"+key)
                .then()
                .log().body()
                .assertThat()
                .statusCode(200)
//                .body("id", equalTo(key.toString()))
                .body("firstName", equalTo(p1.getFirstName()))
                .body("email", equalTo(p1.getEmail()));
    }

    @Test
    @DisplayName("Test DELETE of Person")
    public void testDeletePerson() {
        UUID key = simplePersons.keySet().iterator().next();
        SimplePersonDTO p1 = simplePersons.get(key);

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .pathParam("id", key.toString())
                .delete("/open/person/{id}")
                .then()
                .log().body()
                .statusCode(200);
//                .body("id",equalTo(p2.getId()));
    }

    // More test tools from: https://www.baeldung.com/java-junit-hamcrest-guide
    @Test
    @DisplayName("Test hamcrest matcher: hasSize")
    public void testListSize() {
        System.out.println("Check size of list");
        List<String> hamcrestMatchers = Arrays.asList(
                "collections", "beans", "text", "number");
        assertThat(hamcrestMatchers, hasSize(4));
    }

    @Test
    @DisplayName("Test hamcrest matcher: hasProperty")
    public void testPropAndValue() {
        System.out.println("Check for property and value on an entity instance");
        Person person = new Person("Benjamin", "Henriksen", "henriksen@mail.com", LocalDate.of(2006, 2, 23));
        assertThat(person, hasProperty("lastName", equalTo("Henriksen")));
    }

    @Test
    @DisplayName("Test hamcrest matcher: samePropertyValuesAs")
    public void testCompareObjects() {
        System.out.println("Check if 2 instances has same property values (EG. use compare properties rather than objects");
        Person person1 = new Person("Abraham", "Henriksen", "a-henriksen@mail.com", LocalDate.of(2006, 2, 23));
        Person person2 = new Person("Benjamin", "Henriksen", "b-henriksen@mail.com", LocalDate.of(2006, 2, 23));
        Person person3 = new Person("Abraham", "Henriksen", "a-henriksen@mail.com", LocalDate.of(2006, 2, 23));
        assertThat(person1, samePropertyValuesAs(person3));
    }

    @Test
    @DisplayName("Test hamcrest matcher: hasToString")
    public void testToString() {
        System.out.println("Check if obj.toString() creates the right output");
        Person person = new Person("Abraham", "Henriksen", "a-henriksen@mail.com", LocalDate.of(2006, 2, 23));
        String str = person.toString();
        assertThat(person, hasToString(str));
    }

    @Test
    @DisplayName("Test hamcrest matcher: arrayContaining")
    public void testMapContains() {
        List<Person> parents = Arrays.asList(
                new Person("Abraham", "Henriksen", "a-henriksen@mail.com", LocalDate.of(2006, 2, 23)),
                new Person("Abraham", "Henriksen", "a-henriksen@mail.com", LocalDate.of(2006, 2, 23))
                );
        assertThat(parents.toArray(), arrayContainingInAnyOrder(parents.get(0), parents.get(1)));
    }

    @Test
    @DisplayName("Test hamcrest matcher: closeTo, greaterThanOrEqualTo, everyItem")
    public void testNumeric() {
        System.out.println("Test numeric values");
        assertThat(1.2, closeTo(1, 0.5));
        assertThat(5, greaterThanOrEqualTo(5));

        List<Integer> list = Arrays.asList(1, 2, 3);
        int baseCase = 0;
        assertThat(list, everyItem(greaterThan(baseCase)));
    }

    @Test
    @DisplayName("Test hamcrest matcher: not, is, containsString")
    public void testMoreReadable() {
        System.out.println("Use the IS, NOT etc keywords for readability");
        String str1 = "text";
        String str2 = "texts";
        String str3 = "texts";
        String str4 = "These are several texts in one sentence";
        assertThat(str1, not(str2));
        assertThat(str2, is(str3));
        assertThat(str4, containsString(str2));
    }

}
