#!/bin/sh

java -jar JFlex.jar lexico.flex
java -jar java-cup-11a.jar -expect 1 parser.cup
