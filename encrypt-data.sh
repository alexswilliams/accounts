#!/usr/bin/env bash

set -ex

tar cJf data.tar.xz --include secrets/02-data secrets/*
gpg --yes --symmetric -a -o data.tar.xz.asc data.tar.xz
rm -f data.tar.xz
