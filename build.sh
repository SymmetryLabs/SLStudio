#!/usr/bin/env bash

command -v processing-java >/dev/null 2>&1 || { echo >&2 "processing-java not found. From Processing IDE, use Tools | Install \"processing-java\" to install"; exit 1; }

processing-java --force --sketch=$(pwd)/SLStudio --output=$(pwd)/build --build