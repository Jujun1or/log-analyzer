package academy.acceptance;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ArgumentValidationTest {

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void test2() {
        fail("Not implemented yet");
    }

    @ParameterizedTest
    @ValueSource(strings = ".docx")
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension) {
        fail("Not implemented yet");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void test4(String from) {
        fail("Not implemented yet");
    }

    @ParameterizedTest
    @ValueSource(strings = "txt")
    @DisplayName("Результаты запрошены в неподдерживаемом формате {0}")
    void test5(String format) {
        fail("Not implemented yet");
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output) {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7() {
        fail("Not implemented yet");
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String argument) {
        fail("Not implemented yet");
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String argument) {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() {
        fail("Not implemented yet");
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "./results.txt"),
                Arguments.of("json", "./results.md"),
                Arguments.of("adoc", "./results.ad1"));
    }
}
