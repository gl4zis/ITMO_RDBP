package ru.itmo.is.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.bid.BidFile;

import java.util.List;

@Repository
public interface BidFileRepository extends CrudRepository<BidFile, String> {
    List<BidFile> getByKeyIn(List<String> keys);
}
