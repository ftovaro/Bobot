#!/usr/bin/python 

import io
import cv
import cv2
import sys
import time
import socket
import struct

from threading import Thread
from abc import ABCMeta, abstractmethod

OPENCV = 0
PICAMERA = 1

class Channel:
	

class Module:
	__channel = None

	def connect

	@abstractmethod
	def send(self): pass

	@


class Bobot:
	_instance = None
	_controller = None
	_camera = None

	@staticmethod
	def instance():
		if not Bobot._instance:
			Bobot._instance = Bobot()

		return Bobot._instance

	def connect(self, address, port = 9999):




CV_CAP_PROP_FRAME_WIDTH = 3
CV_CAP_PROP_FRAME_HEIGHT = 4

# Fracciones de 7981 bytes :: MTU de WLAN
CAMERA_FRAME_CHUNK_SIZE = 7981 # Se fracciona el envio VEASE MTU

# Se crea un flujo UDP para el envio de imagenes
comm = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
# comm.connect(("localhost", 9998))

capture = cv2.VideoCapture(0)
encode = [int(cv2.IMWRITE_JPEG_QUALITY), 100]

capture.set(CV_CAP_PROP_FRAME_WIDTH, 640)
capture.set(CV_CAP_PROP_FRAME_HEIGHT, 480)

while True:
	try:
		ts = int(time.time())

		ret, frame = capture.read()
		result, image = cv2.imencode('.jpg', frame, encode)

		# comm.send("%s" % ts)

		sender = io.BytesIO(image)

		while True:
			chunk = sender.read(CAMERA_FRAME_CHUNK_SIZE)

			if chunk == "" or not chunk:
				break

			comm.sendto(chunk, ("localhost", 9998))	

		
		time.sleep((1 / 5))
		
	except ValueError: pass
		#comm.close()

# comm.close()

 