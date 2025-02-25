#!/bin/bash
mvn clean compile > output.log 2>&1
cat output.log
