package dk.security;

import dk.ek.exceptions.EntityNotFoundException;
import dk.ek.exceptions.ValidationException;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException; // used for login
    User createUser(String username, String password); // used for register
    Role createRole(String role);
    User addUserRole(String username, String role) throws EntityNotFoundException;
}