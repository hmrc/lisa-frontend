#!/usr/bin/env bash

sbt clean compile coverage test coverageOff coverageReport dependencyUpdates A11y/test