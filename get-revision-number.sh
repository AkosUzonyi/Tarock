#!/bin/bash

git describe --tags --long | cut -d - -f 2
