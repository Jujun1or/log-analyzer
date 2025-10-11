## Структура проекта

Это типовой Java-проект, который собирается с помощью инструмента автоматической
сборки проектов [Apache Maven](https://maven.apache.org/).

Проект состоит из следующих директорий и файлов:

- [pom.xml](./pom.xml) – дескриптор сборки, используемый maven, или Project
  Object Model. В нем описаны зависимости проекта и шаги по его сборке
- [src/](./src) – директория, которая содержит исходный код приложения и его
  тесты:
  - src/main – здесь находится код вашего приложения
  - src/test – здесь находятся тесты вашего приложения
- [mvnw](./mvnw) и [mvnw.cmd](./mvnw.cmd) – скрипты maven wrapper для Unix и
  Windows, которые позволяют запускать команды maven без локальной установки
- [pmd.xml](pmd.xml) и [spotbugs-excludes.xml](spotbugs-excludes.xml) – в проекте
- используются [линтеры](https://en.wikipedia.org/wiki/Lint_%28software%29) для контроля
  качества кода. Указанные файлы содержат правила для используемых линтеров
- [.mvn/](./.mvn) – служебная директория maven, содержащая конфигурационные
  параметры сборщика
- [.editorconfig](.editorconfig) – файл с описанием настроек форматирования кода
- [.gitlab-ci.yml](.gitlab-ci.yml) – файл с описанием шагов сборки проекта в среде Gitlab
- [.gitattributes](.gitattributes), [.gitignore](.gitignore) – служебные файлы
  для git, с описанием того, как обрабатывать различные файлы, и какие из них
  игнорировать

## Начало работы

Для того чтобы собрать проект, и проверить, что все работает корректно, можно
запустить из модального окна IDEA
[Run Anything](https://www.jetbrains.com/help/idea/running-anything.html)
команду:

```shell
mvn clean verify
```

Альтернативно можно в терминале из корня проекта выполнить следующие команды.

Для Unix (Linux, macOS, Cygwin, WSL):

```shell
./mvnw clean verify
```

Для Windows:

```shell
mvnw.cmd clean verify
```

Для окончания сборки потребуется подождать какое-то время, пока maven скачает
все необходимые зависимости, скомпилирует проект и прогонит базовый набор
тестов.

Если вы в процессе сборки получили ошибку:

```shell
Rule 0: org.apache.maven.enforcer.rules.version.RequireJavaVersion failed with message:
JDK version must be at least 24
```

Значит, версия вашего JDK ниже 24.

Если же получили ошибку:

```shell
Rule 1: org.apache.maven.enforcer.rules.version.RequireMavenVersion failed with message:
Maven version should, at least, be 3.9.11
```

Значит, у вас используется версия maven ниже 3.9.11. Такого не должно произойти,
если вы запускаете сборку из IDEA или через `mvnw`-скрипты.

Далее будут перечислены другие полезные команды maven.

Для автоматического форматирования кода используйте команду:

```shell
mvn spotless:apply
```

Запуск только компиляции основных классов:

```shell
mvn compile
```

Запуск тестов:

```shell
mvn test
```

Запуск линтеров:

```shell
mvn clean compile -am spotless:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check
```

Вывод дерева зависимостей проекта (полезно при отладке транзитивных
зависимостей):

```shell
mvn dependency:tree
```

Вывод вспомогательной информации о любом плагине (вместо `compiler` можно
подставить интересующий вас плагин):

```shell
mvn help:describe -Dplugin=compiler
```

## Дополнительные материалы

- Документация по maven: https://maven.apache.org/guides/index.html
- Поиск зависимостей и их версий: https://central.sonatype.com/search
- Документация по процессу автоматизированной сборки в среде Gitlab: https://docs.gitlab.com/ci/
- Документация по git: https://git-scm.com/doc
- Javadoc для Java 24:
  https://docs.oracle.com/en/java/javase/24/docs/api/index.html

