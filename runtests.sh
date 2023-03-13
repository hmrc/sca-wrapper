#!/bin/bash
sbt clean compile scalastyle test it:test dependencyUpdates
