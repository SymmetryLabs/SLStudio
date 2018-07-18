import serial
import json
import random
from pprint import pprint

device = "/dev/tty.usbmodem14121"
ser = serial.Serial(device, 1152000)

id_to_uuid = dict()

ser.write('{"start":0}')


def traverse(node):
	id_to_uuid[node["i"]] = node["u"]
	for child in node["c"]:
		if child is not None:
			traverse(child)


while True:
	l = ser.readline()
	# print l
	j = json.loads(l)
	print j
	if "in" in j:
		k = j["in"][0]["i"]
		i = id_to_uuid[k]
		print {"uuid": i, "id": k}
	else:
		old = frozenset(id_to_uuid.keys())
		id_to_uuid = dict()
		traverse(j["l"])
		new = frozenset(id_to_uuid.keys())
		for k in (new - old):
			print {"uuid": id_to_uuid[k]}

	ser.write(json.dumps(
		 {"midimap":[{"i":3, "s":[{"c":3, "mc":5, "t":2, "d":55, "m": 1}]}]}
	) + "\n")


# for i in range(1, 5):
# 	led_str = '{"led":[{"i":%d, "m":1, "r":255, "b":255, "g":0}]}\n' % i
# 	ser.write(led_str)

# while True:
# 	l = ser.readline()
# 	pprint(json.loads(l))