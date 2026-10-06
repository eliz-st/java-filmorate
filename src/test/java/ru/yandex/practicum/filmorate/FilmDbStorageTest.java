package ru.yandex.practicum.filmorate;
import ru.yandex.practicum.filmorate.model.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
public class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Autowired
    public FilmDbStorageTest(FilmDbStorage filmDbStorage,
                             UserDbStorage userDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    @Test
    void shouldCreateAndFindFilmById() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание тестового фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        film.getGenres().add(genre);

        Film createdFilm = filmDbStorage.createFilm(film);

        Film savedFilm = filmDbStorage.getFilmById(createdFilm.getId())
                .orElseThrow();

        assertEquals("Тестовый фильм", savedFilm.getName());
        assertEquals("Описание тестового фильма", savedFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), savedFilm.getReleaseDate());
        assertEquals(120, savedFilm.getDuration());

        assertEquals(1, savedFilm.getMpa().getId());
        assertEquals("G", savedFilm.getMpa().getName());

        assertEquals(1, savedFilm.getGenres().size());
        assertTrue(savedFilm.getGenres().stream()
                .anyMatch(g -> g.getId() == 1 && "Комедия".equals(g.getName())));
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Старое название");
        film.setDescription("Старое описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        film.getGenres().add(genre);

        Film createdFilm = filmDbStorage.createFilm(film);

        createdFilm.setName("Новое название");
        createdFilm.setDescription("Новое описание");
        createdFilm.setDuration(150);

        Mpa newMpa = new Mpa();
        newMpa.setId(4);
        createdFilm.setMpa(newMpa);

        createdFilm.getGenres().clear();

        Genre newGenre = new Genre();
        newGenre.setId(2);
        createdFilm.getGenres().add(newGenre);

        filmDbStorage.updateFilm(createdFilm);

        Film updatedFilm = filmDbStorage.getFilmById(createdFilm.getId())
                .orElseThrow();

        assertEquals("Новое название", updatedFilm.getName());
        assertEquals("Новое описание", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());

        assertEquals(4, updatedFilm.getMpa().getId());
        assertEquals("R", updatedFilm.getMpa().getName());

        assertEquals(1, updatedFilm.getGenres().size());
        assertTrue(updatedFilm.getGenres().stream()
                .anyMatch(g -> g.getId() == 2 && "Драма".equals(g.getName())));
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Первый фильм");
        film1.setDescription("Описание первого фильма");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);

        Mpa mpa1 = new Mpa();
        mpa1.setId(1);
        film1.setMpa(mpa1);

        filmDbStorage.createFilm(film1);

        Film film2 = new Film();
        film2.setName("Второй фильм");
        film2.setDescription("Описание второго фильма");
        film2.setReleaseDate(LocalDate.of(2010, 5, 10));
        film2.setDuration(120);

        Mpa mpa2 = new Mpa();
        mpa2.setId(2);
        film2.setMpa(mpa2);

        filmDbStorage.createFilm(film2);

        Collection<Film> films = filmDbStorage.getFilms();

        assertEquals(2, films.size());
    }

    @Test
    void shouldAddAndRemoveLike() {
        User user = new User();
        user.setEmail("like@test.ru");
        user.setLogin("like_user");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1995, 1, 1));
        userDbStorage.createUser(user);

        Film film = new Film();
        film.setName("Фильм с лайком");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2005, 5, 5));
        film.setDuration(110);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmDbStorage.createFilm(film);

        filmDbStorage.addLike(createdFilm.getId(), user.getId());

        Film filmWithLike = filmDbStorage.getFilmById(createdFilm.getId())
                .orElseThrow();

        assertTrue(filmWithLike.getLikes().contains(user.getId()));

        filmDbStorage.removeLike(createdFilm.getId(), user.getId());

        Film filmWithoutLike = filmDbStorage.getFilmById(createdFilm.getId())
                .orElseThrow();

        assertFalse(filmWithoutLike.getLikes().contains(user.getId()));
    }

    @Test
    void shouldDeleteFilm() {
        User user = new User();
        user.setEmail("deletefilm@test.ru");
        user.setLogin("deletefilm_user");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.createUser(user);

        Film film = new Film();
        film.setName("Фильм для удаления");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        film.getGenres().add(genre);

        Film createdFilm = filmDbStorage.createFilm(film);

        filmDbStorage.addLike(createdFilm.getId(), user.getId());

        filmDbStorage.deleteFilm(createdFilm.getId());

        assertTrue(filmDbStorage.getFilmById(createdFilm.getId()).isEmpty());
    }
}
