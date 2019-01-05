#!/usr/bin/python
# variables 
import sys

def slstudioliveon_py2():
	
	
	send_osc_py2()

def slstudioliveon_py3():
	
	client = udp_client.SimpleUDPClient("0.0.0.0", osc_port=3030, osc_address="/lx/output/enabled" )

	# def play_track(track_index):
	#     client.send_message("/vezer/triggercompatindex", track_index)

	# if len(sys.argv) < 2:
	#     print("usage: {} [composition index]".format(sys.argv[0]))
	# else:
	#     play_track(comp_index)

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = "1"
	oscmsg.setAddress(osc_address)
	oscmsg.append(1)
	c.send(oscmsg)

def send_osc_py2(ip_address="0.0.0.0", osc_port=3030, osc_address="/lx/output/enabled"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = "1"
	oscmsg.setAddress(osc_address)
	oscmsg.append(1)
	c.send(oscmsg)

# comp_index = int(sys.argv[1])
VEZER_OSC_INPUT_PORT=3030

if sys.version_info[0] < 3:
    print("python 2 script, slstudio live on")
    import OSC
    slstudioliveon_py2()
else:
	print("python 3 script, slstudio live on")
	from pythonosc import udp_client
	slstudioliveon_py3()

