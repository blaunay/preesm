/*
 ============================================================================
 Name        : aggregateCost.c
 Author      : kdesnos
 Author      : JZHAHG
 Version     : 1.0
 Copyright   : CeCILL-C, IETR, INSA Rennes
 Description : Aggregate the horizontal and vertical disparity error for
 several offsets.
 ============================================================================
 */

#include "utils.h"
#include "aggregateCost.h"
#include <string.h>

#define min(x,y) (((x)<(y))?(x):(y))
#define max(x,y) (((x)<(y))?(y):(x))

void aggregateCost(int height, int width, int nbIterations,
		float *disparityError, int *offsets, float *hWeights, float *vWeights,
		float *aggregatedDisparity) {
	int offsetIdx;
	int i, j;

	// For each of the offset, do the horizontal and vertical
	// aggregation
	for (offsetIdx = 0; offsetIdx < 2 * nbIterations; offsetIdx++) {
		int offset = LOAD_INT(&offsets[offsetIdx/2]);

		// Even iterations are vertical, Odd are horizontal
		int hOffset = (offsetIdx % 2 == 1) ? offset : 0;
		int vOffset = (offsetIdx % 2 == 0) ? offset : 0;

		// Select the weights corresponding to the current offset
		float *weights = (offsetIdx % 2 == 0) ? vWeights : hWeights;
		int weightIdx = (offsetIdx / 2) * (3 * height * width);

		// Select the computation destination and source
		// Even iteration from disparityError and Odd from aggregated Disparity
		float *src =
				(offsetIdx % 2 == 0) ? disparityError : aggregatedDisparity;
		float *dest =
				(offsetIdx % 2 == 0) ? aggregatedDisparity : disparityError;

		// Scan the image pixels
		for (j = 0; j < height; j++) {
			for (i = 0; i < width; i++) {
				float costM, costP, costO;
				float weightM, weightP, weightO;
				float result;

				// Get the costs of the pixels
				costO = LOAD_FLOAT(&src[j*width+i]);
				costM =
						LOAD_FLOAT(&src[max(j-vOffset,0)*width+max(i-hOffset,0)]);
				costP =
						LOAD_FLOAT(&src[min(j+vOffset,height-1)*width+min(i+hOffset,width-1)]);

				// Get the weights
				weightO = LOAD_FLOAT(&weights[weightIdx + 3*(j*width+i)]);
				weightM = LOAD_FLOAT(&weights[weightIdx + 3*(j*width+i)+1]);
				weightP = LOAD_FLOAT(&weights[weightIdx + 3*(j*width+i)+2]);
				result = weightO * costO + weightM * costM + weightP * costP;
				STORE_FLOAT(&dest[j*width+i], &result);
			}
		}
	}

	// Copy the result in the output buffer.
	memcpy(aggregatedDisparity, disparityError, height * width * sizeof(float));
}
