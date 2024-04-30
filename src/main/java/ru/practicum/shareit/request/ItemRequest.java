package ru.practicum.shareit.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.ItemInfo;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
@Table(schema = "public", name = "requests")
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @NotNull
    @Column(name = "description", nullable = false)
    private String description;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @PastOrPresent(message = "wrong created date")
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Transient
    private List<ItemInfo> items;
}