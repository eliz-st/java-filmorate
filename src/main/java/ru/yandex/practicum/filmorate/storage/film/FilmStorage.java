package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(int id);

    Optional<Film> getFilmById(int id);

    Collection<Film> getFilms();

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);
}