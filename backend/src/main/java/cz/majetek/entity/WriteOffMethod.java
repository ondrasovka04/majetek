package cz.majetek.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Dictionary entity representing a write-off method.
 */
@Entity
@Table(name = "write_off_method")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffMethod {

    @Id
    @EqualsAndHashCode.Include
    @NotBlank(message = "ID must not be blank")
    @Size(max = 20, message = "ID must not be longer than 20 characters")
    @Column(length = 20)
    private String id;

    private String name;
}
