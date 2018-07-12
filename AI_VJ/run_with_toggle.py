from utils import *

import pyaudio
import time
import threading
from librosa.feature import melspectrogram
import time
import matplotlib.pyplot as plt
import numpy as np
import theano
#from train_network import build_model
import keras


# tools to create the music tagging dataset
import os
import sys
module_path = os.path.abspath(os.path.join('..'))
if module_path not in sys.path:
    sys.path.append(module_path)
    
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
from os.path import isfile

import ast
import glob

import pickle
import json
from pprint import pprint

from shutil import copyfile, copy
import shutil

import librosa
import librosa.display

from collections import Counter
from sklearn.metrics import classification_report, accuracy_score

from keras import backend as K

from keras.models import Sequential, Model
from keras.layers import Input, Dense, TimeDistributed, LSTM, Dropout, Activation
from keras.layers import Convolution2D, MaxPooling2D, Flatten, AveragePooling2D, ZeroPadding2D
from keras.layers.normalization import BatchNormalization
from keras.layers.advanced_activations import ELU, LeakyReLU
from keras.callbacks import ModelCheckpoint
from keras import backend
from keras import metrics
from keras.utils import np_utils
from keras.optimizers import Adam
from keras.optimizers import SGD
from keras.callbacks import ModelCheckpoint

from sklearn.preprocessing import normalize
from sklearn.metrics import matthews_corrcoef
from sklearn.metrics import hamming_loss, coverage_error, label_ranking_average_precision_score, label_ranking_loss
from keras import optimizers
from timeit import default_timer as timer


from pyaudio import PyAudio, paContinue, paFloat32
from time import sleep
from numpy import array, random, arange, float32, float64, zeros
import matplotlib.pyplot as plt
import sounddevice as sd

import time as time2
import OSC


# Code to run the AI VJ. currently v2



############################
#     Constants
############################

pattern_osc_route = '/lx/channel/1/activePattern'
color_osc_route = '/lx/palette/color/hue'
speed_osc_route = '/lx/engine/speed'
blur_osc_route = '/lx/channel/1/effect/1/amount/'
desat_osc_route = '/lx/channel/1/effect/2/amount/'
blur_osc_boolean = '/lx/channel/1/effect/1/enabled/'
desat_osc_boolean = '/lx/channel/1/effect/2/enabled/'

brightness_osc_route = '/lx/output/brightness'
noise_density_osc_route = '/lx/channel/1/pattern/10/Dens/' #default 0.7
cubeflash_speed_osc_route = '/lx/channel/1/pattern/9/RATE/' #default 0.3
pattern_chan2_osc_route = '/lx/channel/2/activePattern'
output_on_osc_route = '/lx/output/enabled'
transition_osc_route = '/lx/channel/1/transitionEnabled'
transition_duration_osc_route = '/lx/channel/1/transitionTimeSecs'

#WEIGHTS_FOLDER = '/Users/aaronopp/Desktop/SymmetryLabs/ML_model/model_weights/v2/'
AI_VJ_FOLDER = '/Users/aaronopp/Desktop/AI_VJ/'

WEIGHTS_FOLDER = '/Users/aaronopp/Desktop/AI_VJ/model_weights/v2/'
TRAINING_DATA_FOLDER = '/Users/aaronopp/Desktop/AI_VJ/training_data/'

DATA_FOLDER = TRAINING_DATA_FOLDER + sys.argv[1] + '/'
PROCESSING_OUTPUT = AI_VJ_FOLDER + 'logger/data/out.txt'


if not os.path.exists(DATA_FOLDER):
    print 'folder created'
    os.makedirs(DATA_FOLDER)

#DATA_FOLDER = '/Users/aaronopp/Desktop/AI_VJ/training_data/v2/'


print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)
print sd.query_devices()
print 'default devices', sd.default.device

############################
# initiate OSC client
############################

c = OSC.OSCClient()
c.connect(('0.0.0.0', 3030))   # connect to SuperCollider

#X_train = np.load(DATA_FOLDER + 'X_train_v2.npy')
X_train_shape = np.array((1765, 1, 96, 938))

Y_pattern_train_shape = np.array((1765, 1, 96, 938))
Y_color_train_shape = np.array((1765, 1, 96, 938)) #np.load(DATA_FOLDER + 'Y_color_train_v2.npy')
Y_speed_train_shape = np.array((1765, 1, 96, 938)) #np.load(DATA_FOLDER + 'Y_speed_train_v2.npy')

X_train_small_shape = np.array((856, 1, 96, 313)) #np.load(DATA_FOLDER + 'X_train_small_v2.npy')
Y_blur_train_shape = np.array((856, 1, 96, 313)) #np.load(DATA_FOLDER + 'Y_blur_v2.npy')
Y_desat_train_shape = np.array((856, 1, 96, 313)) #np.load(DATA_FOLDER + 'Y_desat_v2.npy')

Y_speed_classes = 3
Y_color_classes = 8
Y_pattern_classes = 24
Y_effect_classes = 4

print 'checking shapes'
print 'pattern shape: ', Y_pattern_train_shape
print 'color shape: ', Y_color_train_shape
print 'speed shape: ', Y_speed_train_shape

print 'comparing all lengths!'
print 'x test trimmed shape: ', X_train_shape

print 'x test trimmed shape: ', X_train_small_shape
#print 'Y_param shape: ', Y_param_full.shape
print 'Y_blur shape: ', Y_blur_train_shape
print 'Y_desat shape: ', Y_desat_train_shape

############################
# build and load models
############################

# big models, 15 sec

model_speed = build_model_linear_end(X_train_shape, Y_speed_train_shape, nb_classes= Y_speed_classes)          
model_color = build_model_linear_end(X_train_shape, Y_color_train_shape, nb_classes= Y_color_classes)
model_pattern = build_model_linear_end_pattern(X_train_shape, Y_pattern_train_shape, nb_classes = Y_pattern_classes)

# v2 MODELS
# small models (5sec)

model_blur = build_model_linear_end(X_train_small_shape, Y_blur_train_shape, nb_classes=Y_effect_classes)
model_desat = build_model_linear_end(X_train_small_shape, Y_desat_train_shape, nb_classes=Y_effect_classes)

# load weights (stable v2)

model_speed.load_weights(WEIGHTS_FOLDER + 'weights_speed_11_2_noval_85.hdf5')
model_color.load_weights(WEIGHTS_FOLDER + 'weights_color_11_2_noval_91.hdf5')
model_pattern.load_weights(WEIGHTS_FOLDER + 'weights_pattern_11_2_noval_84.hdf5')
model_blur.load_weights(WEIGHTS_FOLDER + 'weights_blur_11_2_noval_84.hdf5')
model_desat.load_weights(WEIGHTS_FOLDER + 'weights_desat_11_2_noval_80.hdf5')

###################################
# default osc messages
###################################

send_osc(noise_density_osc_route, 0.8)
send_osc(cubeflash_speed_osc_route, 0.05)
send_osc(output_on_osc_route, 1)
send_osc(transition_osc_route, 1)
send_osc(transition_duration_osc_route, 0.01)

###################################
#         Running test model
###################################

# trying w mel spec size of 15 sec and sample rate every 5 sec
# for sample size of 15 sec, mel size is 938

duration = 15 # seconds
run_time_min = int(sys.argv[2])
run_time = run_time_min*60

X_size = (run_time/5) - 3

#short_term_duration = 5
print 'running for ', run_time_min, ' minutes!'
i = 0
z = 0


x = np.array([],ndmin = 2)

mel_size = 938 # 1872 for 30 sec
RATE = 16000
start = time.time()

b = Buffer(duration * RATE)
#b_small = Buffer(short_term_duration * RATE)
color_old = 0

print 'b read', b.read()
X_test = np.zeros((X_size, 1, 96, mel_size))  # or could be run_time/5 sec
time_test = np.zeros((X_size))

def callback(indata, frames, time, status): #outdata is 5th - when no inputstream
    global i
    global z
    global mel_size
    global run_time
    global speed_labels
    global color_labels_encoding
    global patterns_full, speed_unencode, color_unencode
    global pattern_osc_route, color_osc_route, speed_osc_route
    if status:
        print(status)
    
    b.extend(indata.squeeze())
    #b_small.extend(indata.squeeze())
    elapsed_time = time2.time()- start
    
    
    if elapsed_time > duration and i % 50 == 0:
        #print i
        #print 'time elapsed:', elapsed_time
        aud = b.read()
        Spec = get_mel_spectrogram(aud)
        #print 'spec shape before: ', Spec.shape
        Spec = Spec[:,:,:,0:mel_size]
        #print 'spec shape: ', Spec.shape
        
        Spec_small = Spec[:, :, :, -313:]
        if Spec[:, :, :, -300:].mean() < -96.0:
             print 'music off'
             send_osc(pattern_osc_route, 4)
        else:

            speed_predict = model_speed.predict(Spec)
            color_predict = model_color.predict(Spec)
            pattern_predict = model_pattern.predict(Spec)
            
            blur_predict = model_blur.predict(Spec_small)
            desat_predict = model_desat.predict(Spec_small)

            #print 'max data pos: ' , speed_predict.argmax()
            speed_label, speed_index = get_max_label(speed_predict, speed_labels)
            color_label, color_index = get_max_label(color_predict, color_labels_encoding)
            pattern_label, pattern_index = get_max_label(pattern_predict, patterns_full)
            
            blur_label, blur_index = get_max_label(blur_predict, effect_labels_full)  
            desat_label, desat_index = get_max_label(desat_predict, effect_labels_full)  
            
            print 'speed: ', speed_label
            print 'color: ', color_label
            print 'pattern: ', pattern_label
            print 'blur: ', blur_label
            print 'desat: ', desat_label
            print 'PATTERN MAX READING: ', pattern_predict.max()
            #if (pattern_predict.max() > 0.40):
            send_osc(pattern_osc_route, pattern_index)
            send_osc(color_osc_route, color_unencode[color_index])
            send_osc(speed_osc_route, speed_unencode[speed_index])
            send_osc(blur_osc_route, effect_unencode[blur_index])
            send_osc(desat_osc_route, effect_unencode[desat_index])
            
            X_test[z, :,:,:] = Spec
            time_test[z] = time2.time()
            color_old = color_index
       
        z += 1

    i += 1
with sd.InputStream(samplerate=16000, dtype= np.float32, channels=1, callback=callback):
   
    sd.sleep(int(run_time*1000))

    print 'saving data to ', DATA_FOLDER

    np.save(DATA_FOLDER + 'x_train_run_toggle.npy', X_test)
    time_test_scaled = time_test * 1000
    np.save(DATA_FOLDER + 'time_test_scaled_run_toggle.npy', time_test_scaled)
    print 'saved!'

    print 'copying file at: ', PROCESSING_OUTPUT, 'and pasting at: ', DATA_FOLDER

    shutil.copy(PROCESSING_OUTPUT, DATA_FOLDER + 'logger_output_run_toggle.txt')
    #os.rename(DATA_FOLDER + 'out.txt', 'logger_output_run_toggle.txt')

    
    print 'saved!'

###################################################
#     pre-process extra runtime training data
###################################################

#data_file = (DATA_FOLDER + 'logger_output_run_toggle.txt')
#dataframe_f = logger_to_training_data(data_file)

#dataframe_matched, X_test_matched = match_training_dataset(dataframe_f, time_test_scaled, X_test)

#dataframe_toggle, toggle_timestamps, toggle_indexes, X_test_toggle = drop_runtime_toggle(dataframe_matched, X_test_matched)


#Y_color_toggle, Y_speed_toggle, Y_pattern = dataframe_to_labels(dataframe_toggle)

#np.save(DATA_FOLDER + 'Y_color_runtime_toggled.npy', Y_color_toggle)
#np.save(DATA_FOLDER + 'Y_speed_runtime_toggled.npy', Y_speed_toggle)
#np.save(DATA_FOLDER + 'Y_pattern_runtime_toggled.npy', Y_pattern_toggle)
#np.save(DATA_FOLDER + 'X_runtime_toggled.npy', X_test_toggle)

