#!/bin/bash
while true; do curl -X PUT -d '{ "pulse": "'$(./utility/check_output_in_last_60_seconds.sh)'" }' \
  'https://oslotree-aa3e5.firebaseio.com/osloHeartbeat/'$(date +'%s')'.json' ; sleep 120; done
