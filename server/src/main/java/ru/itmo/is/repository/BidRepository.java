package ru.itmo.is.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.bid.Bid;

import java.util.Collection;
import java.util.List;

@Repository
public interface BidRepository extends CrudRepository<Bid, Long> {
    List<Bid> getByStatusIn(Collection<Bid.Status> status);
    List<Bid> getBySenderLoginOrderByIdDesc(String login);

    boolean existsBySenderLoginAndTypeAndStatusIn(String sender, Bid.Type type, Collection<Bid.Status> status);

    @Query("SELECT DISTINCT b.type FROM Bid b WHERE b.sender.login = :login AND (b.status = 'IN_PROCESS' or b.status = 'PENDING_REVISION')")
    List<Bid.Type> getOpenedBidTypes(@Param("login") String login);

    List<Bid> getBySenderLoginAndStatusIn(String sender, Collection<Bid.Status> status);
}
