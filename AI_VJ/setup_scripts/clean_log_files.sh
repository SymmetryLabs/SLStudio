#!/bin/bash

echo 're-naming log file as backup - make sure there are no name conflicts'
mv out.txt backup_log_files
echo 'deleting all logs'
rm logger/data/out*


