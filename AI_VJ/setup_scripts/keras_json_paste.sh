#!/bin/bash


OUTPUT="$(python -c "import keras; string = "keras.__file__"; print string[:-12]")"
echo "${OUTPUT}"
#cp keras.json ${OUTPUT}

# Other way to do it!
#mkdir -p ~/.keras
#echo '{"epsilon":1e-07,"floatx":"float32","backend":"theano", "image_dim_ordering": "th"}' > ~/.keras/keras.json

