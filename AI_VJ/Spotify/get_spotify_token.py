from spotify_utils import *


import sys

import spotipy
import spotipy.util as util

scope = 'user-read-playback-state user-read-recently-played'
TRAINING_DATA_PATH = '/Users/aaronopp/Desktop/SymmetryLabs/winter_sun/SLStudio/AI_VJ/training_data/aaronopp/1_18/'

print 'running a test to make sure spotify web api works!'
username = sys.argv[1]
token = util.prompt_for_user_token(username, scope, client_id='dbe2a20785304190b8e35d5d6644397b', client_secret='cc259d8378be48beaad9171a5afb19ba', redirect_uri='http://localhost:8888/callback')

if token:
    sp = spotipy.Spotify(auth=token)
    sp.trace=False
    ranges = ['short_term', 'medium_term', 'long_term']
    #esults = sp.current_user_playing_track()
    curr_results = sp.current_playback()
    #save_spotify_df(token, scope, 'fullspotifytest')
    spotify_pipeline(token, scope, time_test_scaled, DATA_FOLDER + 'spotify_data_' + time_tag)
    print '\n test worked!'

else:
    print "Can't get token for", username