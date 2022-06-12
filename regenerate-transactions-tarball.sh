#!/usr/bin/env bash

set -ex

(
  set -ex
  cd secrets/02-data
  tar cf transactions.tar transactions/
)
