package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
public class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

    @Autowired
    public UserDbStorageTest(UserDbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    @Test
    void shouldCreateAndFindUserById() {
        User user = new User();

        user.setEmail("test@test.ru");
        user.setLogin("test_login");
        user.setName("Тестовый пользователь");
        user.setBirthday(LocalDate.of(2000, 1, 15));

        userDbStorage.createUser(user);

        Optional<User> savedUser = userDbStorage.getUserById(user.getId());

        assertTrue(savedUser.isPresent());

        User saved = savedUser.get();
        assertEquals("test@test.ru", saved.getEmail());
        assertEquals("test_login", saved.getLogin());
        assertEquals("Тестовый пользователь", saved.getName());
        assertEquals(LocalDate.of(2000, 1, 15), saved.getBirthday());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();

        user.setEmail("update@test.ru");
        user.setLogin("update_login");
        user.setName("Старое имя");
        user.setBirthday(LocalDate.of(1995, 5,10));

        userDbStorage.createUser(user);

        user.setName("Новое имя");
        userDbStorage.updateUser(user);

        Optional<User> updatedUser = userDbStorage.getUserById(user.getId());

        assertTrue(updatedUser.isPresent());
        User updated = updatedUser.get();
        assertEquals("update@test.ru", updated.getEmail());
        assertEquals("update_login", updated.getLogin());
        assertEquals("Новое имя", updated.getName());
        assertEquals(LocalDate.of(1995, 5, 10), updated.getBirthday());
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();

        user1.setEmail("first@test.ru");
        user1.setLogin("first_login");
        user1.setName("Первый");
        user1.setBirthday(LocalDate.of(1990, 1,1));

        userDbStorage.createUser(user1);

        User user2 = new User();

        user2.setEmail("second@test.ru");
        user2.setLogin("second_login");
        user2.setName("Второй");
        user2.setBirthday(LocalDate.of(1995, 2,2));

        userDbStorage.createUser(user2);

        Collection<User> users = userDbStorage.getUsers();
        assertEquals(2, users.size());
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = new User();
        user1.setEmail("friend1@test.ru");
        user1.setLogin("friend1");
        user1.setName("Первый друг");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.createUser(user1);

        User user2 = new User();
        user2.setEmail("friend2@test.ru");
        user2.setLogin("friend2");
        user2.setName("Второй друг");
        user2.setBirthday(LocalDate.of(1995, 2, 2));
        userDbStorage.createUser(user2);

        userDbStorage.addFriend(user1.getId(), user2.getId());

        Optional<User> userWithFriend =
                userDbStorage.getUserById(user1.getId());

        assertTrue(userWithFriend.isPresent());
        assertTrue(userWithFriend.get().getFriends().contains(user2.getId()));

        userDbStorage.removeFriend(user1.getId(), user2.getId());

        Optional<User> userWithoutFriend =
                userDbStorage.getUserById(user1.getId());

        assertTrue(userWithoutFriend.isPresent());
        assertFalse(userWithoutFriend.get().getFriends().contains(user2.getId()));
    }

    @Test
    void shouldDeleteUser() {
        User user = new User();
        user.setEmail("delete@test.ru");
        user.setLogin("delete_login");
        user.setName("Пользователь для удаления");
        user.setBirthday(LocalDate.of(1990, 3, 15));

        userDbStorage.createUser(user);

        userDbStorage.deleteUser(user.getId());

        Optional<User> deletedUser =
                userDbStorage.getUserById(user.getId());

        assertTrue(deletedUser.isEmpty());
    }
}