#!/bin/bash



echo 'installing homebrew'
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
echo 'installing pip'
sudo easy_install pip
brew install wget --with-libressl
echo 'downloading soundflower - please install the dmg'
wget https://github.com/mattingalls/Soundflower/releases/download/2.0b2/Soundflower-2.0b2.dmg
echo 'installing portaudio'
brew install portaudio
echo 'installing pipenv'
sudo pip install pipenv
echo 'setting up vj python env'
sudo pipenv install


echo 'entering the env'
pipenv shell # may need to move right below install pipenv

echo 'configging matplotlib backend'
echo "backend: TkAgg" >> ~/.matplotlib/matplotlibrc
echo 'configuring keras backend'

python -c "import keras; string = "keras.__file__"; print string"

rm /$HOME/.keras/keras.json
cp keras.json /$HOME/.keras/ 

#echo 'entering the env'
#pipenv shell # may need to move right below install pipenv
echo 'running test script'

echo 'manual changes to spotipy library - updating client.py file \n'
sh ./spotipy_client_paste.sh

echo ' \n please re-read script output and make sure things were correctly installed \n'
echo 'now getting sounddevices- you need to set the output to soundflower (2ch) in python utils script'
python check_sounddevices.py

echo 'if this is your first time using ai vj, you will need to configure a multi output device' 
echo '\n check out the readme for more info \n'
#sh ./test_script.sh
