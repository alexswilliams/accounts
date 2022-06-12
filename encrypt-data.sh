#!/usr/bin/env bash

set -ex

tar cJf data.tar.xz --exclude secrets/02-data/transactions secrets/02-data/
gpg --yes --symmetric -a -o data.tar.xz.asc data.tar.xz
rm -f data.tar.xz
