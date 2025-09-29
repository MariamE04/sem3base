package dk.ek.dtos;

import dk.ek.persistence.model.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String address;
    private Set<String> hobbies;

    public PersonDTO(Long id, String firstName, String lastName, String email, LocalDate birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
    }
    public PersonDTO(Person person) {
        if(person.getId()!=null)
            this.id = person.getId();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
        this.birthDate = person.getBirthDate();
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Person toEntity() {
        Person person = Person.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .birthDate(birthDate)
                .build();
        if(id!=null)
            person.setId(id);
        return person;
    }
    public static Set<PersonDTO> getEntities(Set<Person> persons) {
        return persons.stream().map(person -> new PersonDTO(person)).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "PersonDTO{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                ", address='" + address + '\'' +
                ", hobbies=" + hobbies +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null)
            return false;
        if(obj.getClass()!=this.getClass())
            return false;
        PersonDTO other = (PersonDTO) obj;
        return this.id.equals(other.id)
                && this.firstName.equals(other.firstName)
                && this.lastName.equals(other.lastName)
                && this.email.equals(other.email)
                && this.birthDate.equals(other.birthDate);
    }
}
