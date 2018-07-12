
#from utils import *
import sounddevice as sd
import datetime


print 'making sure sounddevice has the right devices selected'
print '> is input, < is output'
print 'input should be soundflower (2ch)'
print sd.query_devices()
print sd.default.device
print 'PLEASE set input to soundflower (2ch) in the utils file \n \n \n'
for index, device in enumerate(sd.query_devices()):
    #print device['name']
    #print index
    if device['name'] == 'Soundflower (2ch)':
        print 'Soundflower 2ch found at index: ', index
        sd.default.device[0] = index
    if device['name'] == 'Built-in Output':
        print 'Built-in output found at index: ', index
        sd.default.device[1] = index

print '\n \n \n'
print sd.default.device
print sd.query_devices()
