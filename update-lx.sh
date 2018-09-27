#!/usr/bin/env bash

echo -e "Update LX.\n" > .git/COMMIT_EDITMSG
git submodule summary LX >> .git/COMMIT_EDITMSG
git add LX
git commit -F .git/COMMIT_EDITMSG
