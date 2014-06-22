# -*- coding: utf-8 -*-
#!/usr/bin/python

GPIO = False

from abc import ABCMeta, abstractmethod
from threading import Thread
import time
import socket

if GPIO:
	import wiringpi2 as gpio

class Controller:
	@abstractmethod
	def connect(self): pass

	@abstractmethod
	def send(self, data = None): pass

class WiFi(Controller):
	_connection = None 

	def __init__(self):
		self._connection = socket(socket.AF_INET, socket.SOCK_STREAM)

	def connect(self):
		
		return False;

	def send(self, data = None):
		if data:
			self._socket.write()


class Engine:
	_left_engine = 0
	_rigth_engine = 0

	__throtle = 0.0
	__gyro = 0.0

	def __init__(self, left, rigth):
		self._left_engine = left
		self._rigth_engine= rigth

		if GPIO:
			gpio.wiringPiSetupGpio()

			gpio.pinMode(self._left_engine,  1)
			gpio.pinMode(self._rigth_engine, 1)

			gpio.softPwmCreate(self._left_engine,0,100)
			gpio.softPwmCreate(self._rigth_engine,0,100)

# Este metodo define la acceleration de los motores
# x2. Los valores recibidos por este método son valores
# flotantes entre [0, 1.0], cualquier valor fuera de este intervalo
# sera considerado el máximo correspondiente del intervalo.
#
# La aceleración esta dada por el porcentaje de energía entregada
# a los dos motores 0% - 100% -> 0.0 - 1.0 
	def throttle(self, throttle):
		if throttle > 1.0:
			throttle = 1.0
		if throttle < 0:
			throttle = 0

		self.__throtle = throttle

		self.motion()

# Se define el giro haciendo uso de la intensidad 
# de los motores. Los valores recibidos por este
# método son valores flotantes entre [-1.0, 1.0]
# cualquier valor por encima o debajo de este 
# intervalo sera considerado el máximo correspondiente
# del intervalo.
# Formas de giro: 
#   1). Si x < 0 —> Giro a la Izquierda (Software)
#   2). Si x > 0 —> Giro a la Derecha (Software)
#   3). Si x == 0 —> Se mantiene recto.
#
# El giro hace uso de PWM así que su intensidad esta
# dada por el porcentaje: -1.0 máxima intensidad de giro 
# a la izquierda, -0.5 media intensidad de giro, etc.
#
# Es giro por Software es como esta descrito, cualquier
# variación pude ser de Hardware.
	def turn(self, gyro):
		if gyro > 1.0:
			gyro = 1.0
		if gyro < -1.0:
			gyro = -1.0
		
		self.__gyro = gyro

		self.motion()

	def motion(self):
		lPower = self.__throtle
		rPower = self.__throtle

		if self.__gyro < 0:
			# El giro es hacia la izquierda
			lPower = max(0, lPower - abs(self.__gyro))

		elif self.__gyro > 0:
			# El giro es hacia la derecha
			rPower = max(0, rPower - abs(self.__gyro))

		# ESCRIBIR LPOWER && RPOWER EN PWM WIRINGPI2
		if GPIO:
			gpio.softPwmWrite(self._left_engine, int(lPower * 100))
			gpio.softPwmWrite(self._rigth_engine, int(rPower * 100))

class Bobot(Engine):
	_instance = None

	@staticmethod
	def instance():
		if not Bobot._instance:
			Bobot._instance = Bobot(1, 2)

		return Bobot._instance

	def off(self):
		self.throttle(0.0);
		self.turn(0.0);

	def forward(self, speed = 0.5, times = 1):
		i = times

		while i > 0:
			self.turn(0.0)
			self.throttle(speed)
			i -= 1

	def turn(self, angle = 0):
		# girar dado un angulo
		pass

	def turnLeft(self, speed = 1.0, times = 1):
		i = times

		while i > 0:
			self.turn(speed * -1.0)
			i -= 1

	def turnRigth(self, speed = 1.0, times = 1):
		i = times

		while i > 0:
			self.turn(speed)
			i -= 1



class Component:
	__metaclass__ = ABCMeta

	__name = None
	__pin = None

	@abstractmethod
	def value(self): pass

	@abstractmethod
	def write(self, data): pass


#####################################################
######         PRUEBA DE FUNCIONAMIENTO       #######
#####################################################

robot = Bobot.instance()
robot.forward()
robot.turnLeft()
robot.forward(times = 25)

