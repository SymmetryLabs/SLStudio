# AI VJ extension of SLStudio

This branch adds AI VJ functionality to SLStudio. You can run the algorithm, which will generate patterns automatically based on high level features of the music it is hearing. You can also generate new training data to be fed into the algorithm to make it more robust. 

Training data for the algorithm consists of audio data and timestamps, recorded through a macbook soundcard, and OSC messages that are sent out when any parameter (color, speed, pattern, effects, etc.) is changed in the UI.

A lot of the installation process is automated by a bash script called setup_ai_vj_env.sh 
However, you will have to install soundflower manually and create a multi output device by yourself

### Prerequisites

run the setup script!
```
chmod +x setup_ai_vj_env.sh
./setup_ai_vj_env.sh

```
Soundflower

The setup script automatically downloads the dmg for Soundflower from this link
https://github.com/mattingalls/Soundflower/releases/download/2.0b2/Soundflower-2.0b2.dmg

You'll have to install it manually after that. 

Soundflower allows you to record audio through the soundcard. When this output is selected (soundflower 2ch), no music will be played through the speakers. In order to play through speakers/headphones at the same time as soundflower, you need to create a multi-output device with mac

https://lifehacker.com/5933282/how-to-aggregate-and-use-multiple-audio-inputs-and-outputs-in-os-x

Add soundflower (2ch) and built in output



### Testing

This bash script will run a calibrating script, you should see graphs of mel spectrograms of audio if it works right. You can also check the saved files in the training_data/test_calibration folder

```
./test_script.sh
```

## About Training Data

- Each training session will create 3 important files in a folder name you pass in as an argument to data_generation.py. 

- X_train_raw.npy is raw audio (spectrogram) data collected every 5 seconds. The size of the spectrogram is 15 seconds.

- Time_test_scaled.npy are the timestamps of the raw audio, and will be used to parse the logger to get current LX parameters at each spectrogram

- Logger_training_output_raw.txt is the logger output from LX

### Generating Data

1. 	Make sure your sound output is a multi output device that includes soundflower(2ch) 
2.  On the AI VJ tab - record generates training data, and run starts the AI VJ - you can select how long it runs for in the UI.


