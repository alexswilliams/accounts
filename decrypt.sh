#!/usr/bin/env bash

set -ex

gpg --decrypt -o secrets.tar.xz secrets.tar.xz.asc

rm -rf secrets
mkdir secrets
tar xJf ./secrets.tar.xz
rm -f ./secrets.tar.xz
