#!/bin/bash

PROJECT_DIR="$(pwd)"

PKG_NAME=tarock-server
VERSION=$("$PROJECT_DIR/get-version.sh")
REVISION=$("$PROJECT_DIR/get-revision-number.sh")

cd "$PROJECT_DIR/server"
./gradlew assembleDist

cd "$PROJECT_DIR/arch_package"
echo $VERSION > version
echo $REVISION > revision
ln -sf "../server/build/distributions/$PKG_NAME-$VERSION-$REVISION.tar" tarock-server.tar
makepkg -fc
