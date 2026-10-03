package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.service.FilmService;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {

        validateFilm(film);

        Film createdFilm = filmService.createFilm(film);

        log.info("Добавлен фильм: {}", createdFilm);
        return createdFilm;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получен запрос на получение списка фильмов");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {

        validateFilm(film);

        Film updatedFilm = filmService.updateFilm(film);
        log.info("Обновлен фильм: {}", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id,
                        @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
        public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: название не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации фильма: описание не может быть длиннее 200 символов");
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }

        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации фильма: продолжительность должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Ошибка валидации фильма: дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
        }
    }
}
