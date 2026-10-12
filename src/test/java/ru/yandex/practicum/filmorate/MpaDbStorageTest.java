package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
public class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;

    @Autowired
    public MpaDbStorageTest(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    @Test
    void shouldFindMpaById() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
    }

    @Test
    void shouldGetAllMpa() {
        List<Mpa> mpaList = mpaDbStorage.getAllMpa();

        assertEquals(5, mpaList.size());

        assertEquals(1, mpaList.get(0).getId());
        assertEquals("G", mpaList.get(0).getName());

        assertEquals(5, mpaList.get(4).getId());
        assertEquals("NC-17", mpaList.get(4).getName());
    }

    @Test
    void shouldThrowExceptionWhenMpaNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> mpaDbStorage.getMpaById(999));
    }
}
