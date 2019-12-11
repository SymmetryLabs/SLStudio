#!/bin/bash
echo $(echo "$(date +'%s') - $(cat .tmp.time.since.last.packet)"|bc) \< 10|bc
