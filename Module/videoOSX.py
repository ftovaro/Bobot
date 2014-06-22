#!/usr/bin/python 

import io
import cv
import cv2
import sys
import time
import socket
import struct

CV_CAP_PROP_FRAME_WIDTH = 3
CV_CAP_PROP_FRAME_HEIGHT = 4

# Se crea un flujo UDP para el envio de imagenes
comm = socket.socket()
comm.connect(("localhost", 9998))

capture = cv2.VideoCapture(0)
encode = [int(cv2.IMWRITE_JPEG_QUALITY), 50]

capture.set(CV_CAP_PROP_FRAME_WIDTH, 640)
capture.set(CV_CAP_PROP_FRAME_HEIGHT, 480)

while True:
	try:
		ret, frame = capture.read()
		result, image = cv2.imencode('.jpg', frame, encode)

		comm.send(image.tostring())
	except ValueError:
		comm.close()

comm.close()

 