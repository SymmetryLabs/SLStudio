# variables 
import sys
import time

def fader1_up_py2():
	send_osc_py2(osc_address="/lx/output/brightness")

def fader1_up_py3():
	

	client = udp_client.SimpleUDPClient("0.0.0.0", SL_OUTPUT_PORT)

	def fader_1_up():
		i = .1
		while i < 1:
			client.send_message("/lx/channel/1/fader/", i)
			i += .025
			time.sleep(.1)

	fader_1_up()

def send_osc_py2(ip_address="0.0.0.0", osc_port=3030, osc_address="/lx/output/brightness"):

	i = .1
	while i < 1
		c = OSC.OSCClient()
		c.connect((ip_address, osc_port))
		oscmsg = OSC.OSCMessage()
		oscmsg.setAddress(osc_address)
		oscmsg.append(i)
		c.send(oscmsg)
		i += .025
		time.sleep(.1)

#comp_index = int(sys.argv[1])
SL_OUTPUT_PORT=3030

if sys.version_info[0] < 3:
    print("python 2 script, Master On")
    import OSC
    fader1_up_py2()
else:
	print("python 3 script, Master On")
	from pythonosc import udp_client
	fader1_up_py3()

