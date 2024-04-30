package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemInfo;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestRepository requestRepository;

    @Override
    public ItemRequest createRequest(Long userId, ItemRequest itemRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now().withNano(0));
        return requestRepository.save(itemRequest);
    }

    @Override
    public ItemRequest getRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Request not found"));
        List<ItemInfo> items = itemRepository.findByRequestId(requestId);
        itemRequest.setItems(items);
        return itemRequest;
    }

    @Override
    public List<ItemRequest> getAllRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        List<ItemRequest> itemRequest = requestRepository.findByUserId(userId);
        List<ItemRequest> requests = new ArrayList<>();
        if (itemRequest.size() != 0) {
            for (ItemRequest request : itemRequest) {
                List<ItemInfo> items = itemRepository.findByRequestId(request.getId());
                request.setItems(items);
                requests.add(request);
            }
        }
        return requests;
    }

    @Override
    public List<ItemRequest> getAllRequests(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        List<ItemRequest> itemRequest = requestRepository.findByAllRequests(userId, pageable);
        List<ItemRequest> requests = new ArrayList<>();
        if (itemRequest.size() != 0) {
            for (ItemRequest request : itemRequest) {
                List<ItemInfo> items = itemRepository.findByRequestId(request.getId());
                request.setItems(items);
                requests.add(request);
            }
        }
        return requests;
    }
}