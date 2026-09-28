package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int count = 0;

    @PostMapping
    public User createUser(@RequestBody User user) {

        validateUser(user);

        user.setId(++count);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {

        validateUser(user);

        if (!users.containsKey(user.getId())) {
            log.warn("Ошибка обновления пользователя: пользователь с id {} не найден.", user.getId());
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации пользователя: введен некорректный адрес электронной почты");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации пользователя: логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации пользователя: дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
