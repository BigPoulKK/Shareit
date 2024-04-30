package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@Validated
@RequestMapping("/items")
public class ItemController {
    ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemService.getItem(itemId, userId);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto item) {
        return itemService.addNewItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                          @Valid @RequestBody ItemDtoUpdate item) {

        return itemService.updateItem(itemId, userId, item);

    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        final Pageable pageable = PageRequest.of(from / size, size);
        return itemService.getUserItems(userId, pageable);
    }

    @GetMapping("/search")
    public List<ItemInfo> getItemByQueryField(@RequestParam(name = "text") String queryField,
                                              @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @RequestParam(name = "size", defaultValue = "10") Integer size) {

        final Pageable pageable = PageRequest.of(from / size, size);
        return itemService.search(queryField.toLowerCase(), pageable);
    }

    @PostMapping("/{itemId}/comment")
    public Comment getComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @Valid @RequestBody Comment comment) {

        return itemService.addComment(userId, itemId, comment);
    }
}