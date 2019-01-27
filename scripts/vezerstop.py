# variables 
import sys

def vezerstop_py2():
	send_osc_py2(osc_address="/vezer/stopcomps")

def vezerstop_py3():
	
	client = udp_client.SimpleUDPClient("127.0.0.1", VEZER_OSC_INPUT_PORT)

	def stop_track():
		client.send_message("/vezer/stopcomps", "0")

	stop_track()

def send_osc_py2(ip_address="127.0.0.1", osc_port=4040, osc_address="/vezer/triggercompatindex"):

	c = OSC.OSCClient()
	c.connect((ip_address, osc_port))
	oscmsg = OSC.OSCMessage()
	oscmsg.setAddress(osc_address)
	oscmsg.append("0")
	c.send(oscmsg)

#comp_index = int(sys.argv[1])
VEZER_OSC_INPUT_PORT=4040

if sys.version_info[0] < 3:
    print("python 2 script, stopping vezer")
    import OSC
    vezerstop_py2()
else:
	print("python 3 script, stopping vezer")
	from pythonosc import udp_client
	vezerstop_py3()

