#!/bin/sh

(export PLAY_VERSION=2.8; sbt +compile +test)
(export PLAY_VERSION=2.9; sbt +compile +test)
(export PLAY_VERSION=3.0; sbt +compile +test)

