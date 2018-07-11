from spotify_utils import *

import sys
import spotipy
import spotipy.util as util

#scope = 'user-library-read'
#scope = 'user-top-read user-read-playback-state'
scope = 'user-read-playback-state user-read-recently-played'

print 'running a test to make sure spotify web api works!'
username = sys.argv[1]
token = util.prompt_for_user_token(username, scope, client_id='dbe2a20785304190b8e35d5d6644397b', client_secret='cc259d8378be48beaad9171a5afb19ba', redirect_uri='http://localhost:8888/callback')
if token:
    sp = spotipy.Spotify(auth=token)
    sp.trace=False
    ranges = ['short_term', 'medium_term', 'long_term']
    #esults = sp.current_user_playing_track()
    curr_results = sp.current_playback()
    #print curr_results
    #print 'current playback keys ', curr_results.keys()
    #print results['item']
    #print 'user playing track keys', results.keys()
    print '''   
   current song playing thru spotify: 
         '''
    save_spotify_df(token, scope, 'test_spotify.csv')

 #    print '   song name: ', curr_results['item']['name']
 #    print '   timestamp: ', curr_results['timestamp']
 #    artist_info = curr_results['item']['artists']
 #    print '   artist name: ', artist_info[0]['name']
 #    print '   artist id: ', artist_info[0]['id']
	


	# #print curr_results['timestamp']
 #    #print 'type name: ', curr_results['item']['type']
 #    print '   song id: ', curr_results['item']['id']
    print '\n test worked!'

else:
    print "Can't get token for", username



    