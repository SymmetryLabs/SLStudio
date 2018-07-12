import sys

import spotipy
import spotipy.util as util


import calendar
import dateutil
import json
import pprint

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
from numpy import nan as Nan


#scope = 'user-library-read'
#scope = 'user-top-read user-read-playback-state'
# scope = 'user-read-playback-state user-read-recently-played'

# token = util.prompt_for_user_token('aaronopp', scope, client_id='dbe2a20785304190b8e35d5d6644397b', client_secret='cc259d8378be48beaad9171a5afb19ba', redirect_uri='http://localhost:8888/callback')
# import dateutil.parser as dp



# just realized i should only create/save features at the end when i have all ids. then i
# only execute it when i need it!
def spotify_pipeline(token, scope, time_test_scaled, filename, data_gen_run_time=5, save=True):
    
    #if not os.path.exists(filename):
        #print 'data folder created'
        #os.makedirs(filename)
    print('\n \n \n \n \n run time min 2:', data_gen_run_time)

    spotify_data_df = create_spotify_df(token, scope, time_test_scaled, num_songs=data_gen_run_time)
    if save:
        spotify_data_df.to_csv(filename + '_raw.csv')
    print(spotify_data_df.index[1])

    print(time_test_scaled[1:4])
    print(spotify_data_df.index[1]- time_test_scaled[1])
    spotify_dataset = create_spotify_dataset(spotify_data_df, time_test_scaled)
    if save:
        spotify_dataset.to_csv(filename + '.csv')
    return spotify_data_df

def save_spotify_df(token, scope, filename):
	
    spotify_data_df = create_spotify_df(token, scope)
    spotify_data_df.to_csv(filename)

def get_api_history_song_number(num):
    if num > 30:
        return num/2
    if num > 200:
        print('\n \n can only take last 100 songs! \n \n')
        return 100

def create_spotify_df(token, scope, get_genre=True, save_features=False, num_songs=5):
    print('\n \n \n \n \n run time min 3:', num_songs)
    
    timestamp = []
    songs = []
    artists = []
    popularities = []
    ids = []
    artist_ids = []
    genres = []
    durations = []

    if token:
        sp = spotipy.Spotify(auth=token)
        sp.trace=False
        ranges = ['short_term', 'medium_term', 'long_term']
        #esults = sp.current_user_playing_track()
        try:
            curr_results = sp.current_playback()
        except AttributeError as err:
            print (err)
            curr_results = None
        #print 'current playback keys ', curr_results.keys()
        #print results['item']
        #print 'user playing track keys', results.keys()
        if curr_results != None:
            
            print('song name: ', curr_results['item']['name'])
            print('timestamp: ', curr_results['timestamp'])
            curr_artist_info = curr_results['item']['artists']
            print('artist name: ', curr_artist_info[0]['name'])
            print('artist id: ', curr_artist_info[0]['id'])



            #print curr_results['timestamp']
            #print 'type name: ', curr_results['item']['type']
            print('song id: ', curr_results['item']['id'])
            
            print('\n starting spotify data with current playing song! \n')

            timestamp.append(int(str(curr_results['timestamp'])))
            songs.append(curr_results['item']['name'])
            ids.append(curr_results['item']['id'])

            artists.append(curr_artist_info[0]['name'])
            artist_ids.append(curr_artist_info[0]['id'])

            popularities.append(curr_results['item']['popularity'])
            #if get_genre == True:
            genres.append(get_genre_from_track_id(sp, curr_results['item']['id']))
            durations.append(curr_results['progress_ms'])
        else: 
            print('\n no track currently playing \n')
                    
        recent_results = sp.current_user_recently_played(limit=num_songs)
        #print recent_results.keys()

        #print recent_results['href']
        #print recent_results['cursors']
        #print recent_results['next']

        for i, item in enumerate(recent_results['items']):
            print('name: ', item['track']['name'])
            print('id: ', item['track']['id'])
            artist_info = item['track']['artists']
            print('artist: ', artist_info[0]['name'])
            print('duration ms: ', item['track']['duration_ms'])

            print('popularity: ', item['track']['popularity'])


            print('played: ', item['played_at'])
            print('type', type(item['played_at']))
            timestamp.append(item['played_at'])
            songs.append(item['track']['name'])
            ids.append(item['track']['id'])

            artists.append(artist_info[0]['name'])
            artist_ids.append(artist_info[0]['id'])

            popularities.append(item['track']['popularity'])
            #if get_genre == True:
            genres.append(get_genre_from_track_id(sp, item['track']['id']))
            durations.append(item['track']['duration_ms'])

        #get timestamps
        utc_timestamps = []
        for index, time in enumerate(timestamp):
            if index != 0:
                utc_timestamps.append(calendar.timegm(dateutil.parser.parse(time).timetuple())*1000)
            else:
                utc_timestamps.append(time)
        print(utc_timestamps)
        # create dataframe of basic data.
        spotify_data = {'timestamp': utc_timestamps, 'artist': artists, 'artist_id': artist_ids, 'song': songs, 'id': ids, 'genre': genres, 'popularity': popularities, 'duration': durations}
        
        spotify_data_df = pd.DataFrame(spotify_data)
        spotify_data_df = spotify_data_df.set_index('timestamp')
        
        if save_features == True:
            try: 
                save_spotify_features(sp, ids)
                print('features saved!')
            except:
                print('unable to save features')

    return spotify_data_df

def save_spotify_features(sp, ids):
    # get audio features for each track and save as a JSON!
    features = sp.audio_features(ids)
    features_json = []
    for feature in features:
        #print feature
        print((json.dumps(feature, indent=4)))
        print()
        features_json.append(json.dumps(feature, indent=4))
        with open('data.json', 'a') as outfile:
            json.dump(feature, outfile, indent=4)
        #analysis = sp._get(feature['analysis_url'])
        #print(json.dumps(analysis, indent=4))
        #print()
def get_album_id_from_track(sp, id):
    track = sp.track(id)
    #pprint.pprint(track)
    album_id = track['album']['id']
    
    print(album_id)
    return album_id
def get_genre_from_track_id(sp, id):
    track = sp.track(id)
    #pprint.pprint(track)
    album_id = track['album']['id']
    album = sp.album(str(album_id))
    pprint.pprint(album['genres'])
    return album['genres']

def get_audio_features_json(sp, dataframe, savefile):
    # get audio features for each track and save as a JSON!
    ids = spotify_data['id'].tolist()
    features = sp.audio_features(ids)
    features_json = []
    for feature in features:
        #print feature
        print((json.dumps(feature, indent=4)))
        print()
        features_json.append(json.dumps(feature, indent=4))
        with open(savefile, 'a') as outfile:
            json.dump(feature, outfile, indent=4)
        # OPTIONAL song analysis (too much irrelavant data ATM.)    
        
        #analysis = sp._get(feature['analysis_url'])
        #print(json.dumps(analysis, indent=4))
        #print()

####################################################
#  Functions to match song data with timestamp data
#
####################################################
def match_spotify_dataset(dataframe, time_test_scaled):
    j = 0
    Y_logger_df = pd.DataFrame(data=None, columns=dataframe.columns)

    for index_logger, timestamps in enumerate(time_test_scaled):
        for index, value in enumerate(list(dataframe.index)):
            if value < timestamps:
                print(value-timestamps)
                print(index)
                print('\n timestamps - value: ', timestamps - value)
                print('\n dataframe duration: ', dataframe.iloc[index]['duration'])
                print(dataframe.iloc[index])
                if timestamps - value > dataframe.iloc[index]['duration']: # was .iloc[0]
                    print('\n \n \n song is not actually playing')
                    spotify_series = pd.Series([Nan, Nan, Nan, Nan, Nan, Nan, Nan], 
                                               index=['artist', 'artist_id', 'duration', 'genre', 'id',  
                                                    'popularity', 'song'], name=value)
                    Y_logger_df = Y_logger_df.append(spotify_series)
                    break
                Y_logger_df = Y_logger_df.append(dataframe.iloc[index])
                j += 1
                break
    print('j:' , j)
    if j == 0:
        print('\n \n NO MATCHES - please extend your recent playback! \n \n')
        print('terminating script')
        sys.exit(1)
    return Y_logger_df

def create_spotify_dataset(dataframe, time_test_scaled, save_df=False):
    y_df = match_spotify_dataset(dataframe, time_test_scaled)
    print('dataframe length: ', len(y_df))
    print('time test length: ', len(time_test_scaled))

    try:
        y_df['timestamps'] = time_test_scaled
    except:
        y_df['timestamps'] = time_test_scaled[:len(y_df)]
        print('timestamps not same length as DF')
    y_df = y_df.reset_index()
    y_df = y_df.rename(columns={'index': 'song_start_ts'})
    y_df = y_df.set_index('timestamps')
    if save_df == True:
        y_df.to_csv('spotify_dataset.csv')
    return y_df

# to write- get all IDs from big pandas dataframe and then save spotify features
# save spotify data df as dataframe? or just sync it w training data.