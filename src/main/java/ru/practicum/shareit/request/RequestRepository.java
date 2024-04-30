package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long>  {
    List<ItemRequest> findByUserId(long userId);
    @Query(value = "select * from requests where user_id <> ?1",
            nativeQuery = true)
    List<ItemRequest> findByAllRequests(Long userId, Pageable pageable);
}
