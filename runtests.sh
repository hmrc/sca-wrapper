#!/bin/bash
sbt clean coverage compile scalastyle test it:test dependencyUpdates
