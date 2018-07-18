import serial
import json
import random
from pprint import pprint

device = "/dev/tty.usbmodem14121"
ser = serial.Serial(device, 1152000)

class Module(object):
	types = ["hub", "button", "dial", "slider"]

	def __init__(self, ser, i, uid, t):
		self.i = i
		self.uid = uid
		self.mod_type = Module.types[t]
		self.ser = ser

	def __repr__(self):
		return "(%s %d)" % (self.mod_type, self.i)

	def set_color(self, r, g, b):
		ser.write(json.dumps({
			"led": [
				{"i": self.i, "m":0, "r":int(r), "b":int(b), "g":int(g)}
			]
		}))



ser.write('{"start":0}')
start = json.loads(ser.readline())["l"]


def traverse(node):
	modules[node["i"]] = Module(ser, node["i"], node["u"], node["t"])
	for child in node["c"]:
		if child is not None:
			traverse(child)

modules = dict()
traverse(start)
pprint(modules)

while True:
	for m in modules.values():
		m.set_color(random.random() * 255, 0, random.random() * 255)


# for i in range(1, 5):
# 	led_str = '{"led":[{"i":%d, "m":1, "r":255, "b":255, "g":0}]}\n' % i
# 	ser.write(led_str)

# while True:
# 	l = ser.readline()
# 	pprint(json.loads(l))