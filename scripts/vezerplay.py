# variables 
import sys

def vezerplay_py2():
	
	
	send_osc_py2()

def vezerplay_py3():
	
	client = udp_client.SimpleUDPClient("127.0.0.1", VEZER_OSC_INPUT_PORT)

	def play_track(track_index):
	    client.send_message("/vezer/triggercompatindex", track_index)

	if len(sys.argv) < 2:
	    print("usage: {} [composition index]".format(sys.argv[0]))
	else:
	    play_track(comp_index)

def send_osc_py2(ip_address="127.0.0.1", osc_port=4040, osc_address="/vezer/triggercompatindex"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = OSC.OSCMessage()
	oscmsg.setAddress(osc_address)
	oscmsg.append(comp_index)
	c.send(oscmsg)

comp_index = int(sys.argv[1])
VEZER_OSC_INPUT_PORT=4040

if sys.version_info[0] < 3:
    print("python 2 script, playing vezer composition", comp_index)
    import OSC
    vezerplay_py2()
else:
	print("python 3 script, playing vezer composition", comp_index)
	from pythonosc import udp_client
	vezerplay_py3()

