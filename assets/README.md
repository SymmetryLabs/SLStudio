## Why are there small text files here instead of the files I expected?

Files in the `assets` directory aren't automatically downloaded by `git clone`.

To get the actual files:

    git lfs fetch -XX -I assets
    git lfs checkout

The repo is configured this way because files in `assets` aren't used or
changed very often.  To examine or change this setting, see [`.lfsconfig`](
https://github.com/SymmetryLabs/SLStudio/blob/master/.lfsconfig).
