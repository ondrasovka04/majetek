package cz.majetek.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "write_off_method")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffMethod {

    @Id
    @NotBlank(message = "ID must not be blank")
    @Size(max = 20, message = "ID must not be longer than 20 characters")
    private String id;

    private String name;
}
