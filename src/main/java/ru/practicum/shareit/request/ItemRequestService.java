package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRequestService {

    public ItemRequest createRequest(Long userId, ItemRequest itemRequest);

    public ItemRequest getRequest(Long userId, Long requestId);

    public List<ItemRequest> getAllRequestsByUser(Long userId);

    public List<ItemRequest> getAllRequests(Long userId, Pageable pageable);

}
