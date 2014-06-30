import struct
import socket
import io
import cv
import cv2

import time

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# HEADER Del paquete: 
# 0 .... 2 .... 4
#   SYNC | #SEQ
#    TIMESTAMP
# ----------------
#     PAYLOAD

i = 0

CV_CAP_PROP_FRAME_WIDTH = 3
CV_CAP_PROP_FRAME_HEIGHT = 4

capture = cv2.VideoCapture(0)
encode = [int(cv2.IMWRITE_JPEG_QUALITY), 70]

capture.set(CV_CAP_PROP_FRAME_WIDTH, 640)
capture.set(CV_CAP_PROP_FRAME_HEIGHT, 480)


while True:
	i += 1
	sync = 0

	if i == 65535:
		sync = 1
		i = 0

	ret, frame = capture.read()
	result, image = cv2.imencode('.jpg', frame, encode)

	sender = io.BytesIO(image)
	
	while True:	
		chunk = sender.read(1024 - 8)

		if chunk == "" or not chunk:
			break

		header = struct.pack("!HHI", 0, i, time.time())

		sock.sendto(header + chunk, ("localhost", 5555))




