# Runs in Python 2.7 (you'll need to pip install pyosc)
#
# To record OSC messages: edit config/config_118.json and/or
# config/config_119.json and set the config["data"]["record"]
# key to a fully qualified path.
#
# To replay a recorded file: python replay.py filename.osc

import OSC
import json
import sys
import time

c = OSC.OSCClient()
start = time.time()
t0 = None

def play(data):
    global t0
    m = OSC.OSCMessage('/blobs')
    m += data
    seconds = data[1]*0.001
    if t0 is None:
        t0 = time.time() - seconds
    else:
        wait_until(t0 + seconds)
        c.sendto(m, ('127.0.0.1', 4343))
        print('sent %r' % m)

def wait_until(t):
    while time.time() < t:
        time.sleep(0.001)

def msg(t, *blobs):
    millis = int((t - start) * 1000)
    m = OSC.OSCMessage('/blobs')
    m += ['0', millis, len(blobs)]
    i = 0
    for (x, y) in blobs:
        m += [str(i), x, y, 1]
        i += 1
    return m

def send(*blobs):
    m = msg(time.time(), *blobs)
    c.sendto(m, ('127.0.0.1', 4343))
    print('sent', m)

def send_range(start, stop, step, delay):
    x = start
    while x < stop:
        send([x, 0])
        time.sleep(delay)
        x += step

if __name__ == '__main__':
    args = sys.argv[1:]
    if args:
        for line in open(args[0]):
            play(json.loads(line))
    else:
        send_range(-500, 1500, 6, 0.05);
