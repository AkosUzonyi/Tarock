#!/bin/bash

USER=root
HOST=akos0.ddns.net
PORT=3125

PKG_NAME=tarock-server
VERSION=$("$PROJECT_DIR/get-version.sh")
REVISION=$("$PROJECT_DIR/get-revision-number.sh")

PKG_FILE="arch_package/$PKG_NAME-$VERSION-$REVISION-any.pkg.tar.xz"
TMP_FILE="/tmp/$PKG_NAME.tar.xz"

scp -P "$PORT" "$PKG_FILE" "$USER@$HOST:$TMP_FILE"
ssh "$USER@$HOST" -p "$PORT" << EOF
	yes | pacman -U "$TMP_FILE"
	systemctl restart "$PKG_NAME.service"
EOF
