package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessRightsError;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.TheItemHasAlreadyBeenBooked;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Status.REJECTED;

@Service
@Slf4j
@RequiredArgsConstructor
class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> getUserItems(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        List<Item> userItems = itemRepository.findByUserId(userId, pageable);
        List<ItemDto> itemDto = new ArrayList<>();
        for (Item item : userItems) {
            itemDto.add(getItem(item.getId(), userId));
        }
        log.info("get user items {}", userItems);
        return itemDto;
    }

    @Transactional
    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Item item = itemRepository.save(ItemMapper.toItem(itemDto, userId));
        log.info("add new item {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, Long userId, ItemDtoUpdate itemDtoUpdate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Item not found"));
        if (userId.equals(item.getUserId())) {
            if (itemDtoUpdate.getName() != null) {
                item.setName(itemDtoUpdate.getName());
            }
            if (itemDtoUpdate.getAvailable() != null) {
                item.setAvailable(itemDtoUpdate.getAvailable());
            }
            if (itemDtoUpdate.getDescription() != null) {
                item.setDescription(itemDtoUpdate.getDescription());
            }
            if (itemDtoUpdate.getRequestId() != null) {
                item.setRequestId(itemDtoUpdate.getRequestId());
            }
            Item itemUpdate = itemRepository.save(item);
            log.info("update item {}", itemUpdate);
            return ItemMapper.toItemDto(itemUpdate);
        }
        throw new AccessRightsError("You don't have rights");
    }

    @Transactional
    @Override
    public void deleteItem(Long userId, Long itemId) {
        itemRepository.deleteById(itemId);
        log.info("delete item {}", itemId);
    }

    @Override
    public ItemDto getItem(Long id, Long userId) throws NullPointerException {
        Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Item not found"));
        ItemDto itemDto = ItemMapper.toItemDto(item);
        if (item.getUserId().equals(userId)) {
            LocalDateTime date = LocalDateTime.now();
            log.info("Время: {}", date);
            List<Booking> bookingsPast = bookingRepository.findByItemIdPast(id, date.withNano(0), REJECTED);
            List<Booking> bookingsFuture = bookingRepository.findByItemIdFuture(id, date.withNano(0), REJECTED);
            if (bookingsPast.size() != 0) {
                itemDto.setLastBooking(BookingMapper.toBookingByBooker(bookingsPast.get(0)));
            }
            if (bookingsFuture.size() != 0) {
                if (!bookingsFuture.get(0).getStatus().equals(REJECTED)) {
                    itemDto.setNextBooking(BookingMapper.toBookingByBooker(bookingsFuture.get(0)));
                }
            }
        }
        itemDto.setComments(commentRepository.findByItemId(id));
        log.info("get item {}", item);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemInfo> search(String text, Pageable pageable) {
        if (!text.isBlank()) {
            log.info("search {}", text);
            return itemRepository.findItemsWhereContainsTheText(text, pageable);
        }
        return new ArrayList<>();
    }

    @Override
    public Comment addComment(Long userId, Long itemId, Comment comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Item not found"));
        LocalDateTime date = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findBookingPastItemsByOwner(item.getUserId(), date, PageRequest.of(0, 10));
        for (Booking booking : bookings) {
            if (booking.getBooker().getId().equals(userId)) {
                comment.setItem(item);
                comment.setUser(user);
                comment.setCreated(date);
                comment.setAuthorName(user.getName());
                return commentRepository.save(comment);
            }
        }
        throw new TheItemHasAlreadyBeenBooked("You can't book this thing");
    }

}