package cz.majetek.entity.user;

import jakarta.persistence.*;
import lombok.*;

/**
 * User entity representing a system user.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

} 