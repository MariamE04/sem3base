package dk.ek.security;

import dk.bugelhartmann.UserDTO;
import dk.ek.exceptions.ApiException;
import dk.ek.security.entities.Role;
import dk.ek.security.entities.User;
import dk.ek.exceptions.ValidationException;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;


/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;
    private Logger logger = LoggerFactory.getLogger(SecurityDAO.class);

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + username); //RuntimeException
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return new UserDTO(user.getUsername(), user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }
    }

    @Override
    public UserDTO createUser(String username, String password) {
        try (EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, username);
            if (userEntity != null)
                throw new EntityExistsException("User with username: " + username + " already exists");
            userEntity = new User(username, password);
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "user");
            if (userRole == null)
                userRole = new Role("user");
                em.persist(userRole);
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return new UserDTO(userEntity.getUsername(), userEntity.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }catch (Exception e){
            logger.error("Error creating user", e);
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public UserDTO addRoleToUser(String username, String role) {
        try(EntityManager em = emf.createEntityManager()){
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, role);
            em.getTransaction().begin();
            foundUser.addRole(foundRole);
            em.getTransaction().commit();
            return new UserDTO(foundUser.getUsername(), foundUser.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }
    }

    @Override
    public UserDTO removeRoleFromUser(String username, String role) {
        try(EntityManager em = emf.createEntityManager()){
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, role);
            em.getTransaction().begin();
            foundUser.removeRole(role);
            em.getTransaction().commit();
            return new UserDTO(foundUser.getUsername(), foundUser.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }
    }
}
