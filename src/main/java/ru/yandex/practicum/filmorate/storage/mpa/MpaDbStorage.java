package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
public class MpaDbStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE mpa_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY mpa_id";
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mpa getMpaById(int id) {
        List<Mpa> mpaList = jdbcTemplate.query(FIND_BY_ID_QUERY, new MpaRowMapper(), id);
        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA c id " + id + " не найден");
        }

        return mpaList.get(0);
    }

    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query(FIND_ALL_QUERY, new MpaRowMapper());
    }
}
