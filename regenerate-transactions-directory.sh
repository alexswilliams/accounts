#!/usr/bin/env bash

set -ex

(
  set -ex
  cd secrets/02-data
  rm -rf transactions
  tar xf transactions.tar transactions/
)
