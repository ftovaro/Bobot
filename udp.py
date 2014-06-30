import struct
import socket
import io
import time
import sys
import json
import picamera
import wiringpi2 as gpio

from threading import Thread

IP = "192.168.1.112"
PORT = 4545

CAMERA = True
CAMERA_PORT = 5555
CAMERA_MTU = 7981

isOpen = False;

print "Configurando GPIO"
gpio.wiringPiSetupGpio()

# Establecer uso de pines
gpio.pinMode(17, 1)
gpio.pinMode(18, 1)

gpio.softPwmCreate(17,0,100)
gpio.softPwmCreate(18,0,100)
print "GPIO OK"

def move(turn, throttle):
	l_power = throttle
	r_power = throttle

	if (turn < 0):
		l_power = throttle + turn
		r_power = throttle
	elif turn > 0:
		l_power = throttle
		r_power = throttle + (turn * -1)

	gpio.softPwmWrite(17, l_power)
	gpio.softPwmWrite(18, r_power)

def decode(dic):
	if dic['controller']:
		move(int(dic['controller']['turn']), 
			int(dic['controller']['throttle']))


def main():
	global IP
	global PORT

	global isOpen

	channel = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	try:
		channel.connect(("192.168.1.112", 4545))
	except socket.error, (value,message):
		print "ERROR => No esta abierta la direccion %s:%s" % (IP, PORT)
		print " TRACE :: %s" % message

		if channel:
			channel.close()

		sys.exit(2)

	while True:
		if not isOpen:
			break

		try:
			chunk = str(channel.recv(7981)).strip()

			if chunk != "":

				data = json.loads(chunk)

				decode(data)

				channel.send(json.dumps({"status":[1, "OK"]}))

				print "[%s] %s" % (int(time.time()), chunk)
		except Exception, e:
			channel.send(json.dumps({
				"status":[2, "Error en el request", str(e)]}))

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






