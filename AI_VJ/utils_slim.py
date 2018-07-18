# Utility function module for AI VJ

import os
# import ast

import sys
# import glob

# import json
import numpy as np
# import pandas as pd

import librosa
import librosa.display
# import sounddevice as sd
import pythonosc
from pythonosc import udp_client

from numpy import array, random, arange, float32, float64, zeros
import matplotlib.pyplot as plt
# import sounddevice as sd



module_path = os.path.abspath(os.path.join('..'))
if module_path not in sys.path:
    sys.path.append(module_path)

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


color_range_scaled = np.array([0, 20, 45, 75, 150, 200, 255, 285, 330, 361])
color_range = color_range_scaled/360.

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
def import_data_generation_libraries():
    print('importing data gen libraries')


def set_sounddevices(sd, input_name='Soundflower (2ch)', output_name='Built-in Output'):

    for index, device in enumerate(sd.query_devices()):
    #print device['name']
    #print index
        if device['name'] == input_name:
            print(input_name + 'found at index: ', index)
            sd.default.device[0] = index
        if device['name'] == output_name:
            print(output_name + 'found at index: ', index)
            sd.default.device[1] = index

    print('\n \n \n')
    print(sd.default.device)
    print(sd.query_devices())
    return sd


def graph_MS(data, line):

    if data.ndim == 4:
        S = data[line].reshape(data[line].shape[1:])
    if data.ndim == 3:
        S = data[line].reshape(data[line].shape[0:])
    else:
        S = data
    print(S.shape)
    plt.figure(figsize=(10, 4))
    librosa.display.specshow(librosa.power_to_db(S,
                                              ref=np.max),
                       sr=16000, hop_length=256,  y_axis='mel', fmax=8000,
                          x_axis='time')
    plt.colorbar(format='%+2.0f dB')
    plt.title('Mel spectrogram')
    plt.tight_layout()


def reshape_4_to_2(data, line):
    S = data[line].reshape(data[line].shape[1:])
    return S


def get_mel_spectrogram(aud):

    melgram = librosa.power_to_db(librosa.feature.melspectrogram(aud, sr=16000, n_mels=96, n_fft=512, hop_length=256),ref=1.0)[np.newaxis,np.newaxis,:,:]
    return melgram


def unencode_and_list(data, data_labels):

    from collections import Counter
    print('data length: ', len(data))
    data_list = [None] * len(data)

    for index, row in enumerate(data):
        data_list[index] = (data_labels[row.argmax()])
    cc = Counter(data_list)
    print((list(cc.items())))

##############################
#            OSC
##############################

# osc code from python 2.7
# import OSC
# c = OSC.OSCClient()
# c.connect(('0.0.0.0', 3030))


# def send_osc(route, message):
#     oscmsg = OSC.OSCMessage()
#     oscmsg.setAddress(route)
#     oscmsg.append(message)
#     c.send(oscmsg)

client = udp_client.SimpleUDPClient("0.0.0.0", 3030)


def send_osc(route, message):

    client.send_message(route, message)

##############################
#        RING BUFFER
##############################


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
