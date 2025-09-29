package dk.ek.security.entities;

/**
 * Purpose: to set methods for handling security with the user
 * Author: Thomas Hartmann
 */
public interface ISecurityUser {
    boolean verifyPassword(String pw);
    void addRole(Role role);
}
