#!/bin/bash

git describe --tags | cut -d - -f 1
