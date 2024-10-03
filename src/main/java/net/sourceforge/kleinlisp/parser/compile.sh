#!/bin/sh

java -jar JFlex.jar lexical.flex
java -jar java-cup-11a.jar -expect 1 parser.cup
