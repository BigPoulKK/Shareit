package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ItemInfoImpl implements ItemInfo {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}