package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class GenreDbStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Genre getGenreById(int id) {
        List<Genre> genreList = jdbcTemplate.query(FIND_BY_ID_QUERY, new GenreRowMapper(), id);
        if (genreList.isEmpty()) {
            throw new NotFoundException("Жанр c id " + id + " не найден");
        }

        return genreList.get(0);
    }

    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(FIND_ALL_QUERY, new GenreRowMapper());
    }
}
