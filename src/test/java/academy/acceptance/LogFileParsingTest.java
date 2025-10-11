package academy.acceptance;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LogFileParsingTest {

    @Test
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл, "
            + "часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() {
        fail("Not implemented yet");
    }
}
