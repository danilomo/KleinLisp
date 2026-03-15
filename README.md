# KleinLisp

A lightweight Lisp implementation in Java.

## Building

Requires Java 8 or later.

```bash
./gradlew build
```

This will:
- Generate the lexer from `lexical.flex` using JFlex
- Generate the parser from `parser.cup` using CUP
- Compile all Java sources
- Run tests
- Create a fat JAR with all dependencies

The output JAR is located at `build/libs/KleinLisp-0.0.1.jar`.

## Usage

```bash
java -jar build/libs/KleinLisp-0.0.1.jar <script.scm>
```

## Gradle Tasks

| Task | Description |
|------|-------------|
| `./gradlew build` | Full build with parser generation |
| `./gradlew generateLexer` | Regenerate lexer from `lexical.flex` |
| `./gradlew generateParser` | Regenerate parser from `parser.cup` |
| `./gradlew test` | Run tests |
| `./gradlew spotlessApply` | Format code (Google Java Format) |

## License

MIT
