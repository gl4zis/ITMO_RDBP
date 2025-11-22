package ru.itmo.is.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.user.User;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    boolean existsByLogin(String login);
    long countByRole(User.Role role);
    List<User> getUsersByRoleIn(Collection<User.Role> role);
    List<User> getByLoginIn(Collection<String> login);
}
