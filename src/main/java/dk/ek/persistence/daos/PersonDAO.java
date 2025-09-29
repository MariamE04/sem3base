package dk.ek.persistence.daos;


import dk.ek.exceptions.EntityNotFoundException;
import dk.ek.persistence.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose: This class is a specific DAO (Data Access Object) that can be used to perform CRUD operations on the Person entity plus some extra queries.
 * @param <T> The entity class that the DAO should be used for.
 * Author: Thomas Hartmann
 */
public class PersonDAO implements IDAO<Person>{
    private final EntityManagerFactory emf;

    public PersonDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    public List<Person> getAllByZip(Integer zip) {
        try (EntityManager entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            List<Person> persons = entityManager.createQuery("SELECT p FROM Person p LEFT JOIN p.address address WHERE address IS NOT NULL OR address.zip.zip = :zip", Person.class)
                    .setParameter("zip", zip)
                    .getResultList();
            entityManager.getTransaction().commit();
            return persons;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }





    public Person getPersonByEmail(String mail) {
        try (EntityManager entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            Person person = entityManager.createQuery("SELECT p FROM Person p WHERE p.email = :mail", Person.class)
                    .setParameter("mail", mail)
                    .getSingleResult();
            entityManager.getTransaction().commit();
            return person;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }


    @Override
    public Person findById(Long id) {
        return emf
                .createEntityManager()
                .createQuery("SELECT p FROM Person p WHERE p.id = :id", Person.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public Set<Person> getAll() {
        return emf
                .createEntityManager()
                .createQuery("SELECT p FROM Person p", Person.class)
                .getResultStream().collect(Collectors.toSet());
    }

    @Override
    public Person create(Person person) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
            return person;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Person update(Person person) {
        try(EntityManager em = emf.createEntityManager()){
            Person found = em.find(Person.class, person.getId());
            if(found == null)
                throw new EntityNotFoundException("No person with id: " + person.getId());
            em.getTransaction().begin();
            Person merged = em.merge(person);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        try(EntityManager em = emf.createEntityManager()){
            Person found = em.find(Person.class, id);
            if(found == null)
                throw new EntityNotFoundException("No person with id: " + id);
            em.getTransaction().begin();
            em.remove(found);
            em.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}