#!/bin/bash
# Locally publishes every supported play version for every supported scala version

set -o errexit   # abort on nonzero exitstatus
set -o nounset   # abort on unbound variable
set -o pipefail  # don't hide errors within pipes

env PLAY_VERSION=2.9 sbt +test
env PLAY_VERSION=3.0 sbt +test

