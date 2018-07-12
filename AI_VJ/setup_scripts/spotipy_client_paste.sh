#!/bin/bash


OUTPUT="$(python -c "import spotipy; string = "spotipy.__file__"; print string[:-12]")"
echo "${OUTPUT}"
cp Spotify/client.py ${OUTPUT}

