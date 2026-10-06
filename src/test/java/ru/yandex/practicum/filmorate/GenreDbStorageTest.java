package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
public class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Autowired
    public GenreDbStorageTest(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    @Test
    void shouldFindGenreById() {
        Genre genre = genreDbStorage.getGenreById(1);

        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void shouldGetAllGenres() {
        List<Genre> genres = genreDbStorage.getAllGenres();

        assertEquals(6, genres.size());

        assertEquals(1, genres.get(0).getId());
        assertEquals("Комедия", genres.get(0).getName());

        assertEquals(6, genres.get(5).getId());
        assertEquals("Боевик", genres.get(5).getName());
    }

    @Test
    void shouldThrowExceptionWhenGenreNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> genreDbStorage.getGenreById(999));
    }
}
