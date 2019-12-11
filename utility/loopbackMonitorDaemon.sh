#!/bin/bash
export TMP_PACKET_TIME_FILE=.tmp.time.since.last.packet
sudo tshark -t e -i lo -f 'udp port 1337' 2>/dev/null |awk '{print $2}'|\
while read i
do
  echo $i > $TMP_PACKET_TIME_FILE
done
