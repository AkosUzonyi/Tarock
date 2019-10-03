#!/bin/bash

PROJECT_DIR="$(pwd)"

PKG_NAME=tarock-server
VERSION=$(cat "$PROJECT_DIR/VERSION")

cd "$PROJECT_DIR/server"
./gradlew assembleDist

cd "$PROJECT_DIR/arch_package"
ln -sf "../server/build/distributions/$PKG_NAME-$VERSION.tar" tarock-server.tar
makepkg -fc
