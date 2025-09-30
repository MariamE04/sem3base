package dk.security;

import javax.management.relation.Role;
import java.util.Set;

public interface ISecurityUser {
    boolean verifyPassword(String pw);
    void addRole(Role role);
    void removeRole(String role);
}