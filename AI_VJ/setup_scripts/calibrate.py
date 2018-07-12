
from utils import *
import sounddevice as sd
import datetime

print AI_VJ_FOLDER


print 'making sure sounddevice has the right devices selected'
print '> is input, < is output'
print 'input should be soundflower (2ch)'
print sd.query_devices()
print sd.default.device
print 'make sure spectrograms show up, and are moving 5 sec back each iteration'

TRAINING_DATA_FOLDER = sys.argv[0][:-12] + '/training_data/'
#DATA_FOLDER = TRAINING_DATA_FOLDER + sys.argv[1] + '/'
now = datetime.datetime.now()
DATA_FOLDER = TRAINING_DATA_FOLDER + 'test/'

print 'data folder: ', DATA_FOLDER

timetester = np.load(DATA_FOLDER + 'time_test_scaled_raw_' + str(now.month) + '_'+ str(now.day) + '.npy')
xtester = np.load(DATA_FOLDER + 'x_train_raw_' + str(now.month) + '_'+ str(now.day) + '.npy')

print 'test graphs - make sure 5 seconds pass between each!'

for i in range (2,6):
    graph_MS_non_notebook(xtester[i], 0)
print 'time between each recorded timestamp for audio (should be close to 5000)'
for i in range (2, 6):
	print timetester[i] - timetester[i-1]