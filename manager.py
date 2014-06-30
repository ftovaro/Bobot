#!/usr/bin/python
import struct
import socket
import io
import time
import sys
import json
import picamera
import wiringpi2 as gpio
import os
import subprocess

from threading import Thread

IP = "192.168.1.112"
PORT = 4545

CAMERA = True
CAMERA_PORT = 5555
CAMERA_MTU = 7981

isOpen = False
channelTCP = None

print "Configurando GPIO"
gpio.wiringPiSetupGpio()

# Establecer uso de pines
gpio.pinMode(17, 1) # LPOWER PWM
gpio.pinMode(18, 1) # RPOWER PWM

gpio.pinMode(22, 1) # DIR
gpio.pinMode(23, 1) # DIR

gpio.softPwmCreate(17,0,100)
gpio.softPwmCreate(18,0,100)
print "GPIO OK"

def monitor():
	global channelTCP

	while True:
		cmd6 = "iwconfig wlan0 | grep Signal | awk '{print $4}' | cut -d= -f2" 
		process = subprocess.Popen(cmd6, stdout=subprocess.PIPE , shell=True)
		os.waitpid(process.pid, 0)[1]
		wifi_strength = process.stdout.read().strip()

		if channelTCP:
			channelTCP.send(json.dumps({"signal": wifi_strength}) + "\r\n")

		time.sleep(10)
	

def move(turn, throttle):
	throttle /= 2

	l_power = throttle
	r_power = throttle

	if (throttle < 0):
		gpio.digitalWrite(22,1)
		gpio.digitalWrite(23,0)

		l_power = (throttle * -1)
		r_power = (throttle * -1)
	else:
		gpio.digitalWrite(22,0)
		gpio.digitalWrite(23,1)


	if (turn < 0):
		l_power = throttle
		r_power = throttle - (turn * -1)
	elif turn > 0:
		l_power = throttle - turn
		r_power = throttle 

	gpio.softPwmWrite(18, l_power)
	gpio.softPwmWrite(17, r_power)

def decode(dic):
	if dic['controller']:
		move(int(dic['controller']['turn']), 
			int(dic['controller']['throttle']))

def main():
	global IP
	global PORT

	global isOpen
	global channelTCP

	channelTCP = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	try:
		channelTCP.connect((IP, PORT))
	except socket.error, (value,message):
		print "ERROR => No esta abierta la direccion %s:%s" % (IP, PORT)
		print " TRACE :: %s" % message

		if channelTCP:
			channelTCP.close()

		sys.exit(2)

	monitorThread = Thread(target=monitor)
	monitorThread.daemon = True
	monitorThread.start()

	while True:
		if not isOpen:
			break

		try:
			chunk = str(channelTCP.recv(7981)).strip()

			if chunk != "":

				data = json.loads(chunk)

				decode(data)

				channelTCP.send(json.dumps({"status":[1, "OK"]}) + "\r\n")

				print "[%s] %s" % (int(time.time()), chunk)
		except Exception, e:
			move(0, 0)
			try: 
				channel.send(json.dumps({
					"status":[2, "Error en el request", str(e)]}))
			except: pass

print 'Iniciando los procesos necesarios'

main = Thread(target=main)
main.daemon = False
main.start()

isOpen = True

print 'Configurado correctamente'

with picamera.PiCamera() as cam:
	sequence = 0;

	cam.resolution = (640, 480)
	cam.framerate = 15

	channel = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

	print 'Espera... Configurando la camara.'
	time.sleep(2)
	print 'Camara ON'

	sender = io.BytesIO()
	isEnable = True

	try: 
		for frame in cam.capture_continuous(sender, 
			'jpeg', use_video_port = True, quality = 5):
			
			if not isEnable:
				break

			sender.seek(0)

			while True:
				payload = sender.read(CAMERA_MTU - 8)

				if (payload == "" or not payload):
					break

				sync = 0
				sequence += 1

				if sequence >= 65535:
					sequence = 0
					sync = 1

				header = struct.pack("!HHI", sync, sequence, time.time())
				channel.sendto(header + payload, (IP, CAMERA_PORT))	

			sender.seek(0)
			sender.truncate()
	except KeyboardInterrupt:
		sys.stdout.flush()
		sys.stdout.write("\rCamara OFF \r\n")

		isEnable = False
		isOpen = False
	except Exception:
		print 'Error al enviar el frame'






