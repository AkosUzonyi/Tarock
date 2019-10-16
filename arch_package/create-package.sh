#!/bin/bash

PROJECT_DIR="$(realpath ..)"

PKG_NAME=tarock-server
VERSION=$(cat "$PROJECT_DIR/VERSION")

cd "$PROJECT_DIR/server"
./gradlew assembleDist

cd "$PROJECT_DIR/arch_package"
ln -sf "$PROJECT_DIR/server/build/distributions/$PKG_NAME-$VERSION.tar" tarock-server.tar
PKGDEST=package makepkg -fc
