/*
 ============================================================================
 Name        : ppm.c
 Author      : kdesnos
 Version     : 1.0
 Copyright   : CECILL-C, IETR, INSA Rennes
 Description : Actor code to read/write a ppm file from the hard drive
 ============================================================================
 */

#include "ppm.h"

#include <xdc/runtime/System.h>
#include <xdc/runtime/Timestamp.h>
#include <xdc/runtime/Types.h>

#include <string.h>

#include "im0.h"
#include "im1.h"
#define NB_ITERATION_MEASURED 1

/*========================================================================

 Global Variable

 ======================================================================*/

void writePPM(int height, int width, unsigned char *gray) {

}

void readPPMInit(int id, int height, int width) {

}

void readPPM(int id, int height, int width, unsigned char *r, unsigned char *g,
		unsigned char *b) {
	const unsigned char* im_data = (id == 0) ? im0_data : im1_data;
	int j;
	static int i = 0;
	static unsigned int time = 0;
	unsigned int now;

	if (i == 0) {
		// 32bits precision is not sufficient here. Needs 64bits instead.
		now = Timestamp_get32();
		unsigned int delta = (now - time) / NB_ITERATION_MEASURED;
		float fps = 1000000000.0 / (float) delta;
		System_printf("fps: %f\n", fps);
		time = Timestamp_get32();
	}
	i = (i + 1) % NB_ITERATION_MEASURED;

	for (j = 0; j < height * width; j++) {
		r[j] = im_data[3 * j + 0];
		g[j] = im_data[3 * j + 1];
		b[j] = im_data[3 * j + 2];
	}
}
