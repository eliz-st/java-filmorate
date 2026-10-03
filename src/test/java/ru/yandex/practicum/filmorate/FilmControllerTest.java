package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private final FilmStorage filmStorage = new InMemoryFilmStorage();
    private final UserStorage userStorage = new InMemoryUserStorage();

    private final FilmService filmService = new FilmService(filmStorage, userStorage);
    private final FilmController filmController = new FilmController(filmService);

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);
        return film;
    }

    @Test
    void shouldNotCreateFilmWithEmptyName() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldCreateFilmWithDescriptionOf200Characters() {
        Film film = createValidFilm();

        film.setDescription("а".repeat(200));

        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    void shouldNotCreateFilmWithDescriptionOver200Characters() {
        Film film = createValidFilm();

        film.setDescription("а".repeat(201));

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldCreateFilmReleasedOnDecember28_1895() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    void shouldNotCreateFilmReleasedBeforeDecember28_1895() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldCreateFilmWithPositiveDuration() {
        Film film = createValidFilm();
        film.setDuration(1);

        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    void shouldNotCreateFilmWithZeroDuration() {
        Film film = createValidFilm();
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldNotCreateFilmWithNegativeDuration() {
        Film film = createValidFilm();
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

}
