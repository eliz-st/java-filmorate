package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int count = 0;

    @Override
    public User createUser(User user) {
        user.setId(++count);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);
        if (user != null) {
            user.getFriends().add(friendId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        if (user != null) {
            user.getFriends().remove(friendId);
        }
    }

    @Override
    public Set<User> getUsersByIds(Set<Integer> ids) {
        return users.values().stream()
                .filter(user -> ids.contains(user.getId()))
                .collect(Collectors.toSet());
    }
}
