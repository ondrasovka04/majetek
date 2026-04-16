package cz.majetek.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Dictionary entity representing an asset group.
 */
@Entity
@Table(name = "asset_group")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssetGroup {

    @Id
    @EqualsAndHashCode.Include
    @NotBlank(message = "ID must not be blank")
    @Size(max = 20, message = "ID must not be longer than 20 characters")
    private String id;

    private String name;
}
