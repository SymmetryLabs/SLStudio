#!/usr/bin/env bash
cd "$(dirname ${BASH_SOURCE[0]})"/../git-hooks && ln -sir * ../.git/hooks/
