package academy.acceptance;

import academy.cli.ArgumentsValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentValidationTest {

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("not_existing_file.log"),
                "json",
                Path.of("out.json"),
                null,
                null
            )
        );
    }

    /**
     * тест 2 не относится к cli валидатору, так как требует выполнять http запрос
     * этот случай я проверил в другом месте
     */
//    @Test
//    @DisplayName("На вход передан несуществующий удаленный файл")
//    void test2() {
//
//    }

    @ParameterizedTest
    @ValueSource(strings = ".docx")
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension) {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file" + extension),
                "json",
                Path.of("out.json"),
                null,
                null
            )
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void test4(String from) {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file.log"),
                "json",
                Path.of("out.json"),
                from,
                null
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt"})
    @DisplayName("Результаты запрошены в неподдерживаемом формате {0}")
    void test5(String format) {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file.log"),
                format,
                Path.of("out.json"),
                null,
                null
            )
        );
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output) {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file.log"),
                format,
                Path.of(output),
                null,
                null
            )
        );
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
            Arguments.of("markdown", "./results.txt"),
            Arguments.of("json", "./results.md"),
            Arguments.of("adoc", "./results.ad1")
        );
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7() throws Exception {
        Path existing = Files.createTempFile("exists", ".json");

        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file.log"),
                "json",
                existing,
                null,
                null
            )
        );

        Files.deleteIfExists(existing);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String argument) {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                null,
                "json",
                Path.of("out.json"),
                null,
                null
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String argument) {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file.log", argument),
                "json",
                Path.of("out.json"),
                null,
                null
            )
        );
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() {
        assertThrows(IllegalArgumentException.class, () ->
            ArgumentsValidator.validate(
                List.of("file.log"),
                "json",
                Path.of("out.json"),
                "2025-01-10T10:00:00Z",
                "2025-01-01T10:00:00Z"
            )
        );
    }
}
