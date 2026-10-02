package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        return getFilmOrThrow(filmId);
    }

    public Film updateFilm(Film film) {
        getFilmOrThrow(film.getId());
        return filmStorage.updateFilm(film);
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        film.getLikes().add(userId);

    }

    public void removeLike(int filmId, int userId) {
        getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = new ArrayList<>(filmStorage.getFilms());
        films.sort(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed());
        return films.subList(0, Math.min(count, films.size()));
    }

    private Film getFilmOrThrow(int filmId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film == null) {
            throw new NotFoundException("Фильм с таким id не найден");
        }

        return film;
    }

    private User getUserOrThrow(int userId) {
        User user = userStorage.getUserById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        return user;
    }
}
