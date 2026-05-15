# SudokuSolver
An old, badly structured sudoku solver project that I made conveniently runnable using Gradle

## Executing
1. Clone the repository
2. Open a terminal and navigate to the repo's root directory
3. On Windows, run `.\gradlew.bat run`. On Linux/macOS, run `./gradlew run`.

It is recommended to use [Java 16](https://jdk.java.net/java-se-ri/16) to run the application.
Versions 17 and newer don't work at all because Gradle is fun.
You can manually set the Java version gradlew will use via `./gradlew run -Dorg.gradle.java.home=/path/to/java16`.
