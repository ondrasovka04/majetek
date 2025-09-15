package cz.majetek.auth.dto;

import cz.majetek.entity.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user used in login response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    String id;
    String name;
    String username;
    UserRole role;
}
