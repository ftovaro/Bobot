#include "Peripheral.h"

Peripheral::Peripheral() : __core()
{ }

void Peripheral::read() {
	if (Esplora.readJoystickX() != __core.axis_x) {
		__core.axis_x = Esplora.readJoystickX();
	}

	if (Esplora.readJoystickY() != __core.axis_y) {
		__core.axis_y = Esplora.readJoystickY();
	}

	if (Esplora.readSlider() != __core.throttle) {
		__core.throttle = Esplora.readSlider();
	}

	for (uint8_t i = 1; i < 5; ++i) {
		__core.buttons[i - 1] = (Esplora.readButton(i) & 0xFF);
	}
}

void Peripheral::write(peripheral_t p) {
	this->__core = p;
} 

peripheral_t * Peripheral::getData() {

	return &__core;
}