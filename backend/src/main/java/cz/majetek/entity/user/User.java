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

    @Column(name = "refresh_jti", length = 36)
    private String refreshJti = null;

    public User(String id, String username, String password, String name, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

} 