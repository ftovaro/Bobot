#ifndef PERIPHERAL_H_
#define PERIPHERAL_H_

#include <stdint.h>
#include "Esplora.h"

#define PERIPHERAL_BUTTON_SIZE 4

struct peripheral_t {
	int axis_x;
	int axis_y;
	int throttle;

	uint8_t buttons[PERIPHERAL_BUTTON_SIZE];
};

class Peripheral {
private:
	peripheral_t __core;

public:
	Peripheral();

	void read();
	void write(peripheral_t);

	peripheral_t * getData();
};

#endif