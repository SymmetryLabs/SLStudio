#!/bin/bash
cd "$(dirname "$0")"

git_hash=$(git rev-parse --short HEAD)
test -z "$(git status -s --porcelain --untracked-files=normal)"
git_dirty=$?
docker_tag=$git_hash$(if [ $git_dirty = 1 ]; then echo '-dirty'; fi)

docker build -t slstudio:$docker_tag .
