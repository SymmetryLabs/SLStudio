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

from shutil import copyfile

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


time_test_scaled = np.load(DATA_FOLDER + 'time_test_scaled_raw.npy')
X_train = np.load(DATA_FOLDER + 'x_train_raw.npy')

AI_VJ_FOLDER = '/Users/aaronopp/Desktop/AI_VJ/'
TRAINING_DATA_FOLDER = '/Users/aaronopp/Desktop/AI_VJ/training_data/'
PROCESSING_OUTPUT = AI_VJ_FOLDER + 'data/out.txt'

DATA_FOLDER = TRAINING_DATA_FOLDER + sys.argv[1] + '/'

#####################################################
#                GET data FROM LOGGER
#####################################################

data_file = "/Users/aaronopp/Desktop/logger/data/out.txt"
%pwd


# ======== real workflow =========

# 1 convert logger data to dataframe

dataframe_f = logger_to_training_data(data_file)

# 2, match dataframe with recorded audio data
print 'dataframe created from raw data, length: ' , len(dataframe_f)

dataframe_matched, X_test_matched = match_training_dataset(dataframe_f, time_test_scaled, X_test)

print 'dataframe matched with audio data, length: ' , len(dataframe_matched)
# 2a, take out data where the lights weren't actually on
try:
    dataframe_trimmed, dark_timestamps, dark_indexes, X_test_trimmed = drop_darkness(dataframe_matched, X_test_matched)
except:
    dataframe_trimmed = dataframe_matched
#print 'dataframe trimmed- when master is only on with audio data, length: ' , len(dataframe_trimmed)

# 3, create training data from labels
Y_color, Y_speed, Y_pattern = dataframe_to_labels(dataframe_trimmed)

print 'labels created', Y_pattern

# ========== workflow to create param data ============

# PARAM data is hyperparameters for each pattern

dataframe_f_param = logger_to_param_data(data_file)
dataframe_matched_param, X_test_matched_param = match_training_dataset(dataframe_f_param, time_test_scaled, X_test)
dataframe_trimmed_param, X_trimmed_param = drop_darkness_post(dataframe_matched_param, X_test_matched_param, dark_indexes, dark_timestamps)

Y_param, Y_param_encoded = create_Y_param(dataframe_trimmed_param)

# ========== workflow to create effect data ============

# effect data is EFFECTS in LXStudio (blur, desat, etc.)
# 1 = blur, 2 = desat, 3 = LSD  ---- FOR NOW


Y_blur, Y_blur_encoded = create_Y_effect(dataframe_trimmed, 1)
Y_desat, Y_desat_encoded = create_Y_effect(dataframe_trimmed, 2)
#try:
    #Y_lsd, Y_lsd_encoded = create_Y_effect(dataframe_trimmed, 3)
#except:
    #print 'no lsd'

# ========== CREATE BRIGHTNESS AND CHANNEL 2 DATA ======    
Y_channel_2_boolean, Y_pattern_2, Y_pattern_2_encoded, Y_channel_2_alt, Y_channel_2_alt_encoded, channel_2_indexes = create_Y_channel_2(dataframe)
Y_brightness, Y_brightness_encoded = create_Y_brightness(dataframe)

print 'comparing all lengths!'
print 'x test trimmed shape: ', X_test_trimmed.shape
print 'Y color shape: ', Y_color.shape
print 'Y speed shape: ', Y_speed.shape
print 'Y pattern shape: ', Y_pattern.shape
print 'Y_param shape: ', Y_param_encoded.shape
print 'Y_blur shape: ', Y_blur_encoded.shape
print 'Y_desat shape: ', Y_desat_encoded.shape
print 'Y_brightness shape: ', Y_brightness_encoded.shape
print 'Y_pattern_2 shape: ', Y_pattern_2_encoded.shape

print 'comparing firsts'
print 'X test first and last 5'
print X_test_trimmed[0:5]
print X_test_trimmed[-5:]
print 'Y color: '
print Y_color[0:5]
print Y_color[-5:]
print 'Y speed: '
print Y_speed[0:5]
print Y_speed[-5:]
print 'Y pattern: '
print Y_pattern[0:5]
print Y_pattern[-5:]
print 'Blur: '
print Y_blur[0:5]
print Y_blur[-5:]
print 'Desat: '
print Y_desat[0:5]
print Y_desat[-5:]
print 'Brightness: '
print Y_brightness[0:5], Y_brightness[-5:]
print 'Channel 2: '
print Y_pattern_2[0:5] , Y_pattern_2[-5:]

###################################################################
##################   SAVE TRAINING DATA   #########################
###################################################################


print 'copying raw logger data to folder'

print 'saving all Y training data as np arrays'

np.save(DATA_FOLDER + 'Y_color_train.npy', Y_color)
np.save(DATA_FOLDER + 'Y_speed_train.npy', Y_speed)
np.save(DATA_FOLDER + 'Y_pattern_train.npy', Y_pattern)
np.save(DATA_FOLDER + 'Y_blur_train.npy', Y_blur_encoded)
np.save(DATA_FOLDER + 'Y_desat_train.npy', Y_desat_encoded)
np.save(DATA_FOLDER + 'Y_param_train.npy', Y_param_encoded)

print 'saving all dataframes as csvs'
dataframe_trimmed_param.to_csv(DATA_FOLDER + 'dataframe_trimmed_param.csv')
dataframe_trimmed.to_csv(DATA_FOLDER + 'dataframe_trimmed.csv')
dataframe_f.to_csv(DATA_FOLDER + 'dataframe_raw.csv')


np.save(DATA_FOLDER + 'X_train_raw.npy', X_test)
np.save(DATA_FOLDER + 'X_train.npy', X_test_trimmed)
np.save(DATA_FOLDER + 'timestamps.npy', time_test_scaled)




