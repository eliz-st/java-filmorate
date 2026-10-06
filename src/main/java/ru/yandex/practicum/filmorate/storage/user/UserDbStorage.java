package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository
public class UserDbStorage implements UserStorage {
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM users ORDER BY user_id";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM users WHERE user_id = ?";
    private static final String INSERT_FRIEND_QUERY =
            "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY =
            "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String DELETE_USER_FRIENDS_QUERY =
            "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
    private static final String DELETE_USER_LIKES_QUERY =
            "DELETE FROM likes WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert userInsert;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
    }

    @Override
    public User createUser(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        params.put("login", user.getLogin());
        params.put("name", user.getName());
        params.put("birthday", user.getBirthday());

        Number id = userInsert.executeAndReturnKey(params);

        user.setId(id.intValue());

        return user;
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        return user;
    }

    @Override
    public void deleteUser(int id) {
        jdbcTemplate.update(DELETE_USER_FRIENDS_QUERY, id, id);
        jdbcTemplate.update(DELETE_USER_LIKES_QUERY, id);
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<User> getUserById(int id) {
        List<User> users = jdbcTemplate.query(FIND_BY_ID_QUERY, new UserRowMapper(), id);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        user.setFriends(getUserFriends(user.getId()));

        return Optional.of(user);
    }

    @Override
    public Collection<User> getUsers() {

        List<User> users = jdbcTemplate.query(FIND_ALL_QUERY, new UserRowMapper());

        for (User user : users) {
            user.setFriends(getUserFriends(user.getId()));
        }

        return users;
    }

    public void addFriend(int userId, int friendId) {
        jdbcTemplate.update(INSERT_FRIEND_QUERY, userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    private Set<Integer> getUserFriends(int userId) {
        return new LinkedHashSet<>(
                jdbcTemplate.query(
                        FIND_FRIENDS_QUERY,
                        (rs, rowNum) -> rs.getInt("friend_id"),
                        userId));
    }
}
