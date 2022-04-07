#!/usr/bin/env bash

set -ex

tar cJf secrets.tar.xz --exclude secrets/02-data secrets/*
gpg --yes --symmetric -a -o secrets.tar.xz.asc secrets.tar.xz
rm -f secrets.tar.xz
