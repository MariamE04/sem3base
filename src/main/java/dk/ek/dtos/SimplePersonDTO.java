package dk.ek.dtos;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimplePersonDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthday;
    private JobTitle jobTitle;

    public SimplePersonDTO(String firstName, String lastName, String email, LocalDate birthday, JobTitle jobTitle) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthday = birthday;
        this.jobTitle = jobTitle;
    }

    public enum JobTitle {
        TEACHER, STUDENT, ADMIN
    }
}
