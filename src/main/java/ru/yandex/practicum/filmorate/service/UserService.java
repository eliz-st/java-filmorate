package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int userId) {
        return getUserOrThrow(userId);
    }

    public User updateUser(User user) {
        getUserOrThrow(user.getId());
        return userStorage.updateUser(user);
    }

    public void addFriend(int userId, int friendId) {

        if (userId == friendId) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(int userId, int friendId) {

        if (userId == friendId) {
            throw new ValidationException("Пользователь не может удалить самого себя из друзей");
        }

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Set<User> getFriends(int userId) {
        User user = getUserOrThrow(userId);

        Set<User> friends = new HashSet<>();

        for (Integer friendId : user.getFriends()) {
            User friend = getUserOrThrow(friendId);
            friends.add(friend);
        }

        return friends;
    }

    public Set<User> getCommonFriends(int userId, int otherId) {

        if (userId == otherId) {
            throw new ValidationException("Нельзя искать общих друзей пользователя с самим собой");
        }

        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherId);

        Set<User> commonFriends = new HashSet<>();

        for (Integer friendId : user.getFriends()) {
            if (otherUser.getFriends().contains(friendId)) {
                User commonFriend = getUserOrThrow(friendId);
                commonFriends.add(commonFriend);
            }
        }

        return commonFriends;
    }

    private User getUserOrThrow(int userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + userId + " не найден"));
    }
}
