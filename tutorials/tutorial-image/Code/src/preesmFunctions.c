/*

Functions enabling the use of Preesm calls with special arguments (argc, argv for example)

*/
#include "displayYUV.h"
#include "preesmPrototypes.h"

#ifdef WIN32
// Table of display windows
T_VDWINDOW_YUV   Window_YUV [2];
int              NumberofWindows_YUV = 2;
#endif


// Ignored function
void noCall(){
}


void Read_YUV_init_preesm(int xsize, int ysize){
	Read_YUV_init("D:/IETR/Sequences/cif/akiyo/akiyo_cif.yuv",xsize, ysize,25);
}

