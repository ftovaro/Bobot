#include "Esplora.h"

JoyState_t joySt;

void thread();

int main(void) {
	init();

#if defined(USBCON)
	/*
	 * Pre-configuracion del control 
	 */
	Esplora.writeRGB(255, 0, 0);

	while(Esplora.readJoystickSwitch());

	/*
	 * Conectar el control al puerto USB y 
	 * cambiar su estado interno
	 */

	 USBDevice.attach();
	 Esplora.writeRGB(0, 128, 0);

	 joySt.xAxis = 0;
	joySt.yAxis = 0;
	joySt.zAxis = 0;
	joySt.xRotAxis = 0;
	joySt.yRotAxis = 0;
	joySt.zRotAxis = 0;
	joySt.throttle = 0;
	joySt.rudder = 0;
	joySt.hatSw1 = 0;
	joySt.hatSw2 = 0;
	joySt.buttons = 0;

#endif

	/*
	 * Ciclo de ejecucion principal
	 */
	for (;;) {
		thread();

		if (serialEventRun) serialEventRun();
	}
	return 0;
}

void thread() {
	joySt.xAxis = Esplora.readJoystickX();
	joySt.yAxis = Esplora.readJoystickY();
	joySt.zAxis = 0;
	joySt.xRotAxis = 0;
	joySt.yRotAxis = 0;
	joySt.zRotAxis = 0;
	//joySt.throttle = random(255);
	joySt.rudder = 0;

	joySt.throttle = Esplora.readSlider() / 4;


	joySt.buttons = 0b11110000;


	// Call Joystick.move
	Joystick.setState(&joySt);
}