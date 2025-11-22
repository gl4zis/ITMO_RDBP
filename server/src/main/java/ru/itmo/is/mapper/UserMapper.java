package ru.itmo.is.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.itmo.is.dto.request.RegisterRequest;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.PasswordManager;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "password", qualifiedByName = "hash")
    User toUser(RegisterRequest req);

    @Named("hash")
    default String hash(String source) {
        return PasswordManager.hash(source);
    }
}
