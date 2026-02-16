package cz.majetek.auth.mapper;


import cz.majetek.auth.dto.UserDto;
import cz.majetek.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "role", target = "role")
    UserDto toDto(User user);

    List<UserDto> toDto(List<User> users);
}