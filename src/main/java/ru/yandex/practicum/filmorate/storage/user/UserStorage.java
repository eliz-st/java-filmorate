package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    void deleteUser(int id);

    Optional<User> getUserById(int id);

    Collection<User> getUsers();

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);
}
