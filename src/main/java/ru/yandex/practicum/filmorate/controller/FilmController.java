package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int count = 0;

    @PostMapping
    public Film createFilm(@RequestBody Film film) {

        validateFilm(film);

        film.setId(++count);
        films.put(film.getId(), film);

        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {

        validateFilm(film);

        if (!films.containsKey(film.getId())) {
            log.warn("Ошибка обновления фильма: фильм с таким id {} не найден", film.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }

        films.put(film.getId(), film);
        log.info("Обновлен фильм: {}", film);
        return film;
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
                && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации фильма: дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
        }
    }

}
