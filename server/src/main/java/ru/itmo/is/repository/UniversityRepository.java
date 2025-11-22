package ru.itmo.is.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.dorm.University;

import java.util.Collection;
import java.util.List;

@Repository
public interface UniversityRepository extends CrudRepository<University, Integer> {
    List<University> getByIdIn(Collection<Integer> ids);

    List<University> findAllByOrderById();
}
