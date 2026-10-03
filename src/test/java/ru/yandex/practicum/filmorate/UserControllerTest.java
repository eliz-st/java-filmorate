package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private final UserStorage userStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(userStorage);
    private final UserController userController = new UserController(userService);

    private User createValidUser() {
        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("user123");
        user.setName("Елизавета");
        user.setBirthday(LocalDate.of(1998, 1, 1));
        return user;
    }

    @Test
    void shouldCreateValidUser() {
        User user = createValidUser();

        assertDoesNotThrow(() -> userController.createUser(user));
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() {
        User user = createValidUser();
        user.setEmail("useryandex.ru");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldNotCreateUserWithEmptyLogin() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldNotCreateUserWithSpaceInLogin() {
        User user = createValidUser();
        user.setLogin("user 123");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = createValidUser();
        user.setName("");

        User createdUser = userController.createUser(user);

        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    void shouldCreateUserBornToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.createUser(user));
    }

    @Test
    void shouldNotCreateUserBornInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }
}
