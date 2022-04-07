#!/usr/bin/env bash

set -ex

gpg --decrypt -o secrets.tar.xz secrets.tar.xz.asc
gpg --decrypt -o data.tar.xz data.tar.xz.asc

rm -rf secrets
mkdir secrets
tar xJf ./secrets.tar.xz
tar xJf ./data.tar.xz
rm -f ./secrets.tar.xz
rm -f ./data.tar.xz
