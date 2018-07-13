# function to generate training data for AI VJ
# this version also uses the spotify API to generate music metadata

# from utils import *
from spotify_utils import *

# #import time
# import time as _time
# from time import sleep
# import datetime
# import os
# from os.path import isfile
# import ast
# from shutil import copyfile, copy
# import sys
# from timeit import default_timer as timer

# import numpy as np
# from numpy import array, random, arange, float32, float64, zeros
# import pandas as pd

# import librosa
# import librosa.display
# from librosa.feature import melspectrogram
# #import pyaudio
# #from pyaudio import PyAudio, paContinue, paFloat32
# import sounddevice as sd
# function to generate training data for AI VJ
from utils import *


import time as _time
import datetime
import os
from shutil import copyfile, copy
import sys

import numpy as np
from numpy import array, random, arange, float32, float64, zeros
import sounddevice as sd

from collections import Counter
import pythonosc
import spotipy
import spotipy.util as util

from server import block_until_token
from numpy import nan as Nan


import webbrowser

module_path = os.path.abspath(os.path.join('..'))
if module_path not in sys.path:
    sys.path.append(module_path)

#############################
# Set sounddevice!
#############################


audio_input_device = sys.argv[3]
print('aud input: ' + audio_input_device)

set_sounddevices(sd, input_name=audio_input_device)
print(sd.query_devices())
sd.default.channels = 1

#############################
# Initalize spotify web API
#############################

sys.path.append('/Users/aaronopp/Desktop/SymmetryLabs/winter_sun/SLStudio/AI_VJ/Spotify')
scope = 'user-read-playback-state user-read-recently-played'

print('\n trying to create spotify data and marry it \n')
username = sys.argv[1]

print("GETTING TOKEN")
sp_oauth = spotipy.oauth2.SpotifyOAuth(client_id='dbe2a20785304190b8e35d5d6644397b',
    client_secret='9741d373ad3d479fb8dc135f53580e71', redirect_uri='http://localhost:5555/redirect',  scope=scope)
auth_url = sp_oauth.get_authorize_url()

webbrowser.open(auth_url)
token = block_until_token(sp_oauth)

print("GOT TOKEN", token)


#############################
# Constants
#############################

fs            = 16000   # Hz
threshold     = 0.8     # absolute gain
delay         = 40      # samples
signal_length = 30     # second
release_coeff = 0.5555  # release time factor
attack_coeff  = 0.5     # attack time factor
dtype         = float32 # default data type
block_length  = 1024    # samples



print('default devices', sd.default.device)

print('current time', _time.time())
now = datetime.datetime.now()

best_threshold = [ 0.2,  0.1,  0.2,  0.1,  0.4,  0.1 , 0.1,  0.1,  0.1,  0.1 , 0.2 , 0.2,  0.1,  0.2,  0.1,
  0.1 , 0.3,  0.2 , 0.2 , 0.2 , 0.3 , 0.2 , 0.1 , 0.1 , 0.2 , 0.1 , 0.1,  0.1 , 0.2,  0.6,
  0.2,  0.2 , 0.1 , 0.2 , 0.1,  0.1,  0.1,  0.1,  0.1,  0.1,  0.1,  0.1,  0.3,  0.2,  0.2,
  0.5,  0.2,  0.1  ,0.2,  0.1]

top_50 = ['rock', 'pop', 'alternative', 'indie', 'electronic', 'female vocalists',
    'dance', '00s', 'alternative rock', 'jazz', 'beautiful', 'metal',
    'chillout', 'male vocalists', 'classic rock', 'soul', 'indie rock',
    'Mellow', 'electronica', '80s', 'folk', '90s', 'chill', 'instrumental',
    'punk', 'oldies', 'blues', 'hard rock', 'ambient', 'acoustic', 'experimental',
    'female vocalist', 'guitar', 'Hip-Hop', '70s', 'party', 'country', 'easy listening',
    'sexy', 'catchy', 'funk', 'electro' ,'heavy metal', 'Progressive rock',
    '60s', 'rnb', 'indie pop', 'sad', 'House', 'happy']

color_labels = ['red', 'orange', 'yellow', 'green', 'teal', 'blue', 'purple', 'magenta', 'red']
color_labels_encoding = ['red', 'orange', 'yellow', 'green', 'teal', 'blue', 'purple', 'magenta']

speed_range_scaled = [0, 34, 67, 101]
speed_labels = ['slow', 'medium', 'fast']


speed_unencode = [0.2, 0.5, 0.8]
color_unencode = [0.0, 0.08, 0.15, 0.35, 0.48, 0.67, 0.76, 0.84]

boundary_range_scaled = np.array([0, 20, 45, 75, 150, 200, 255, 285, 330, 361])
boundary_range = boundary_range_scaled/360.

patterns_full = ['AskewPlanes', 'Balance', 'Ball', 'BassPod', 'Blank', 'Bubbles', 'CrossSections', 'CubeEQ',
                 'CubeFlash', 'Noise', 'Palette', 'Pong', 'Rings', 'ShiftingPlane', 'SoundParticles', 'SpaceTime',
                'Spheres', 'StripPlay', 'Swarm', 'Swim', 'TelevisionStatic', 'Traktor', 'ViolinWave']
patterns_reduced =  ['AskewPlanes', 'Balance', 'CrossSections', 'CubeEQ',
                 'CubeFlash', 'Noise', 'Pong', 'Rings', 'ShiftingPlane', 'SoundParticles', 'SpaceTime',
                'Spheres', 'StripPlay', 'Swarm', 'Swim', 'Traktor', 'ViolinWave']


effect_labels = ['low', 'medium', 'high']
effect_labels_full = [None, 'low', 'medium', 'high']
brightness_labels = ['off', 'half', 'full']

param_labels = ['Faster', 'Hue Variation', 'Sparkle', 'Crazy', 'Larger']
param_labels_full = [None, 'Faster', 'Hue Variation', 'Sparkle', 'Crazy', 'Larger']

output_on_osc_route = '/lx/output/enabled'

###########################################################

# ---------------- GENERATE TRAINING DATA -----------------

###########################################################

# Generate data with mel spec size of 15 sec and sample rate of 5 sec

if (len(sys.argv) < 3 or  len(sys.argv) > 3):
    print('need 3 args, first is your name, second is run time in min, third is spotify token!')
    sys.exit(1)

AI_VJ_FOLDER = sys.argv[0][:-28]

#print 'ai vj folder: ', AI_VJ_FOLDER

WEIGHTS_FOLDER = AI_VJ_FOLDER + 'model_weights/v2/'
TRAINING_DATA_FOLDER = AI_VJ_FOLDER + 'training_data/'
PROCESSING_OUTPUT = AI_VJ_FOLDER + 'logger/data/out.json'
DATA_FOLDER = TRAINING_DATA_FOLDER + sys.argv[1] + '/'+ str(now.month) + '_'+ str(now.day) + '/'

print(DATA_FOLDER)
if not os.path.exists(DATA_FOLDER):
	print('data folder created')
	os.makedirs(DATA_FOLDER)

duration = 10 # seconds
run_time_min = int(sys.argv[2]) # run_time/60
run_time = run_time_min*60
run_test = False
X_size = int(run_time//2.5) - 3
#X_size_final = (run_time//5) - 3

mel_size = 626 # 938 is 15 seconds, 626 is 10 sec
RATE = 16000

i = 0
z = 0

if run_time_min == 1:
    print('''
    ######################
    test run - only 1 min!
    ######################
    ''')
    run_test = True


print('starting to generate training data, running for ', run_time_min , ' minutes!')
print('x size:' , X_size)

start = _time.time()

x = np.array([],ndmin = 2)
b = Buffer(duration * RATE)
print('b read', b.read())

X_test = np.zeros((X_size, 1, 96, mel_size))  # or could be run_time/5 sec
time_test = np.zeros((X_size))


def callback(indata, frames, time, status): #outdata is 5th - when no inputstream


    global i
    global z
    global mel_size
    global run_time
    if status:
        print(status)


    b.extend(indata.squeeze())
    elapsed_time = _time.time()- start

    #time.read()

    if elapsed_time > duration and i % 25 == 0:
        print(i)
        print('time elapsed:', elapsed_time)

        aud = b.read()
        Spec = get_mel_spectrogram(aud)
        Spec = Spec[:,:,:,0:mel_size]

        X_test[z, :,:,:] = Spec
        time_test[z] = _time.time()

        z += 1

    i += 1
with sd.InputStream(samplerate=16000, dtype= np.float32, channels=1, callback=callback):

    sd.sleep(int(run_time*1000))

    print('saving data to ', DATA_FOLDER)
    now_end = datetime.datetime.now()

    time_test_scaled = time_test * 1000
    time_test_scaled = time_test_scaled.astype(int)

    X_test = X_test[:X_size, :, :, :]
    time_test_scaled = time_test_scaled[:X_size]

    send_osc(output_on_osc_route, 0) # to be changed to text in center of SLStudio

    if run_test:
        print('returning before saving real data, saving test for calibration')
        np.save(TRAINING_DATA_FOLDER + 'test/' + 'x_train_raw_' + str(now.month) + '_'+ str(now.day) + '.npy', X_test)
        np.save(TRAINING_DATA_FOLDER + 'test/' + 'time_test_scaled_raw_' + str(now.month) + '_'+ str(now.day) + '.npy', time_test_scaled)

        sys.exit(1)



    time_tag = str(now_end.hour) + '_' + str(now_end.minute)

    if os.path.isfile(DATA_FOLDER + 'x_train_raw_' + time_tag + '.npy'):
        print('path already exists, saving to another location!')
        np.save(DATA_FOLDER + 'x_train_raw_2_' + time_tag + '.npy', X_test)
    else:
        np.save(DATA_FOLDER + 'x_train_raw_' + time_tag + '.npy', X_test)

    if os.path.isfile(DATA_FOLDER + 'time_test_scaled_raw_' + time_tag + '.npy'):
        print('path already exists, saving to another location!')
        np.save(DATA_FOLDER + 'time_test_scaled_raw_' + time_tag + '.npy', time_test_scaled)
    else:
        np.save(DATA_FOLDER + 'time_test_scaled_raw_' + time_tag + '.npy', time_test_scaled)

    print('saved!')

    print('copying file at: ', PROCESSING_OUTPUT, 'and pasting at: ', DATA_FOLDER)

    if os.path.isfile(PROCESSING_OUTPUT):
        copy(PROCESSING_OUTPUT, DATA_FOLDER)

    os.rename(DATA_FOLDER + 'out.json', DATA_FOLDER + 'logger_training_output_raw_' + time_tag + '.json')

# Call spotify web api and get song metadata!
    if token:
        sp = spotipy.Spotify(auth=token)
        sp.trace=False
        print(sp.current_user())
        ranges = ['short_term', 'medium_term', 'long_term']
        #esults = sp.current_user_playing_track()
        #curr_results = sp.current_playback()
        #save_spotify_df(token, scope, 'fullspotifytest')
        print('\n \n \n \n \n run time min:', run_time)
        spotify_pipeline(token, scope, time_test_scaled, DATA_FOLDER + 'spotify_data_' + time_tag, data_gen_run_time=run_time_min)

        print('\n test worked!')

    else:
        print("Can't get token for", username)




#
