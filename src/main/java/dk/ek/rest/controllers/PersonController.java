package dk.ek.rest.controllers;

import dk.ek.dtos.SimplePersonDTO;
import dk.ek.exceptions.ApiException;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.validation.BodyValidator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Purpose: To demonstrate rest api with Javalin and no database.
 * Author: Thomas Hartmann
 */
public class PersonController implements IController {
    static Map<UUID, SimplePersonDTO> persons = new HashMap(Map.of(
             UUID.randomUUID(), new SimplePersonDTO("Anders", "Henningsen", "hans@mail.com", LocalDate.of(1966, 1, 31), SimplePersonDTO.JobTitle.TEACHER)
            , UUID.randomUUID(), new SimplePersonDTO("Bente", "Henningsen", "grethe@mail.com", LocalDate.of(1976, 12, 31), SimplePersonDTO.JobTitle.TEACHER)
            , UUID.randomUUID(), new SimplePersonDTO("Carl", "Frederiksen", "jens@mail.com", LocalDate.of(1986, 2, 9), SimplePersonDTO.JobTitle.STUDENT)
            , UUID.randomUUID(), new SimplePersonDTO("Donna", "Frederiksen", "jorgen@mail.com", LocalDate.of(1996, 12, 22), SimplePersonDTO.JobTitle.STUDENT)
            , UUID.randomUUID(), new SimplePersonDTO("Erik", "Juhl", "jesper@mail.com", LocalDate.of(2006, 2, 23), SimplePersonDTO.JobTitle.ADMIN)
            , UUID.randomUUID(), new SimplePersonDTO("Frida", "Isaaksen", "jj@mail.com", LocalDate.of(2016, 12, 30), SimplePersonDTO.JobTitle.ADMIN)
    ));

    @Override
    public Handler getAll() {
        boolean isExceptionTest = false; // To provoke a 500 error for demo purposes
        return ctx -> {
            if (isExceptionTest) {
                throw new ApiException(500, "Something went wrong in the getAll method in the PersonController");
            }
            ctx.json(persons);
        };
    }

    @Override
    public Handler getById() {
        return ctx -> {
            ctx.pathParamAsClass("id", String.class)
            .check(id -> id.length() == 36, "Id must be UUID with 36 characters"); // Use a path param validator
            UUID id = UUID.fromString(ctx.pathParam("id"));
            System.out.println("ID: "+id);
            if(!persons.containsKey(id))
                throw new ApiException(404, "No person with that id");
            ctx.json(persons.get(id));
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<SimplePersonDTO> validator = ctx.bodyValidator(SimplePersonDTO.class);
//            validator.check(person -> person.getBirthday().isBefore(), "Age must be greater than 0 and less than 120");
            validator.check(person -> person.getFirstName().length() > 0, "Name must be longer than 0");
//            validator.check(person -> person.getBirthday())
            SimplePersonDTO person = ctx.bodyAsClass(SimplePersonDTO.class);
            UUID id = UUID.randomUUID();
            persons.put(id, person);
            person.setId(id);
            ctx.json(person).status(HttpStatus.CREATED);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            ctx
                    .pathParamAsClass("id", String.class) // returns a validator
                    .check(id -> id.length() == 36, "Id must be UUID with 36 characters"); // Use a path param validator
            UUID id = UUID.fromString(ctx.pathParam("id"));
            SimplePersonDTO person = ctx.bodyAsClass(SimplePersonDTO.class);
            this.persons.put(id, person);
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            UUID id = UUID.fromString(ctx.pathParam("id"));
            if(! persons.containsKey(id)){
                ctx.status(404);
                ctx.attribute("msg", String.format("No person with id: {id}", id));
                return;
            }
            SimplePersonDTO person = this.persons.remove(id);
            ctx.json(person);
        };
    }

    public Handler getByEmail() {
        return ctx -> {
            try {
                SimplePersonDTO found = persons
                        .values()
                        .stream()
                        .filter((person) -> person.getEmail().equals(ctx.pathParam("email")))
                        .toList()
                        .get(0);
                ctx.json(found).status(HttpStatus.OK);
            } catch (IndexOutOfBoundsException e) {
                throw new ApiException(404, String.format("No person with name: %s", ctx.pathParam("name")));
            }
        };
    }

        public static Map<UUID, SimplePersonDTO> getCollection(){
            return persons;
        }
}
