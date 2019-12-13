Run loopbackMonitorDaemon.sh in the background.  This will continuously log last time a packet was received on loopback on port 1337.

Add the check_output_in_last_10_seconds.sh to a crontab to run every few seconds... if it's output is zero then the application has died (no packet received in the last 10 seconds).
