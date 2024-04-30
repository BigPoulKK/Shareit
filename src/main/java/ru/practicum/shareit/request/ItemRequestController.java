package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequest create(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemRequest itemRequest) {

        return itemRequestService.createRequest(userId, itemRequest);
    }

    @GetMapping("/{requestId}")
    public ItemRequest get(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        return itemRequestService.getRequest(userId, requestId);
    }

    @GetMapping
    public List<ItemRequest> getAllByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequest> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                    @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        final Pageable pageable = PageRequest.of(from / size, size, sort);
        return itemRequestService.getAllRequests(userId, pageable);
    }
}