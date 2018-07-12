import sys
import spotipy
import spotipy.util as util

#scope = 'user-library-read'
#scope = 'user-top-read user-read-playback-state'
scope = 'user-read-playback-state user-read-recently-played'

token = util.prompt_for_user_token('aaronopp', scope, client_id='dbe2a20785304190b8e35d5d6644397b', client_secret='cc259d8378be48beaad9171a5afb19ba', redirect_uri='http://localhost:8888/callback')
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
    print 'song name: ', curr_results['item']['name']
    print 'timestamp: ', curr_results['timestamp']
    artist_info = curr_results['item']['artists']
    print 'artist name: ', artist_info[0]['name']
    print 'artist id: ', artist_info[0]['id']
	


	#print curr_results['timestamp']
    #print 'type name: ', curr_results['item']['type']
    print 'song id: ', curr_results['item']['id']
    print '''




	'''
    recent_results = sp.current_user_recently_played(limit=5)
    print recent_results.keys()

    print recent_results['href']
    print recent_results['cursors']
    print recent_results['next']

    for i, item in enumerate(recent_results['items']):
    	print 'name: ', item['track']['name']
    	print 'id: ', item['track']['id']
    	artist_info = item['track']['artists']
    	print 'artist: ', artist_info[0]['name']
    	print 'duration ms: ', item['track']['duration_ms']

    	print 'popularity: ', item['track']['popularity']
    	print 'played: ', item['played_at']
    	print item
    	#for num, it in enumerate(item['track']):
    		#print num ,type(it[0])
    	#print type(item)
    	#print dir(item)
    	#print item[0]
    	#print item['track']
    	#print type(item['track'])
    	#print i, item['artists']
    #print results['timestamp']
    #print results['progress_ms'] 
    #print results['is_playing']
    #print results['id']
    #print results['artist']
    #for range in ranges:
        #print "range:", range
        #print dir(sp)
        
        #results = sp.current_user_top_artists(time_range=range, limit=50)
       
        #print 'device', sp.device
        
        #print results['timestamp']
        #print 'is playing : ', sp.is_playing
        #for i, item in enumerate(results['track']):
            #print i, item['name']
        #print

    #results = sp.current_user_saved_tracks()
    
    #for item in results['items']:
        #track = item['track']
        #print track['name'] + ' - ' + track['artists'][0]['name']
else:
    print "Can't get token for", username



# if len(sys.argv) > 1:
#     tid = sys.argv[1]
# else:
#     tid = 'spotify:track:4TTV7EcfroSLWzXRY6gLv6'

# start = time.time()
# analysis = sp.audio_analysis(tid)
# delta = time.time() - start
# print(json.dumps(analysis, indent=4))
# print ("analysis retrieved in %.2f seconds" % (delta,))

# methods to call- 
# artist, artist_related_artists, artist_top_tracks, audio_analysis, audio_features, current_playback