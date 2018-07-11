#!/bin/bash
echo "argument 1 $1, arg 2 $2"
echo "full path with pwd variable"
echo $PWD
python $PWD/data_generation.py test_calibration 1
python $PWD/calibrate.py test_calibration
