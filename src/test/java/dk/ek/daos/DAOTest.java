package dk.ek.daos;

import dk.ek.persistence.daos.PersonDAO;
import dk.ek.persistence.model.*;
import jakarta.persistence.EntityManagerFactory;
import dk.ek.persistence.HibernateConfig;
import org.junit.jupiter.api.*;
import rest.TestUtils;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DAOTest {
    private static EntityManagerFactory emf;
    private static PersonDAO personDao;

    Person p1, p2, p3;


    @BeforeAll
    static void setUpAll() {
        HibernateConfig.setTestMode(true);
        emf = HibernateConfig.getEntityManagerFactory();
        personDao = new PersonDAO(emf);

    }

    @AfterAll
    static void tearDownAll() {
        HibernateConfig.setTestMode(false);
    }

    @BeforeEach
    void setUp() {
        TestUtils utils = new TestUtils();
        // Create 3 users and 2 roles: user, admin and super and user and admin roles
//        utils.createUsersAndRoles(emf);
        // Populate the persons with addresses, Phone and Hobbies
        Map<String, Person> populated = utils.createPersonEntities(emf);
        p1 = (Person) populated.get("Person1");
        p2 = (Person) populated.get("Person2");
        p3 = (Person) populated.get("Person3");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Test that we can create a person")
    void create() {
        Person toBeCreated = new Person("Kurt", "Kurtson", "kurt@mail.com", LocalDate.now());
        Person person = personDao.create(toBeCreated);
        assert person.getId() != null;
    }


    @Test
    @DisplayName("Test that we can get all persons")
    void getAll() {
        assertEquals(3, personDao.getAll().size());
    }

    @Test
    @DisplayName("Test that we can get a person by id")
    void getById() {
        Person person = personDao.findById(p1.getId());
        assert person.getId() != null && person.getId().equals(p1.getId());
    }

    @Test
    @DisplayName("Test that we can update a person")
    void update() {
        Person person = personDao.findById(p1.getId());
        person.setFirstName("Hansine");
        personDao.update(person);
        Person updated = personDao.findById(p1.getId());
        assertEquals("Hansine", updated.getFirstName());
    }

    @Test
    @DisplayName("Test that we can delete a person")
    void delete() {
        Person person = personDao.findById(p1.getId());
        personDao.delete(person.getId());
        assertEquals(2, personDao.getAll().size());
    }
}