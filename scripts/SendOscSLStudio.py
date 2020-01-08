#!/usr/bin/python

# This script is called by the UpstreamManager
# everytime an upstream inventory event is sent.
#
# The arguments passed are...
# $1 = EPC (a bunch of hex characters)
# $2 = Event (none, arrival, moved, departed, returned, cycle_count)
# $3 = Location (RSP-xxxxxx, where xxxxxx are the last 3 octets of the RSP MAC address)

import sys

# Check for three arguements
if (len(sys.argv) != 4):
    print("")
    print("USAGE: python inventory-event-action.py <epc> <event> <location>")
    print("")
    sys.exit(1)

file = open("event-list.txt","a+")
file.write("EPC = " + sys.argv[1] + "  EVENT = " + sys.argv[2] + "  LOCATION = " + sys.argv[3] + "\n")
file.close()


def send_osc_pattern1_py3():
	
	client = udp_client.SimpleUDPClient("127.0.0.1", SL_OSC_INPUT_PORT)

	client.send_message("/pattern/1", 1)

def send_osc_pattern2_py3():
	
	client = udp_client.SimpleUDPClient("127.0.0.1", SL_OSC_INPUT_PORT)

	client.send_message("/pattern/2", 1)

def send_osc_pattern3_py3():
	
	client = udp_client.SimpleUDPClient("127.0.0.1", SL_OSC_INPUT_PORT)

	client.send_message("/pattern/3", 1)

def send_osc_pattern4_py3():
	
	client = udp_client.SimpleUDPClient("127.0.0.1", SL_OSC_INPUT_PORT)

	client.send_message("/pattern/4", 1)


SL_OSC_INPUT_PORT=8585



def send_osc_pattern_1_py2(ip_address="127.0.0.1", osc_port=8585, osc_address="/pattern/1"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = OSC.OSCMessage()
	oscmsg.setAddress(osc_address)
	c.send(oscmsg)

def send_osc_pattern_2_py2(ip_address="127.0.0.1", osc_port=8585, osc_address="/pattern/2"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = OSC.OSCMessage()
	oscmsg.setAddress(osc_address)
	c.send(oscmsg)

def send_osc_pattern_3_py2(ip_address="127.0.0.1", osc_port=8585, osc_address="/pattern/3"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = OSC.OSCMessage()
	oscmsg.setAddress(osc_address)
	c.send(oscmsg)

def send_osc_pattern_4_py2(ip_address="127.0.0.1", osc_port=8585, osc_address="/pattern/4"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = OSC.OSCMessage()
	oscmsg.setAddress(osc_address)
	c.send(oscmsg)

	

# comp_index = int(sys.argv[1])
SL_OSC_INPUT_PORT=8585



if sys.version_info[0] < 3:
	import OSC
	print("python2 osc initalized")
	send_osc_pattern_1_py2()
	send_osc_pattern_2_py2()
	send_osc_pattern_3_py2()
	send_osc_pattern_4_py2()


else:
	print("python 3 osc initalized")
	from pythonosc import udp_client
	send_osc_pattern1_py3()
	send_osc_pattern2_py3()
	send_osc_pattern3_py3()
	send_osc_pattern4_py3()

sys.exit(0)