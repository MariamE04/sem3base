package rest;

import dk.ek.persistence.model.*;
import dk.ek.security.entities.Role;
import dk.ek.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    // method to create users and roles before each test
    public void createUsersAndRoles(EntityManagerFactory emfTest) {
        try (EntityManager em = emfTest.createEntityManager()) {
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

    public Map<String, Person> createPersonEntities(EntityManagerFactory emfTest) {
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Person").executeUpdate();
            Person p1 = new Person("Hans", "Hansen", "hans@gmail.com", LocalDate.now());
            Person p2 = new Person("Grethe", "Hansen", "grethe@gmail.com", LocalDate.now());
            Person p3 = new Person("Heksen", "Hansen", "heks@gmail.com", LocalDate.now());
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.getTransaction().commit();
            Map<String, Person> entityMap = new HashMap<>();
            return Map.of("Person1", p1 // MAX 10 entries this way (alternatively use Map.ofEntries(Map.entry("Person1", p1))
                    , "Person2", p2
                    , "Person3", p3
            );
        }
    }
}

