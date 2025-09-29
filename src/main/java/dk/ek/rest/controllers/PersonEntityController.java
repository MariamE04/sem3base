package dk.ek.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.ek.dtos.PersonDTO;
import dk.ek.persistence.daos.PersonDAO;
import dk.ek.persistence.HibernateConfig;
import dk.ek.exceptions.ApiException;
import dk.ek.persistence.model.*;
import dk.ek.rest.Populator;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.validation.BodyValidator;
import jakarta.persistence.NoResultException;
import okhttp3.Address;

/**
 * Purpose: To demonstrate rest api with Javalin and a database.
 * Author: Thomas Hartmann
 */
public class PersonEntityController implements IController {

    private static PersonEntityController instance;
    private static PersonDAO personDAO;

    private PersonEntityController() { }

    public static PersonEntityController getInstance() { // Singleton because we don't want multiple instances of the same class
        if (instance == null) {
            instance = new PersonEntityController();
        }
        // Everytime we request an instance, we get a new EMF, so we can get the proper EMF for test or prod
        personDAO = new PersonDAO(HibernateConfig.getEntityManagerFactory());
        return instance;
    }

    @Override
    public Handler getAll() {
        return ctx -> {
            ctx.status(HttpStatus.OK).json(PersonDTO.getEntities(personDAO.getAll()));
        };
    }

    @Override
    public Handler getById() {
        return ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            try {
                Person p = personDAO.findById(id);
                ctx.status(HttpStatus.OK).json(new PersonDTO(p));
            } catch(NoResultException ex){
                throw new ApiException(404, "No person with id: " + id);
            }
        };
    }

    @Override
    public Handler create() {
        return ctx -> {
            BodyValidator<PersonDTO> validator = ctx.bodyValidator(PersonDTO.class);
//            validator.check(person -> person.getAge() > 0 && person.getAge() < 120, "Age must be greater than 0 and less than 120");
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            Person created = personDAO.create(person.toEntity());
            ctx.json(new PersonDTO(created)).status(HttpStatus.CREATED);
        };
    }

    @Override
    public Handler update() {
        return ctx -> {
            Long id = (Long.parseLong(ctx.pathParam("id")));
            PersonDTO person = ctx.bodyAsClass(PersonDTO.class);
            person.setId(id);
            personDAO.update(person.toEntity());
            ctx.json(person);
        };
    }

    @Override
    public Handler delete() {
        return ctx -> {
            String id = ctx.pathParam("id");
            Person found = personDAO.findById(Long.parseLong(id));
            if(found == null)
                throw new ApiException(404, "No person with that id");
            PersonDTO person = new PersonDTO(found);
            personDAO.delete(person.toEntity().getId());
            ctx.json(person);
        };
    }
    public Handler resetData(){
        return ctx -> {
//            new Populator().createUsersAndRoles(HibernateConfig.getEntityManagerFactory());
            new Populator().createPersonEntities(HibernateConfig.getEntityManagerFactory());
            ctx.json(new ObjectMapper().createObjectNode().put("message", "Data reset"));
        };
    }
}
