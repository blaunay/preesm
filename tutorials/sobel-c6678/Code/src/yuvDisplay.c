

#include "displayYUV.h"
#include <time.h>
#include <xdc/runtime/System.h>


/**
* Initializes a display frame. Be careful, once a window size has been chosen,
* all videos must share the same window size
*
* @param id display unique identifier
* @param xsize width
* @param ysize heigth
*/
void yuvDisplayInit (int id, int xsize, int ysize)
{
	System_printf("yuvDisplayInit\n");

}
/**
* Display one YUV frame
*
* @param id display unique identifier
* @param y luma
* @param u chroma U
* @param v chroma V
*/
void yuvDisplay(int id, unsigned char *y, unsigned char *u, unsigned char *v)
{
	System_printf("yuvDisplay\n");

}
void yuvRefreshDisplay(int id)
{
}

void yuvFinalize(int id)
{

}
