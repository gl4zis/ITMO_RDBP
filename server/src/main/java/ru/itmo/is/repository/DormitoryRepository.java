package ru.itmo.is.repository;

import org.springframework.data.repository.CrudRepository;
import ru.itmo.is.entity.dorm.Dormitory;

import java.util.List;

public interface DormitoryRepository extends CrudRepository<Dormitory, Integer> {
    List<Dormitory> findAllByOrderById();
}
