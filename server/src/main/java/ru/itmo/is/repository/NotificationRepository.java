package ru.itmo.is.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.notification.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {

    List<Notification> getByReceiverLoginAndStatus(String receiver, Notification.Status status);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.id = :id")
    void setReadStatus(@Param("id") long id);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.receiver.login = :receiver")
    void setAllReadStatus(@Param("receiver") String receiver);
}
