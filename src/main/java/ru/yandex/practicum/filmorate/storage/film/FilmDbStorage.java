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
    private static final String FIND_ALL_LIKES_QUERY =
            "SELECT film_id, user_id " + "FROM likes " + "ORDER BY film_id, user_id";
    private static final String FIND_POPULAR_QUERY =
            "SELECT f.*, m.name AS mpa_name " + "FROM films f " + "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, " + "f.mpa_id, m.name " +
                    "ORDER BY COUNT(l.user_id) DESC, f.film_id ASC " + "LIMIT ?";
    private static final String FIND_ALL_GENRES_QUERY =
            "SELECT fg.film_id, g.genre_id, g.name " + "FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.genre_id " + "ORDER BY fg.film_id, g.genre_id";

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

        Map<Integer, Set<Genre>> genresByFilmId = getGenresByFilmIds();
        Map<Integer, Set<Integer>> likesByFilmId = getLikesByFilmIds();

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));

            film.setLikes(likesByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    private Set<Genre> getFilmGenres(int filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query(FIND_GENRES_QUERY, new GenreRowMapper(), filmId));
    }

    private void saveFilmGenres(Film film) {
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .toList();

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_GENRE_QUERY, batchArgs);
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

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_QUERY, new FilmRowMapper(), count);

        Map<Integer, Set<Genre>> genresByFilmId = getGenresByFilmIds();
        Map<Integer, Set<Integer>> likesByFilmId = getLikesByFilmIds();

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));

            film.setLikes(likesByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    private Map<Integer, Set<Genre>> getGenresByFilmIds() {
        Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();

        jdbcTemplate.query(FIND_ALL_GENRES_QUERY, rs -> {
            int filmId = rs.getInt("film_id");

            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));

            genresByFilmId.computeIfAbsent(filmId, id -> new LinkedHashSet<>()).add(genre);
        });

        return genresByFilmId;
    }

    private Map<Integer, Set<Integer>> getLikesByFilmIds() {
        Map<Integer, Set<Integer>> likesByFilmId = new HashMap<>();

        jdbcTemplate.query(FIND_ALL_LIKES_QUERY, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");

            likesByFilmId.computeIfAbsent(filmId, id -> new LinkedHashSet<>()).add(userId);
        });

        return likesByFilmId;
    }
}
