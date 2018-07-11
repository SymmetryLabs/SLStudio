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

import OSC

#%matplotlib inline
#%load_ext autoreload

#%autoreload
################################
#          constants
################################

fs            = 16000   # Hz
threshold     = 0.8     # absolute gain
delay         = 40      # samples
signal_length = 30     # second
release_coeff = 0.5555  # release time factor
attack_coeff  = 0.5     # attack time factor
dtype         = float32 # default data type
block_length  = 1024    # samples

# sd default in is mic, output is built in output
# change the default to 2 (soundflower)

print sd.query_devices()
sd.default.device[0] = 3
sd.default.device[1] = 1

print 'default devices', sd.default.device

import time as time2
print 'current time', time.time()

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
effect_unencode = [0.0, 0.3, 0.6, 0.9]


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


################################
#           general
################################

def encode_classes(class_name, class_names):  
    # makes a "one-hot" vector for each class - Multi-class implementation
    vec = np.zeros(len(class_names))
    for classes in class_name:
        print classes
        
        try:
            idx = class_names.index(classes)
            #vec = np.zeros(len(class_names))
            vec[idx] = 1
        except ValueError:
            return None
    return vec
def encode_class(class_name, class_names):  
    # makes a "one-hot" vector for each class name - Single class implementation
    try:
        idx = class_names.index(class_name)
        vec = np.zeros(len(class_names))
        vec[idx] = 1
        return vec
    except ValueError:
        return None
def get_spec_from_mp3(audio_path):
    #audio_path = '/Users/aaronopp/Downloads/440Hz_44100Hz_16bit_05sec.mp3'
    #try:
    aud, sr = librosa.load(audio_path, mono=True, sr=16000)
    # print('samplerate:', sr)
    melgram = librosa.logamplitude(librosa.feature.melspectrogram(aud, sr=sr, n_mels=96, n_fft=512, hop_length=256),ref_power=1.0)[np.newaxis,np.newaxis,:,:]

    return melgram

        
def graph_MS(data, line):
    if data.ndim == 4:
        S = data[line].reshape(data[line].shape[1:])
    if data.ndim == 3:
        S = data[line].reshape(data[line].shape[0:])
    else:
        S = data
    print S.shape
    plt.figure(figsize=(10, 4))
    librosa.display.specshow(librosa.power_to_db(S,
                                              ref=np.max),
                       sr=16000, hop_length=256,  y_axis='mel', fmax=8000,
                          x_axis='time')
    plt.colorbar(format='%+2.0f dB')
    plt.title('Mel spectrogram')
    plt.tight_layout()

def graph_MS_non_notebook(data, line):
    if data.ndim == 4:
        S = data[line].reshape(data[line].shape[1:])
    if data.ndim == 3:
        S = data[line].reshape(data[line].shape[0:])
    else:
        S = data
    print S.shape
    plt.figure(figsize=(10, 4))
    librosa.display.specshow(librosa.power_to_db(S,
                                              ref=np.max),
                       sr=16000, hop_length=256,  y_axis='mel', fmax=8000,
                          x_axis='time')
    plt.colorbar(format='%+2.0f dB')
    plt.title('Mel spectrogram')
    plt.tight_layout()
    plt.show()
    
def graph_MS_44k(data, line):
    S = data[line].reshape(data[line].shape[1:])
    print S.shape
    plt.figure(figsize=(10, 4))
    librosa.display.specshow(librosa.power_to_db(S,
                                              ref=np.max),
                       sr=44100, hop_length=256,  y_axis='mel', fmax=8000,
                          x_axis='time')
    plt.colorbar(format='%+2.0f dB')
    plt.title('Mel spectrogram')
    plt.tight_layout()
def reshape_4_to_2(data, line):
    S = data[line].reshape(data[line].shape[1:])
    return S

def get_mel_spectrogram(aud):
    melgram = librosa.logamplitude(librosa.feature.melspectrogram(aud, sr=16000, n_mels=96, n_fft=512, hop_length=256),ref_power=1.0)[np.newaxis,np.newaxis,:,:]
    return melgram

################################
# related to music tagging model
################################

def get_music_tags(json_path):
    tag_list = []
    with open(json_path) as data_file:
        data = json.load(data_file)
        #print data["tags"]
        for data in data["tags"]:
            #print 'data type', type(data[1])
            
            rating_int = int(data[1])
            #rating_int = rating_string.toInt
            #print 'rat int', rating_int
            if rating_int > 50:
                #print 'tags:', data[0]
                #print 'rating:', data[1]
                tag_list.append(data[0])
    #print 'tag_list', tag_list
    return tag_list

            #print data[1]
    #pprint(data)
#def get_music_tags():


def is_top_50(tags):
    top_50_tags = [i for i in tags if i in top_50]
    return top_50_tags

################################
#    data pre-processing
################################


def get_max_label(data, labels):
    print 'max data pos: ' , data.argmax()
    
    return labels[data.argmax()], data.argmax()#, data.max()

def num_to_color_label(num):
    global color_labels
    num = int(num*360)
    print num
    
    i = 0
    for ranges in boundary_range_scaled:
        if i != 0:
            if num in range(old_range, ranges):
                #print 'yes!!!!'
                print color_labels[i-1]

                break
            #print i, old_range
        
        #print ranges
        old_range = ranges
        i += 1
    return 


def td_to_color_label(Y_logger):
    global color_labels
    for i in range(0, Y_logger.shape[0]):    
        num = int(num*360)
        print num

        i = 0
        for ranges in boundary_range_scaled:
            if i != 0:
                if num in range(old_range, ranges):
                    #print 'yes!!!!'
                    print color_labels[i-1]

                    break
                #print i, old_range

            #print ranges
            old_range = ranges
            i += 1
    return 
def csv_to_dataframe(data_file):
    
    data = pd.read_csv(data_file, names = ["lx_route", "data", "raw_reading", "osc_reading", "timestamp"])
    timestamps = data.timestamp.str.extract('(\d+)').fillna(0).astype(int)
    print data.shape
    data_2 = data.set_index(timestamps)
    data_2 = data_2[~data_2.index.duplicated(keep='first')]
    print data_2.shape
 
    raw_data_size = data_2.shape[0]
    
    raw_data_list = data_2.lx_route.str.split('lx/').tolist()
    raw_data_list2 = data_2.lx_route.str.split('lx/', expand=True)
    raw_data_list2.columns = ['pre_route', 'route']
    training_data = raw_data_list2.drop('pre_route', 1)
    training_data = training_data.join(data_2.data)
    print training_data.shape
    #training_data_red = trainind_data.str.replace('data:', '')

    training_data_pivoted = training_data.pivot(columns = 'route', values = 'data')
    # -------- WORKING TDP MAKER -----------------



    td_p = training_data_pivoted.replace('data:', '', regex= True)
    
    try:
        td_p.drop([None], 1, inplace = True)
    except:
        print 'no nan!'
    
    for col in td_p:
        #col = col.strip('"')
        td_p[col] = td_p[col].str.split('[').str[-1]
        #td_p[col] = td_p[col].str.split(']').str[-1]
        td_p = td_p.replace(']', '', regex= True)
        td_p = td_p.replace('"', '', regex= True)
        
        #col = col.strip('"')
    for index in td_p.index:
        if index < 1000000000000:
            print 'dropping rows with indexes: ', index
            td_p.drop(index, inplace=True)
    td_p.rename(columns=lambda x: x.strip('"'), inplace = True)
    return td_p

def fill_none(dataframe):
    placeholder = 0 
    j = 0 
    for col in dataframe:
        i = 0
        placeholder = 0
        
        for row in dataframe[col]: 
            if row != None:
                placeholder = row
            else:
                dataframe.iloc[i, dataframe.get_loc(col)] = placeholder

                print row , '=',  placeholder
            i += 1
        j += 1

def hue_to_color(num):
    num = int(num*360)
    #print num
    j = 0
    for ranges in boundary_range_scaled:
        if j != 0:
            if num in range(old_range, ranges):
                print color_labels[j-1]
                current_color = color_labels[j-1]
                #print i
                break
            #print i, old_range

        #print ranges
        old_range = ranges
        j += 1
    return current_color


def speed_to_label(speed):
    #old_range = 0 
    j = 0 
    #current_speed = []
    speed = int(speed*100)
    print speed
    for ranges in speed_range_scaled:
        if j != 0:

            if speed in range(old_range, ranges):
                print speed_labels[j-1]
                return speed_labels[j-1]
                break
        j += 1
        old_range = ranges

        
def effect_to_label(effect):
    
    effect_labels = ['low', 'medium', 'high']
    #old_range = 0 
    j = 0 
    #current_speed = []
    effect = int(effect*100)
    print effect
    for ranges in speed_range_scaled:
        if j != 0:

            if effect in range(old_range, ranges):
                print effect_labels[j-1]
                return effect_labels[j-1]
                break
        j += 1
        old_range = ranges

def brightness_to_label(effect):
    
    brightness_labels = ['off', 'half', 'full']
    #old_range = 0 
    j = 0 
    #current_speed = []
    effect = int(effect*100)
    print effect
    for ranges in speed_range_scaled:
        if j != 0:

            if effect in range(old_range, ranges):
                print brightness_labels[j-1]
                return brightness_labels[j-1]
                break
        j += 1
        old_range = ranges

        #speed_range_scaled = [0, 34, 67, 101]
        
def get_unique_tags(data):    # to get unique music tags in a list!
    unique_tags = []
    for i in range(len(data)):

        tags_test = data[i]
        print tags_test

        if tags_test != 0:
            # if multiple labels...
            # for x in tags_test:
            if tags_test not in unique_tags:
                unique_tags.append(tags_test)
        
    print 'unique tags', unique_tags
    return unique_tags

def logger_to_training_data(data_file):
    datan = pd.read_csv(data_file, sep="]", header=None, error_bad_lines=False)
    datan_2 = pd.DataFrame(datan[0].str.rsplit(',',1).tolist(), columns = ['lx_route','data'])
    datan_ts = pd.DataFrame(datan[1].str.rsplit(',',1).tolist(), columns = ['raw','timestamps'])
    timestamps2 = datan_ts['timestamps'].str.extract('(\d+)').fillna(0).astype(int)
    #datan_2.set_index(timestamps2)
    for col in datan_2:
        datan_2 = datan_2.replace('{"route":"', '', regex= True)
        datan_2 = datan_2.replace('"data":', '', regex= True)
        datan_2 = datan_2.replace('"', '', regex= True)
        if col == 'lx_route':
            datan_2[col] = datan_2[col].str.split(',').str[0]
        else: 
            datan_2[col] = datan_2[col].str.split('[').str[-1]  


    datan_3 = datan_2.set_index(timestamps2)
    datan_3 = datan_3[datan_3.lx_route != '/lx/channel/1/nextPattern']
    datan_3 = datan_3[~datan_3.index.duplicated(keep='last')]

    training_data_pivoted = datan_3.pivot(columns = 'lx_route', values = 'data')
    td_p = training_data_pivoted
    
    placeholder = 0 
    j = 0 
    for col in td_p:
        i = 0
        placeholder = 0
        if 'activePattern' in col or 'hue' in col or 'speed' in col or 'enabled':
            for row in td_p[col]:
                #print row
                if row != None:
                    placeholder = row
                    #print row
                else:
                    #print placeholder /lx/output/enabled
                    #td_p.loc[col, row] = placeholder
                    #td_p.loc[i, col] = placeholder
                    td_p.iloc[i, td_p.columns.get_loc(col)] = placeholder

                    #print row , '=',  placeholder
                i += 1
            j += 1
     
    return td_p

def logger_to_param_data(data_file):
    datan = pd.read_csv(data_file, sep="]", header=None, error_bad_lines=False)
    datan_2 = pd.DataFrame(datan[0].str.rsplit(',',1).tolist(), columns = ['lx_route','data'])
    datan_ts = pd.DataFrame(datan[1].str.rsplit(',',1).tolist(), columns = ['raw','timestamps'])
    timestamps2 = datan_ts['timestamps'].str.extract('(\d+)').fillna(0).astype(int)
    #datan_2.set_index(timestamps2)
    for col in datan_2:
        datan_2 = datan_2.replace('{"route":"', '', regex= True)
        datan_2 = datan_2.replace('"data":', '', regex= True)
        datan_2 = datan_2.replace('"', '', regex= True)
        if col == 'lx_route':
            datan_2[col] = datan_2[col].str.split(',').str[0]
        else: 
            datan_2[col] = datan_2[col].str.split('[').str[-1]  


    datan_3 = datan_2.set_index(timestamps2)
    datan_3 = datan_3[datan_3.lx_route != '/lx/channel/1/nextPattern']
    datan_3 = datan_3[~datan_3.index.duplicated(keep='last')]

    training_data_pivoted = datan_3.pivot(columns = 'lx_route', values = 'data')
    td_p = training_data_pivoted
    return td_p
   


def dataframe_to_labels(Y_logger_df):

######################################################################
#                   Raw values to labels
######################################################################
    
    print 'converting dataframe to labels'
    print 'dataframe length: ', len(Y_logger_df)
    print 'dataframe columns: ', Y_logger_df.columns
    
    color_labels = ['red', 'orange', 'yellow', 'green', 'teal', 'blue', 'purple', 'magenta', 'red']
    color_labels_encoding = ['red', 'orange', 'yellow', 'green', 'teal', 'blue', 'purple', 'magenta']
    speed_range_scaled = [0, 34, 67, 101]
    speed_labels = ['slow', 'medium', 'fast']




######################################################################
#         create individual lists and pattern label array
######################################################################
    color_list = []
    speed_list = []
    pattern_list = []
    
    if '/lx/palette/color/hue' in Y_logger_df.columns:
        print 'hue is in dataframe, making labels'

        for i in Y_logger_df['/lx/palette/color/hue']:
            #print type(i)
            color_list.append(hue_to_color(float(i)))
    else:
        print 'hue is not in dataframe, moving on'

    if '/lx/engine/speed' in Y_logger_df.columns:

        for i in Y_logger_df['/lx/engine/speed']:
            speed_list.append(speed_to_label(float(i)))
    else:
        print 'speed not in dataframe, moving on'
        
    if '/lx/channel/1/activePattern' in Y_logger_df.columns:

        for i in Y_logger_df['/lx/channel/1/activePattern']:
            pattern_list.append(i)
    else:
        print 'patterns not in dataframe, moving on'

    pattern_labels = get_unique_tags(pattern_list)

    Y_color = np.zeros((len(color_list), len(color_labels_encoding)))
    Y_speed = np.zeros((len(speed_list), len(speed_labels)))
    
    # ------ GLOBALLY encoded patterns ------
    
    Y_pattern = np.zeros((len(pattern_list), len(patterns_full)))
    
    # ------ OLD way of locally encoding patterns -------
    
    #Y_pattern = np.zeros((len(pattern_list), len(pattern_labels)))

    for index, label in enumerate(color_list):
        #print type(i)
        #print 'ind' , index
        #print label
        Y_color[index] = encode_class(label, color_labels_encoding)

    for index, label in enumerate(speed_list):
        #print type(i)
        #print 'ind' , index
        #print label
        Y_speed[index] = encode_class(label, speed_labels)
    for index, label in enumerate(pattern_list):
        #print type(i)
        #print 'ind' , index
        #print label
        Y_pattern[index] = encode_class(label, patterns_full)


    return Y_color, Y_speed, Y_pattern


def match_training_dataset_old(td_p, time_test_scaled):
    #time_test = time_test * 1000

    Y_logger = np.zeros((21, td_p.shape[1]))
    print 'input data length', len(td_p)
    Y_logger_df = pd.DataFrame(data=None, columns=td_p.columns)
    j = 0
    old_index = 0
    old_value = td_p.index[0]
    for timestamps in time_test_scaled:
        for index, value in enumerate(list(td_p.index)):
            #print timestamp_array[8]
            if value > timestamps:
                print old_value, value
                print 'index: ', index
                print timestamps
                Y_logger_df = Y_logger_df.append(td_p.loc[old_value]) #ignore_index=True
                #Y_logger[j] = td_p.loc[old_value].as_matrix()
                j += 1
                break
                #print index
            old_index = index
            old_value = value
    print Y_logger_df
    return Y_logger_df

def match_training_dataset(td_p, time_test_scaled, X_test):
    
    # function to match the dataframe with LX data and the audio data by timestamp
    # notice that we have to scale the audio timestamps by multiplying it by 1000
    # when the timestamp of the logger data passes the each audio timestamp, we append
    # the reading before the audio timestamp to the new logger data...
    # this is because that was the current LX settings when that audio timestamp occured 
    
    #time_test = time_test * 1000
    X_test_trimmed = np.zeros((len(X_test), 1, 96, 938))
    Y_logger = np.zeros((21, td_p.shape[1]))
    print 'input data length', len(td_p)
    
    Y_logger_df = pd.DataFrame(data=None, columns=td_p.columns)
    j = 0
    old_index = 0
    old_value = td_p.index[0]
    for index_logger, timestamps in enumerate(time_test_scaled):
        for index, value in enumerate(list(td_p.index)):
            #print timestamp_array[8]
            if value > timestamps:
                print old_value, value
                print 'index: ', index
                print timestamps
                Y_logger_df = Y_logger_df.append(td_p.loc[old_value]) #ignore_index=True
                X_test_trimmed[j] = X_test[index_logger]
                #Y_logger[j] = td_p.loc[old_value].as_matrix()
                
                j += 1
                break
                #print index
            old_index = index
            old_value = value
    print 'j:' , j
    print 'trimming new X data'
    X_test_trimmed = X_test_trimmed[0:j]
    print Y_logger_df
    return Y_logger_df, X_test_trimmed

def drop_darkness(dataframe, X_test):
    

    print 'dropping all datapoints where light was not on'
    print 'matching up lengths of logger vs. audio data'
    print 'logger: ', len(dataframe), 'audio: ', X_test.shape
    #X_data_trimmed = X_data
    dataframe_old = dataframe
    dark_timestamps = dataframe.index[dataframe['/lx/output/enabled'] == '0'].tolist()
    dark_indexes = np.zeros(len(dark_timestamps))
    
    # Create a non-duplicated version of dark timestamps
    dark_timestamps_nodup = []
    for i in dark_timestamps:
        if i not in dark_timestamps_nodup:
            dark_timestamps_nodup.append(i)
            
    # get row numbers where there were dark timestamps
    j = 0
    for row_number_ts, value in enumerate(dark_timestamps_nodup):
        #print value
        for row_number, df_index in enumerate(dataframe_matched.index):
            if df_index == value:
                dark_indexes[j] = row_number
                print row_number
                j += 1
               

    # drop all dark timestamped rows from Dataframe logger data
    for dark_index in dark_timestamps:
        dataframe.drop(dark_index, inplace=True)
        #dataframe.index.get_loc(dark_index)

    # drop all dark indexes from music data (X)
    X_test_trimmed = np.delete(X_test, dark_indexes, axis=0)
    print 'new X length:', len(X_test_trimmed)
    
    return dataframe, dark_timestamps, dark_indexes, X_test_trimmed

def drop_darkness_post(dataframe, X_test_matched, dark_indexes, dark_timestamps):
    # drop all dark timestamped rows from Dataframe logger data
    for dark_index in dark_timestamps:
        dataframe.drop(dark_index, inplace=True)
        #dataframe.index.get_loc(dark_index)

    # drop all dark indexes from music data (X)
    X_test_trimmed = np.delete(X_test_matched, dark_indexes, axis=0)
    print 'new X length:', len(X_test_trimmed)
    
    return dataframe, X_test_trimmed
    

    
def create_Y_param(dataframe):
    
    global param_labels_full
    
    Y_effects = [None] * len(dataframe)
    placeholder = 0 
    j = 0 
    for col in dataframe:
        i = 0
        placeholder = 0
        if 'Spd' in col or 'spd' in col or 'RATE' in col or 'Speed' in col or 'speed' in col:
            print 'col: ', col
            for row in dataframe[col]:
                #print row, i
                if row != None and row != '0':
                    #placeholder = row
                    print 'row: ', row, 'col: ', i
                    Y_effects[i] = 'Faster'
                i += 1
        if 'H.V.' in col or 'hShift' in col or 'HUE' in col:
            print 'col: ', col
            for row in dataframe[col]:
                #print row, i
                if row != None and row != '0':
                    #placeholder = row
                    print 'row: ', row, 'col: ', i
                    Y_effects[i] = 'Hue Variation'
                i += 1
        if 'Sprk' in col or 'Spark' in col:  #or 'hue' in col or 'speed' in col or 'enabled':
            print 'col: ', col
            for row in dataframe[col]:
                #print row, i
                if row != None and row != '0':
                    #placeholder = row
                    print 'row: ', row, 'col: ', i
                    Y_effects[i] = 'Sparkle'

                i += 1
        if 'Crzy' in col or 'Wave' in col:
            print 'col: ', col
            for row in dataframe[col]:
                #print row, i
                if row != None and row != '0':
                    #placeholder = row
                    print 'row: ', row, 'col: ', i
                    Y_effects[i] = 'Crazy'
                i += 1
        if 'Dens' in col or 'thck' in col or 'DEPTH' in col or 'Size' in col or 'SIZE' in col:
            print 'col: ', col
            for row in dataframe[col]:
                #print row, i
                if row != None and row != '0':
                    #placeholder = row
                    print 'row: ', row, 'col: ', i
                    Y_effects[i] = 'Larger'
                i += 1
    
    Y_params_encoded = np.zeros((len(Y_effects), len(param_labels_full)))

    for index, label in enumerate(Y_effects):
        Y_params_encoded[index] = encode_class(label, param_labels_full)
   
        
    return Y_effects, Y_params_encoded

def create_Y_effect_old(dataframe, effect_number):
    global effect_labels_full
    Y_effect = [None] * len(dataframe)
    i= 0
    for index, row in dataframe.iterrows():
        print 'row:', row['/lx/channel/1/effect/1/enabled'], 
        if row['/lx/channel/1/effect/' + str(effect_number) + '/enabled'] == '1':
            print float(row['/lx/channel/1/effect/' + str(effect_number) + '/amount'])
            print i
            if row['/lx/channel/1/effect/' + str(effect_number) + '/amount'] == '0':
                print '0 value although its on!'
                break
            Y_effect[i] = effect_to_label(float(row['/lx/channel/1/effect/' + str(effect_number) + '/amount']))
            print 'effect on at: ', row['/lx/channel/1/effect/' + str(effect_number) + '/amount']
        else:
            print 'blur off'
        i += 1    
    #for index, label in enumerate(Y_blur):
        #Y_pattern[index] = encode_class(label, patterns_full)
    
    Y_effect_encoded = [None] * len(Y_effect)
    for index, label in enumerate(Y_effect):
        Y_effect_encoded[index] = encode_class(label, effect_labels_full)
    return Y_effect, Y_effect_encoded

def create_Y_effect(dataframe, effect_number):
    
    global effect_labels_full
    Y_effect = [None] * len(dataframe)
    
    i = 0
    
    for index, row in dataframe.iterrows():
        #print 'row:', row['/lx/channel/1/effect/1/enabled'], 
        if row['/lx/channel/1/effect/' + str(effect_number) + '/enabled'] == '1':
            #print float(row['/lx/channel/1/effect/' + str(effect_number) + '/amount'])
            #print i
            if row['/lx/channel/1/effect/' + str(effect_number) + '/amount'] != '0':
                #print '0value although its on! - at row: ', i
                #continue
                Y_effect[i] = effect_to_label(float(row['/lx/channel/1/effect/' + str(effect_number) + '/amount']))
            else: 
                print '0 value although its on! - at row: ', i
            #print 'effect on at: ', row['/lx/channel/1/effect/' + str(effect_number) + '/amount']
        else:
            print 'blur off'
        i += 1    
    #for index, label in enumerate(Y_blur):
        #Y_pattern[index] = encode_class(label, patterns_full)

    Y_effect_encoded = np.zeros((len(Y_effect), len(effect_labels_full)))
    
    for index, label in enumerate(Y_effect):
        Y_effect_encoded[index] = encode_class(label, effect_labels_full)
    return Y_effect, Y_effect_encoded

# function to pull Y_channel_2 and Y_pattern_2 from Dataframe
# IF WE want to create 2 separate models (ONE to tell if channel 2 is on and 1 to predict pattern)
# USE Y_channel_2 AS BOOLEAN
# channel 2 indexes to whittle down X_train
# Y_pattern 2 to train only pattern predicter
#
# THE ALTERNATE is to use just Y_pattern_2 and if it predicts a pattern, it turns on channel 2
# Y_pattern_2 is always running

def create_Y_channel_2(dataframe):
    j = 0
    Y_channel_2 = np.zeros(len(dataframe))
    Y_pattern_2 = [None] * len(dataframe)
    Y_channel_2_alt = []
    channel_2_indexes = np.zeros(0)
    for index, row in dataframe.iterrows():
        #print 'row:', row['/lx/channel/2/enabled'], 

        if row['/lx/channel/2/enabled'] == '1':
            if row['/lx/channel/2/activePattern'] != 0:
                Y_pattern_2[j] = row['/lx/channel/2/activePattern']
                channel_2_indexes = np.append(channel_2_indexes, j)
                print row['/lx/channel/2/activePattern']
                Y_channel_2_alt.append(row['/lx/channel/2/activePattern'])
            #print 'pattern 2 on'
            #print j
            Y_channel_2[j] = 1
        else:
            Y_channel_2[j] = 0
        j += 1
        
        Y_pattern_2_encoded = np.zeros((len(Y_pattern_2), len(patterns_full)))
        Y_channel_2_alt_encoded = np.zeros((len(Y_channel_2_alt), len(patterns_full)))
        
    # encode full pattern
    for index, label in enumerate(Y_pattern_2):
        if label == None:
            label = 'Blank'
        if label == 'SolidColor':
            label = 'Palette'
        Y_pattern_2_encoded[index] = encode_class(label, patterns_full)

    # encode only when pattern is on       
    for index, label in enumerate(Y_channel_2_alt):
        if label == None:
            label = 'Blank'
        if label == 'SolidColor':
            label = 'Palette'
        Y_channel_2_alt_encoded[index] = encode_class(label, patterns_full)


    
    return Y_channel_2, Y_pattern_2, Y_pattern_2_encoded, Y_channel_2_alt, Y_channel_2_alt_encoded, channel_2_indexes
        #else:
        
def create_Y_brightness(dataframe):
    j = 0 
    Y_brightness = [None] * len(dataframe) #np.zeros(len(dataframe))
    
    for index, row in dataframe.iterrows():
        Y_brightness[j] = brightness_to_label(float(row['/lx/output/brightness']))
        j += 1
    Y_brightness_encoded = np.zeros((len(Y_brightness), len(brightness_labels)))
    
    for index, label in enumerate(Y_brightness):
        Y_brightness_encoded[index] = encode_class(label, brightness_labels)
    return Y_brightness, Y_brightness_encoded
    
def get_labels(data, top_50):
    z = 0
    for i in range(len(data)):
        k = 0
        for x in data[i]:
            if x == 1:
                #print data[i]
                print 'row:', i , 'index:', k
                print top_50[k]
                z += 1
            k += 1
    if z ==0:
        print 'no labels!'
        #print data[i]
        #print i
        
def get_labels_top(data, top_50):
    for i in range(len(data)):
        k = 0
        for x in data[i]:
            if x == 1:
                #print data[i]
                print 'row:', i , 'index:', k
                print top_50[k]
            k += 1
        #print data[i]
        #print i

    
################################
#    machine learning models
################################

# ML Stuff

def build_model_local(X_shape,Y_shape,nb_classes):
    #print("ALL THE SHAPES", X.shape, Y.shape, nb_classes)

    
    nb_filters = 128  # number of convolutional filters to use
    # pool_size = (2, 2)  # size of pooling area for max pooling - OLD
    
    pool_size = (4, 7)
    kernel_size = (3, 3)  # convolution kernel size
    nb_layers = 4
    #input_shape = (1, X.shape[2], X.shape[3])
    input_shape = (1, X_shape[2], X_shape[3])

    #leaky_relu = keras.layers.advanced_activations.LeakyReLU(alpha=0.3)

    #print('input shape x', X.shape)
    model = Sequential()
    #layer 1
    #model.add(ZeroPadding2D((1,1),input_shape=input_shape))

    model.add(Convolution2D(32, 3, 3, border_mode='same', input_shape=input_shape))
                        #border_mode='valid')
    #model.add(BatchNormalization(axis=1, mode=2))
    model.add(Activation('relu'))
    #model.add(ELU(alpha=1.0))

    #model.add(LeakyReLU(alpha=0.3)) 
    #model.add(Activation('ELU')) # ELU, leakyRelu
    model.add(MaxPooling2D(pool_size=(2, 5)))
    model.add(Dropout(0.2))
    #layer 2
    #model.add(ZeroPadding2D((1,1)))
    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2))
    #model.add(LeakyReLU(alpha=0.3))
    model.add(Activation('relu'))

    #model.add(ELU(alpha=1.0))
    #model.add(Activation('ELU'))
    model.add(MaxPooling2D(pool_size=pool_size))
    model.add(Dropout(0.2))
    #layer 3
    #model.add(ZeroPadding2D((1,1)))
    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2))
    model.add(Activation('relu'))

    #model.add(LeakyReLU(alpha=0.3))
    #model.add(ELU(alpha=1.0))
    #model.add(Activation('ELU'))
    model.add(MaxPooling2D(pool_size=(3, 8))) # 3, 8
    model.add(Dropout(0.2))
    #layer 4
    #model.add(ZeroPadding2D((1,1)))
    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2)) 
    model.add(Activation('relu'))

    #model.add(LeakyReLU(alpha=0.3))
    #model.add(ELU(alpha=1.0)) 
    #model.add(ELU(alpha=1.0))
    model.add(MaxPooling2D(pool_size=(4, 6))) # 4, 5
    model.add(Dropout(0.2))
    # go LINEAR
    #model.add(AveragePooling2D(pool_size=pool_size))

    model.add(Flatten())
    #model.add(Dense(128))
    #model.add(Activation('relu'))
    #model.add(Dropout(0.2))
    model.add(Dense(nb_classes))
    model.add(Activation("sigmoid"))


    return model


def build_model_linear_end_pattern(X_shape, Y_shape, nb_classes):
    print("ALL THE SHAPES", X_shape, Y_shape, nb_classes)

    
    nb_filters = 128  # number of convolutional filters to use
    # pool_size = (2, 2)  # size of pooling area for max pooling - OLD
    
    pool_size = (4, 7)
    kernel_size = (3, 3)  # convolution kernel size
    nb_layers = 4
    #input_shape = (1, X.shape[2], X.shape[3])
    input_shape = (1, X_shape[2], X_shape[3])

    #leaky_relu = keras.layers.advanced_activations.LeakyReLU(alpha=0.3)

    #print('input shape x', X.shape)
    model = Sequential()
    #layer 1
    #model.add(ZeroPadding2D((1,1),input_shape=input_shape))

    model.add(Convolution2D(32, 3, 3, border_mode='same', input_shape=input_shape))
                        #border_mode='valid')
    #model.add(BatchNormalization(axis=1, mode=2))
    model.add(Activation('relu'))
    #model.add(ELU(alpha=1.0))

    #model.add(LeakyReLU(alpha=0.3)) 
    #model.add(Activation('ELU')) # ELU, leakyRelu
    model.add(MaxPooling2D(pool_size=(2, 4)))
    model.add(Dropout(0.1))
    #layer 2
    #model.add(ZeroPadding2D((1,1)))
    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2))
    #model.add(LeakyReLU(alpha=0.3))
    model.add(Activation('relu'))

    #model.add(ELU(alpha=1.0))
    #model.add(Activation('ELU'))
    model.add(MaxPooling2D(pool_size=(4,6)))

    model.add(Dropout(0.1))

    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2))
    model.add(Activation('relu'))
    model.add(MaxPooling2D(pool_size=(3, 6)))
    
    model.add(Dropout(0.1))
    #layer 3
    #model.add(ZeroPadding2D((1,1)))
    
    model.add(Flatten())

    model.add(Dense(128))
    
    model.add(Activation('relu'))
    model.add(Dropout(0.3))
    model.add(Dense(nb_classes))
    model.add(Activation("softmax"))
    
    return model

def build_model_linear_end(X_shape,Y_shape,nb_classes):
    print("ALL THE SHAPES", X_shape, Y_shape, nb_classes)

    
    nb_filters = 128  # number of convolutional filters to use
    # pool_size = (2, 2)  # size of pooling area for max pooling - OLD
    
    pool_size = (4, 7)
    kernel_size = (3, 3)  # convolution kernel size
    nb_layers = 4
    #input_shape = (1, X.shape[2], X.shape[3])
    input_shape = (1, X_shape[2], X_shape[3])

    #leaky_relu = keras.layers.advanced_activations.LeakyReLU(alpha=0.3)

    #print('input shape x', Xshape)
    model = Sequential()
    #layer 1
    #model.add(ZeroPadding2D((1,1),input_shape=input_shape))

    model.add(Convolution2D(32, 3, 3, border_mode='same', input_shape=input_shape))
                        #border_mode='valid')
    #model.add(BatchNormalization(axis=1, mode=2))
    model.add(Activation('relu'))
    #model.add(ELU(alpha=1.0))

    #model.add(LeakyReLU(alpha=0.3)) 
    #model.add(Activation('ELU')) # ELU, leakyRelu
    model.add(MaxPooling2D(pool_size=(2, 4)))
    #model.add(Dropout(0.25))
    #layer 2
    #model.add(ZeroPadding2D((1,1)))
    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2))
    #model.add(LeakyReLU(alpha=0.3))
    model.add(Activation('relu'))

    #model.add(ELU(alpha=1.0))
    #model.add(Activation('ELU'))
    model.add(MaxPooling2D(pool_size=(4,6)))

    #model.add(Dropout(0.25))

    model.add(Convolution2D(64, 3, 3, border_mode='same'))
    #model.add(BatchNormalization(axis=1, mode=2))
    model.add(Activation('relu'))
    model.add(MaxPooling2D(pool_size=(3, 6)))
    
    #model.add(Dropout(0.25))
    #layer 3
    #model.add(ZeroPadding2D((1,1)))
    
    model.add(Flatten())

    model.add(Dense(128))
    
    model.add(Activation('relu'))
    
    model.add(Dense(nb_classes))
    model.add(Activation("softmax"))
    
    
#     model.add(Convolution2D(64, 3, 3, border_mode='same'))
#     #model.add(BatchNormalization(axis=1, mode=2))
#     model.add(Activation('relu'))

#     #model.add(LeakyReLU(alpha=0.3))
#     #model.add(ELU(alpha=1.0))
#     #model.add(Activation('ELU'))
#     model.add(MaxPooling2D(pool_size=(3, 6))) # 3, 8
#     #model.add(Dropout(0.35))
#     #layer 4
#     #model.add(ZeroPadding2D((1,1)))
#     model.add(Convolution2D(64, 3, 3, border_mode='same'))
#     #model.add(BatchNormalization(axis=1, mode=2)) 
#     model.add(Activation('relu'))

#     #model.add(LeakyReLU(alpha=0.3))
#     #model.add(ELU(alpha=1.0)) 
#     #model.add(ELU(alpha=1.0))
#     model.add(MaxPooling2D(pool_size=(4, 4))) # 4, 5
#     #model.add(Dropout(0.35))
#     # go LINEAR
#     #model.add(AveragePooling2D(pool_size=pool_size))

#     model.add(Flatten())
#     #model.add(Dense(128))
#     #model.add(Activation('relu'))
#     #model.add(Dropout(0.2))
#     model.add(Dense(nb_classes))
#     #model.add(Activation("sigmoid")) SIGMOID IS FOR Multi label classification (multiple predictions) -> binary
#     model.add(Activation("softmax"))


    return model

#model.add(Flatten())


################################
# LOSS FUNCTION DEFINITIONS
################################

def binary_accuracy(y_true, y_pred):
    return K.mean(K.equal(y_true, K.round(y_pred)), axis=-1)

def binary_cross(y_true, y_pred): 
    return K.sum(K.binary_crossentropy(y_pred, y_true), axis=-1)
def binary_accuracy_multi(y_true, y_pred):
    return K.mean(K.equal(y_true, K.round(y_pred)), axis=-1)

# Prediction Functions

def run_predictions(X_train, Y_train, X_test, Y_test):
    predictions_train = model.predict(X_train)
    binary_predictions_train = make_binary(predictions_train, 0.5)
    predictions_test = model.predict(X_test)
    binary_predictions_test = make_binary(predictions_test, 0.5)
    print "predictions against training set:"
    print(classification_report(Y_train, predictions_binary_train))
    print(accuracy_score(Y_train, predictions_binary_train))
    print "predictions against test set:"
    print(classification_report(Y_test, predictions_binary_test))
    print(accuracy_score(Y_test, predictions_binary_test))

def prediction_epochs(epochs, X_train, Y_train, X_test, Y_test):
    for i in range(epochs):
        print 'epoch: ', i
        model.fit(X_train, Y_train, batch_size=batch_size, nb_epoch=1,
               verbose=1, callbacks=[checkpointer]) 
        print 'running prediction on train... '
        
        try_prediction(X_train, Y_train)
        print 'running prediction on test...'
        try_prediction(X_test, Y_test)
        
def try_prediction(X_test, Y_test):
    out = model.predict_proba(X_test)
    out = np.array(out)
    threshold = np.arange(0.1,0.9,0.1)

    acc = []
    accuracies = []
    best_threshold = np.zeros(out.shape[1])
    for i in range(out.shape[1]):
        y_prob = np.array(out[:,i])
        for j in threshold:
            y_pred = [1 if prob>=j else 0 for prob in y_prob]
            acc.append( matthews_corrcoef(Y_test[:,i],y_pred))
        acc   = np.array(acc)
        index = np.where(acc==acc.max()) 
        accuracies.append(acc.max()) 
        best_threshold[i] = threshold[index[0][0]]
        acc = []
    print 'best threshholds:', best_threshold

    total_correctly_predicted = len([i for i in range(len(Y_test)) if (Y_test[i]==y_pred[i]).sum() == 5])
    print 'total correct:', total_correctly_predicted
    y_pred = np.array([[1 if out[i,j]>=best_threshold[j] else 0 for j in range(Y_test.shape[1])] for i in range(len(Y_test))])

    print 'y pred sample:', y_pred[:5]

    print 'hamming loss:', hamming_loss(Y_test,y_pred)  #the loss should be as low as possible and the range is from 0 to 1
    print 'coverage error:', coverage_error(Y_test, y_pred)
    print 'label ranking avg: ', label_ranking_average_precision_score(Y_test, y_pred)
    print 'label ranking loss', label_ranking_loss(Y_test, y_pred)
    print 'classification report: ', classification_report(Y_test, y_pred)
    return y_pred;

def predict_from_ms(melgram):
    melgram = melgram[:,:,:,0:1872]
    out = model.predict_proba(melgram)
    out = np.array(out)
    y_pred = np.array([[1 if out[i,j]>=best_threshold[j] else 0 for j in range(50)] for i in range(len(melgram))])
    labels = get_labels(y_pred, top_50)
    print 'labels: ', labels
    return out, y_pred
def predict_from_ms_scaled(melgram):
    melgram = melgram[:,:,:,0:1872]
    out = model.predict_proba(melgram)
    out = np.array(out)
    out = out*10
    y_pred = np.array([[1 if out[i,j]>=best_threshold[j] else 0 for j in range(50)] for i in range(len(melgram))])
    y_pred_equal_thresh = np.array([[1 if out[i,j]>=0.2 else 0 for j in range(50)] for i in range(len(melgram))])
    
    print 'labels: '#, labels
    
    labels = get_labels(y_pred, top_50)
    
    print 'labels equal thresh: '#, labels_equal_thresh
    labels_equal_thresh = get_labels(y_pred_equal_thresh, top_50)
    return out, y_pred
# LABEL functions


##############################
#            OSC
##############################

c = OSC.OSCClient()
c.connect(('0.0.0.0', 3030))

def send_osc(route, message):
    oscmsg = OSC.OSCMessage()
    oscmsg.setAddress(route)
    oscmsg.append(message)
    c.send(oscmsg)


class Limiter:
    def __init__(self, attack_coeff, release_coeff, delay, dtype=float32):
        self.delay_index = 0
        self.envelope = 0
        self.gain = 1
        self.delay = delay
        self.delay_line = zeros(delay, dtype=dtype)
        self.release_coeff = release_coeff
        self.attack_coeff = attack_coeff

    def limit(self, signal, threshold):
        for i in arange(len(signal)):
            self.delay_line[self.delay_index] = signal[i]
            self.delay_index = (self.delay_index + 1) % self.delay

            # calculate an envelope of the signal
            self.envelope *= self.release_coeff
            self.envelope  = max(abs(signal[i]), self.envelope)

            # have self.gain go towards a desired limiter gain
            if self.envelope > threshold:
                target_gain = (1+threshold-self.envelope)
            else:
                target_gain = 1.0
            self.gain = ( self.gain*self.attack_coeff +
                          target_gain*(1-self.attack_coeff) )

            # limit the delayed signal
            signal[i] = self.delay_line[self.delay_index] * self.gain


# https://stackoverflow.com/questions/8908998/ring-buffer-with-numpy-ctypes
class Buffer(object):
    def __init__(self, size, dtype=np.float32):
        self.size = size
        self.buf = np.zeros(self.size * 2, dtype=dtype)
        self.i = 0
        
    def extend(self, data):
        if len(data.shape) > 1:
            raise ValueError("data must be a flat array")
            
        l = data.size
        if l > self.size:
            raise ValueError("data cannot be larger than size")
            
        start = (self.i % self.size)
        end = start + l
        
        start_2 = start + self.size
        end_2 = end + self.size
        
        self.i += l

        if end < self.buf.size:
            self.buf[start:end] = data


        if end_2 < self.buf.size:
            self.buf[start_2:end_2] = data
        
        
    def read(self):
        start = (self.i % self.size)
        end = start + self.size
        
        return self.buf[start:end]
