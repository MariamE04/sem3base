package dk.security;

import dk.ek.exceptions.EntityNotFoundException;
import dk.ek.exceptions.ValidationException;
import dk.ek.persistence.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class SecurityDAO implements ISecurityDAO{

    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try(EntityManager em = emf.createEntityManager()){
           User foundUser =  em.find(User.class,username);
           if(foundUser.checkPassword(password)){
               return foundUser;
           } else {
               throw new ValidationException("User or password is incorrect");
           }
        }
    }

    @Override
    public User createUser(String username, String password) {
        try(EntityManager em = emf.createEntityManager()){
            User user = new User(username, password);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return user;
        }
    }

    @Override
    public Role createRole(String rolename) {
        try(EntityManager em = emf.createEntityManager()) {
           Role role = new Role(rolename);
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();

            return role;
        }
    }

    @Override
    public User addUserRole(String username, String role)  throws EntityNotFoundException {
        try(EntityManager em = emf.createEntityManager()) {
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, role);

            if (foundRole == null || foundUser == null) {
                throw new EntityNotFoundException("User or Role does not eksist");
            }
            em.getTransaction().begin();

            foundUser.addRole(foundRole);

            em.getTransaction().commit();

            return foundUser;
        }
    }

    public static void main(String[] args) throws EntityNotFoundException {
        ISecurityDAO dao= new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        dao.createUser("user2", "pass124");
        dao.createRole("User");
        dao.addUserRole("user2", "User");
    }

}
