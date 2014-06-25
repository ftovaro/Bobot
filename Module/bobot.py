#!/usr/bin/python
# -*- coding: utf-8 -*-
import io
import cv
import cv2
import sys
import time
import getopt
import socket
import struct
from threading import Thread

HEADER = '\033[95m'
OK = '\033[94m'
WARNING = '\033[93m'
FAIL = '\033[91m'
ENDC = '\033[0m'

VERSION = "1.0.0 Alpha"

IP = "127.0.0.1"
PORT = 9999

CAMERA = None
CAMERA_ENABLE = False
CAMERA_PORT = 9998
CAMERA_WIDTH = 640
CAMERA_HEIGHT = 480
CAMERA_QUALITY = 75
CAMERA_FPS = 10
CAMERA_MTU= 7981

channel = None

def error(msn, fatal = False):
	print FAIL + '[ERROR] ' + ENDC + msn

	if fatal:
		sys.exit(2)

def warning(msn):
	print WARNING + '[WARNING] ' + ENDC + msn

def ok(msn):
	print OK + '[ OK ] ' + ENDC + msn

def info(msn, newline = True, header = True):
	head = ""

	if header:
		head = HEADER + '[INFO] ' + ENDC

	if newline:
		print head + msn
	else:
		print(head + msn),

def usage():
	print "Bobot Module."
	print " Este es el modulo de comandos del robot."
	print " 2014 EAFIT"
	print ""
	print "Modo de uso:"

	print " -i --ip <ip> : IP a la cual se va a conectar [MAIN]"
	print " -p --port <port> : Puerto de comunicacion [MAIN]"
	print " * este puerto es distinto al del streaming, esta"
	print "conexion [MAIN] se realiza via TCP mientras el streaming"
	print "via UDP"

	print ""

	# USO DE CONFIG DE LA CAMARA
	print " -c --camera <port> : Activa el modulo de la camara, y hace stream via <port>"
	print " --camera-width <width> : Ancho del frame"
	print " --camera-height <height> : Alto del frame"
	print " --camera-quality <quality> : Calidad de compresion [1, 100]"
	print " --camera-fps <fps> : Frames que se envian cada segundo"
	print " --camera-mtu <mtu> : Maximo tamaño de fraccion UDP"

def loopmain():
	global IP
	global PORT

	global channel

	channel = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	try:
		channel.connect((IP, PORT))
	except Exception:
		error(("No esta abierta la direccion %s:%s") % (IP, PORT), fatal=True)

	while True:
		try: pass
			

			# sys.stdout.write("Se entrego la trama UDP: %s bytes   \r" % (sys.getsizeof(sender)))
			# sys.stdout.flush()
			# time.sleep(0)
		except Exception:
			channel.close()
			error("Ocurrio un error fatal en la comunicacion", fatal = False)
		except KeyboardInterrupt:
			channel.close()
			break

def loopcam():
	CV_CAP_PROP_FRAME_WIDTH = 3
	CV_CAP_PROP_FRAME_HEIGHT = 4

	global CAMERA_MTU
	global CAMERA_WIDTH
	global CAMERA_HEIGHT
	global CAMERA_QUALITY
	global CAMERA_MTU
	global CAMERA_FPS

	global IP
	global CAMERA_PORT

	info("Configuracion de la camara:")
	info((" -> Width: %s" % CAMERA_WIDTH), header = False)
	info((" -> Height: %s" % CAMERA_HEIGHT), header = False)
	info((" -> Quality: %s" % CAMERA_QUALITY), header = False)
	info((" -> MTU: %s" % CAMERA_MTU), header = False)
	info((" -> FPS: %s" % CAMERA_FPS), header = False)


	capture = cv2.VideoCapture(0)
	encode = [int(cv2.IMWRITE_JPEG_QUALITY), CAMERA_QUALITY]

	capture.set(CV_CAP_PROP_FRAME_WIDTH, CAMERA_WIDTH)
	capture.set(CV_CAP_PROP_FRAME_HEIGHT, CAMERA_HEIGHT)

	channel = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

	ok("La camara esta lista para el Streaming")

	i = 0

	while True:
		i += 1
		sync = 0

		if i == 65535:
			sync = 1
			i = 0

		try:
			ret, frame = capture.read()
			result, image = cv2.imencode('.jpg', frame, encode)

			sender = io.BytesIO(image)

			while True:
				payload = sender.read(CAMERA_MTU - 8)

				if payload == "" or not payload:
					break

				header = struct.pack("!HHI", sync, i, time.time())

				channel.sendto(header + payload, (IP, CAMERA_PORT))	

			time.sleep(1 / CAMERA_FPS)
		except KeyboardInterrupt:
			print ""

			channel.close()
			info("Flujo de datos detenido")
			ok("Streaming finalizado")

			break
		except Exception:
			error("Ocurrio un error en el Streaming [-1]")

def init():
	global CAMERA_ENABLE
	global IP, PORT

	info("Iniciando conexion con %s:%s" % (IP, PORT))

	if CAMERA_ENABLE:
		main = Thread(target=loopmain)
		main.daemon = True
		main.start()

		info("Inciando Streaming via puerto %s" % CAMERA_PORT)
		ok("La camara se configuro correctamente")
		loopcam()

	else:
		loopmain()

	ok("Conexion finalizada")

def main(argv):
	try:
		opts, args = getopt.getopt(argv, "i:c:p:", 
			["camera=", "camera-width=", "camera-height=", "camera-quality=", "camera-fps=", 
			"camera-mtu=", "ip=", "port=", "help", "version"])
	except getopt.GetoptError:
		usage()
		sys.exit(2)

	for opt, arg in opts:
		if opt in ("-i", "--ip"):
			global IP

			IP = arg

		elif opt in ("-p", "--port"):
			global PORT

			try:
				port = int(arg.strip())

				if port > 1024 and port < 65536:
					PORT = port
				else:
					warning(("MAIN: No es posible usar " + arg.strip() + ", se usara por defecto %s" % PORT))
			except ValueError:
				error("Puerto desconocido", fatal=True)
		elif opt == "--help":
			usage()
			sys.exit(0)

		elif opt in ("-c", "--camera"):
			global CAMERA_PORT
			global CAMERA_ENABLE

			try:
				port = int(arg.strip())

				CAMERA_ENABLE = True

				if port > 1024 and port < 65536:
					CAMERA_PORT = port
				else:
					warning(("No es posible usar " + arg.strip() + ", se usara por defecto %s" % CAMERA_PORT))
			except ValueError:
				error("Puerto desconocido", fatal=True)


		elif opt == "--camera-width":
			global CAMERA_WIDTH

			try:
				CAMERA_WIDTH = int(arg.strip())
			except ValueError:
				warning("No fue posible configurar el ancho del frame, se usara por defecto " + CAMERA_WIDTH)

		elif opt == "--camera-height":
			global CAMERA_HEIGHT

			try:
				CAMERA_HEIGHT = int(arg.strip())
			except ValueError:
				warning("No fue posible configurar el alto del frame, se usara por defecto " + CAMERA_HEIGHT)

		elif opt == "--camera-quality":
			global CAMERA_QUALITY

			try:
				quality = int(arg.strip())

				if quality > 0 and quality < 101:
					CAMERA_QUALITY = quality
				else:
					warning("Calidad de frame fuera de rango [1, 100]")
			except ValueError:
				error("No es posible interpretar " + arg + " como calidad de frame")

		elif opt == "--camera-fps":
			global CAMERA_FPS

			try:
				fps = int(arg.strip())

				CAMERA_FPS = fps
			except:
				warning("FPS invalido, se usara por defecto %s fps" % CAMERA_FPS)

		elif opt == "--camera-mtu":
			global CAMERA_MTU

			try:
				mtu = int(arg.strip())

				CAMERA_MTU = mtu
			except:
				warning("MTU invalido, se usara por defecto %s" % CAMERA_MTU)

		elif opt == "--version":
			print VERSION
			sys.exit(0)


	init()


if __name__ == "__main__":
	print "Bienvenido al modulo de comandos."
	print "  Semillero de Sistemas Embebidos SISE"
	print "    * Sergio Monsalve"
	print "    * Sebastian Garces"
	print "    * Felipe Tovar"
	print "    * Alejandro Salgado"
	print "    * Mateo Olaya Bernal"
	print "  2014 EAFIT - Medellin Colombia"
	print ""

	main(sys.argv[1:])