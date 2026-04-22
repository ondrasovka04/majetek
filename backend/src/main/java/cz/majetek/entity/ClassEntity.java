package cz.majetek.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Dictionary entity representing an asset class.
 */
@Entity
@Table(name = "class")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @Id
    @EqualsAndHashCode.Include
    @NotBlank(message = "ID must not be blank")
    @Size(max = 10, message = "ID must not be longer than 10 characters")
    @Column(length = 10)
    private String id;

    private String name;
}