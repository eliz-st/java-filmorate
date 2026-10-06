package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;


import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "WHERE f.film_id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "ORDER BY f.film_id";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                    "WHERE film_id = ?";
    private static final String DELETE_QUERY =
            "DELETE FROM films WHERE film_id = ?";
    private static final String FIND_GENRES_QUERY =
            "SELECT g.genre_id, g.name " +
                    "FROM genres g " +
                    "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.genre_id";
    private static final String INSERT_GENRE_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";
    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_QUERY =
            "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String DELETE_ALL_LIKES_QUERY =
            "DELETE FROM likes WHERE film_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert filmInsert;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
    }

    @Override
    public Film createFilm(Film film) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpa_id", film.getMpa().getId());

        Number id = filmInsert.executeAndReturnKey(params);

        film.setId(id.intValue());

        saveFilmGenres(film);

        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update(DELETE_GENRES_QUERY, film.getId());

        saveFilmGenres(film);

        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    public void deleteFilm(int id) {
        jdbcTemplate.update(DELETE_GENRES_QUERY, id);
        jdbcTemplate.update(DELETE_ALL_LIKES_QUERY, id);
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        List<Film> films = jdbcTemplate.query(FIND_BY_ID_QUERY, new FilmRowMapper(), id);

        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        film.setGenres(getFilmGenres(film.getId()));

        film.setLikes(getFilmLikes(film.getId()));

        return Optional.of(film);
    }

    @Override
    public Collection<Film> getFilms() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY, new FilmRowMapper());
        for (Film film : films) {
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(getFilmLikes(film.getId()));
        }
        return films;
    }

    private Set<Genre> getFilmGenres(int filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query(FIND_GENRES_QUERY, new GenreRowMapper(), filmId));
    }

    private void saveFilmGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(
                    INSERT_GENRE_QUERY,
                    film.getId(),
                    genre.getId());
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    private Set<Integer> getFilmLikes(int filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query(FIND_LIKES_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"), filmId));
    }
}
