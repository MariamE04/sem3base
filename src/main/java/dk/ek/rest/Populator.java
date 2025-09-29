package dk.ek.rest;

import dk.ek.persistence.HibernateConfig;
import dk.ek.persistence.model.*;
import dk.ek.security.entities.Role;
import dk.ek.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;

/**
 * Purpose: To populate the database with users and roles
 * Author: Thomas Hartmann
 */
public class Populator {
    // method to create users and roles before each test
    public void createUsersAndRoles(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User u").executeUpdate();
            em.createQuery("DELETE FROM Role r").executeUpdate();
            User user = new User("user", "user123");
            User admin = new User("admin", "admin123");
            User superUser = new User("super", "super123");
            Role userRole = new Role("user");
            Role adminRole = new Role("admin");
            user.addRole(userRole);
            admin.addRole(adminRole);
            superUser.addRole(userRole);
            superUser.addRole(adminRole);
            em.persist(user);
            em.persist(admin);
            em.persist(superUser);
            em.persist(userRole);
            em.persist(adminRole);
            em.getTransaction().commit();
        }
    }

    public void createPersonEntities(EntityManagerFactory emf){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Person").executeUpdate();
            Person p1 = new Person("Hans", "Hansen", "hans@gmail.com", LocalDate.now());
            Person p2 = new Person("Grethe", "Hansen", "grethe@gmail.com", LocalDate.now());
            Person p3 = new Person("Heksen", "Hansen", "heks@gmail.com", LocalDate.now());
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.getTransaction().commit();

            System.out.println("Persons in DB: ");
            em.createQuery("SELECT p FROM Person p", Person.class).getResultList().forEach(System.out::println);
        }
    }

    public static void main(String[] args) {
        Populator populator = new Populator();
        populator.createUsersAndRoles(HibernateConfig.getEntityManagerFactory());
        populator.createPersonEntities(HibernateConfig.getEntityManagerFactory());
    }
}

