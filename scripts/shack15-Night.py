# variables 
import sys
import time

def enabled1_up_py2():
	send_osc_py2(osc_address="/lx/output/brightness")

def enableds_up_py3():
	

	client = udp_client.SimpleUDPClient("0.0.0.0", SL_OUTPUT_PORT)

	def enabled_1_up():
		
		i = 1
		client.send_message("/lx/channel/1/enabled/", i)
			
			

	def enabled_2_up():
		
		i = 1
		client.send_message("/lx/channel/2/enabled/", i)
			
			

	def enabled_3_up():
		
		i = 1
		client.send_message("/lx/channel/3/enabled/", i)
			
			

	def enabled_4_up():
		
		i = 1
		client.send_message("/lx/channel/4/enabled/", i)
			
			

	def enabled_5_up():
		
		i = 1
		client.send_message("/lx/channel/5/enabled/", i)
			
			

	
	def enabled_1_down():
		
		i = 0
		client.send_message("/lx/channel/1/enabled/", i)
			
			


	def enabled_2_down():
		
		i = 0
		client.send_message("/lx/channel/2/enabled/", i)
			
			

	def enabled_3_down():
		
		i = 0
		client.send_message("/lx/channel/3/enabled/", i)
			
			
	
	def enabled_4_down():
		
		i = 0
		client.send_message("/lx/channel/4/enabled/", i)
			
			

	def enabled_5_down():
		
		i = 0
		client.send_message("/lx/channel/5/enabled/", i)
			


	def green_1_on():
		
		client.send_message("/lx/channel/1/effect/1/enabled", 1)	

	def green_1_off():
		
		client.send_message("/lx/channel/1/effect/1/enabled", 0)

	def green_2_on():
		
		client.send_message("/lx/channel/2/effect/1/enabled", 1)	

	def green_2_off():
		
		client.send_message("/lx/channel/2/effect/1/enabled", 0)

	def green_3_on():
		
		client.send_message("/lx/channel/3/effect/1/enabled", 1)	

	def green_3_off():
		
		client.send_message("/lx/channel/3/effect/1/enabled", 0)

	def green_4_on():
		
		client.send_message("/lx/channel/4/effect/1/enabled", 1)	

	def green_4_off():
		
		client.send_message("/lx/channel/4/effect/1/enabled", 0)

	def green_5_on():
		
		client.send_message("/lx/channel/5/effect/1/enabled", 1)	

	def green_5_off():
		
		client.send_message("/lx/channel/5/effect/1/enabled", 0)



	def blue_1_on():
		
		client.send_message("/lx/channel/1/effect/2/enabled", 1)	

	def blue_1_off():
		
		client.send_message("/lx/channel/1/effect/2/enabled", 0)

	def blue_2_on():
		
		client.send_message("/lx/channel/2/effect/2/enabled", 1)	

	def blue_2_off():
		
		client.send_message("/lx/channel/2/effect/2/enabled", 0)

	def blue_3_on():
		
		client.send_message("/lx/channel/3/effect/2/enabled", 1)	

	def blue_3_off():
		
		client.send_message("/lx/channel/3/effect/2/enabled", 0)

	def blue_4_on():
		
		client.send_message("/lx/channel/4/effect/2/enabled", 1)	

	def blue_4_off():
		
		client.send_message("/lx/channel/4/effect/2/enabled", 0)

	def blue_5_on():
		
		client.send_message("/lx/channel/5/effect/2/enabled", 1)	

	def blue_5_off():
		
		client.send_message("/lx/channel/5/effect/2/enabled", 0)




	def peach_1_on():
		
		client.send_message("/lx/channel/1/effect/3/enabled", 1)	

	def peach_1_off():
		
		client.send_message("/lx/channel/1/effect/3/enabled", 0)

	def peach_2_on():
		
		client.send_message("/lx/channel/2/effect/3/enabled", 1)	

	def peach_2_off():
		
		client.send_message("/lx/channel/2/effect/3/enabled", 0)

	def peach_3_on():
		
		client.send_message("/lx/channel/3/effect/3/enabled", 1)	

	def peach_3_off():
		
		client.send_message("/lx/channel/3/effect/3/enabled", 0)

	def peach_4_on():
		
		client.send_message("/lx/channel/4/effect/3/enabled", 1)	

	def peach_4_off():
		
		client.send_message("/lx/channel/4/effect/3/enabled", 0)

	def peach_5_on():
		
		client.send_message("/lx/channel/5/effect/3/enabled", 1)	

	def peach_5_off():
		
		client.send_message("/lx/channel/5/effect/3/enabled", 0)



	def pink_1_on():
		
		client.send_message("/lx/channel/1/effect/4/enabled", 1)	

	def pink_1_off():
		
		client.send_message("/lx/channel/1/effect/4/enabled", 0)

	def pink_2_on():
		
		client.send_message("/lx/channel/2/effect/4/enabled", 1)	

	def pink_2_off():
		
		client.send_message("/lx/channel/2/effect/4/enabled", 0)

	def pink_3_on():
		
		client.send_message("/lx/channel/3/effect/4/enabled", 1)	

	def pink_3_off():
		
		client.send_message("/lx/channel/3/effect/4/enabled", 0)

	def pink_4_on():
		
		client.send_message("/lx/channel/4/effect/4/enabled", 1)	

	def pink_4_off():
		
		client.send_message("/lx/channel/4/effect/4/enabled", 0)

	def pink_5_on():
		
		client.send_message("/lx/channel/5/effect/4/enabled", 1)	

	def pink_5_off():
		
		client.send_message("/lx/channel/5/effect/4/enabled", 0)

		

	# enabled ons
	enabled_1_up()
	# enabled_2_up()
	# enabled_3_up()
	# enabled_4_up()
	enabled_5_up()


	# enable offs
	# enabled_1_down()
	enabled_2_down()
	enabled_3_down()
	enabled_4_down()
	# enabled_5_down()

	#color enabled ons
	pink_1_on()
	# pink_2_on()
	# pink_3_on()
	# pink_4_on()
	pink_5_on()

	# green_1_on()
	# green_2_on()
	# green_3_on()
	# green_4_on()
	# green_5_on()

	# blue_1_on()
	# blue_2_on()
	# blue_3_on()
	# blue_4_on()
	# blue_5_on()

	# peach_1_on()
	# peach_2_on()
	# peach_3_on()
	# peach_4_on()
	# peach_5_on()

	# colored enables offs
	# pink_1_off()
	pink_2_off()
	pink_3_off()
	pink_4_off()
	# pink_5_off()

	green_1_off()
	green_2_off()
	green_3_off()
	green_4_off()
	green_5_off()

	blue_1_off()
	blue_2_off()
	blue_3_off()
	blue_4_off()
	blue_5_off()

	peach_1_off()
	peach_2_off()
	peach_3_off()
	peach_4_off()
	peach_5_off()





# def send_osc_py2(ip_address="0.0.0.0", osc_port=3030, osc_address="/lx/output/brightness"):

# 	
# 	while i < 1
# 		c = OSC.OSCClient()
# 		c.connect((ip_address, osc_port))
# 		oscmsg = OSC.OSCMessage()
# 		oscmsg.setAddress(osc_address)
# 		oscmsg.append(i)
# 		c.send(oscmsg)
# 		
# 		

#comp_index = int(sys.argv[1])
SL_OUTPUT_PORT=3030

if sys.version_info[0] < 3:
    print("python 2 script, Master On")
    import OSC
    enabled1_up_py2()
else:
	print("python 3 script, Master On")
	from pythonosc import udp_client
	enableds_up_py3()


