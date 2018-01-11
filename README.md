# AI VJ extension of SLStudio

This branch adds AI VJ functionality to SLStudio. You can run the algorithm, which will generate patterns automatically based on high level features of the music it is hearing. You can also generate new training data to be fed into the algorithm to make it more robust. 

Training data for the algorithm consists of audio data and timestamps, recorded through a macbook soundcard, and OSC messages that are sent out when any parameter (color, speed, pattern, effects, etc.) is changed in the UI.

## Getting Started

### Prerequisites

Install/update homebrew

Create an anaconda environment with Python 2.7
https://www.anaconda.com/download/

Pip
```
sudo easy_install pip
```
Soundflower

https://github.com/mattingalls/Soundflower/releases/download/2.0b2/Soundflower-2.0b2.dmg

Soundflower allows you to record audio through the soundcard. When this output is selected (soundflower 2ch), no music will be played through the speakers. In order to play through speakers/headphones at the same time as soundflower, you need to create an Multi-output device with mac

https://lifehacker.com/5933282/how-to-aggregate-and-use-multiple-audio-inputs-and-outputs-in-os-x

Add soundflower (2ch) and built in output

Portaudio

```
brew install portaudio
```

Python package requirements
```
pip install -r requirements.txt
```

** Librosa
If you get an error importing the librosa library, you may need to upgrade it 
```
pip install -—upgrade librosa
```
** Keras 1.2.1
Edit the keras.json file (“nano /$HOME/.keras/keras.json") for mac users to be exactly the config shown below!
```
{
"image_dim_ordering": "th",
  "epsilon": 1e-07,
    "floatx": "float32",
    "image_data_format": "channels_last",
    "backend": "theano"
}
```

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

1.  Rename or delete existing logger file @ data/out.txt 
2. 	Build SLStudio in ai-vj or winter-sun-ai-vj branch
3. 	Make sure OSC input/output are on
4. 	Click AI VJ button to start generating training data
5. 	Click the run button to run the AI VJ


### More about scripts

How training data is saved
- After each generation session that lasts x (5) minutes (hard set in UIOverwrittenRightPane.java right now, audio data, timestamps, and SLStudio logged data are all saved in a folder whose path is <your_name>/<current_date>/ 
- The time of day is tagged after the filename
Data_generation.py 
- Run ‘data_generation.py <your_name> <run_time_minutes>
- Example ‘data_generation.py aaron 5’
- Runs audio capture for 5 min, and saves to file <your_name>/<current_date>/
run.py
- Runs pretrained AI_VJ model


