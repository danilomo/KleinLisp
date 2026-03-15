#!/bin/sh

# Run from project root directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
PARSER_DIR="$PROJECT_DIR/src/main/java/net/sourceforge/kleinlisp/parser"

java -jar "$SCRIPT_DIR/JFlex.jar" -d "$PARSER_DIR" "$PARSER_DIR/lexical.flex"
java -jar "$SCRIPT_DIR/java-cup-11a.jar" -destdir "$PARSER_DIR" -expect 1 "$PARSER_DIR/parser.cup"
