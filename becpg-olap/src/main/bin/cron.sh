#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

TARGET_JAR="$DIR/lib/${project.artifactId}-${project.version}-jar-with-dependencies.jar"

java -jar $TARGET_JAR -file=jdbc.properties
