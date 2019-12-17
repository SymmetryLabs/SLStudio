#!/bin/bash
echo $(echo "$(date +'%s') - $(cat .tmp.time.since.last.packet)"|bc) \< 60|bc
