#!/bin/sh
set -e

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$BASE_DIR"

sh "$BASE_DIR/mvnw" -q -DskipTests package
exec java -jar "$BASE_DIR/target/doudizhu.jar"
