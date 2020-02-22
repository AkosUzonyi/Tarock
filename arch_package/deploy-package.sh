#!/bin/bash

PROJECT_DIR="$(realpath ..)"

USER=root
HOST=akos0.ddns.net
PORT=3125

PKG_NAME=tarock-server
VERSION=$(git describe --tags --dirty | tr - _)

PKG_FILE="$PROJECT_DIR/arch_package/package/$PKG_NAME-$VERSION-1-any.pkg.tar.xz"
TMP_FILE="/tmp/$PKG_NAME.tar.xz"

scp -P "$PORT" "$PKG_FILE" "$USER@$HOST:$TMP_FILE"
ssh "$USER@$HOST" -p "$PORT" << EOF
	pacman -U --noconfirm "$TMP_FILE"
	systemctl restart "$PKG_NAME.service"
EOF
