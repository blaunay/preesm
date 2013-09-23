/*
	============================================================================
	Name        : ppm.c
	Author      : kdesnos
	Version     : 1.0
	Copyright   : CECILL-C
	Description :
	============================================================================
*/

#include "ppm.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

/*========================================================================

   Global Variable

   ======================================================================*/
#define NB_PATH 2
char* paths[] = {"./im0.ppm","./im1.ppm"};
char* outPath = "./out.ppm";
static FILE * ptfile[NB_PATH] ;
clock_t tick;
long imageStartPosition[NB_PATH];

void writePPM(int height, int width, unsigned char *gray){
	FILE * outFile;
	int i;
	if((outFile = fopen(outPath, "wb+")) == NULL )
    {
        fprintf(stderr,"ERROR: Task read cannot create/open ppm_file '%s'\n", outPath);
        system("PAUSE");
        return;
    }

	fprintf(outFile,"P6\n");
	fprintf(outFile,"%d %d\n",width,height);
	fprintf(outFile,"255\n");
	for(i=0; i<height*width;i++){
		fwrite(gray+i,sizeof(char),1,outFile);
		fwrite(gray+i,sizeof(char),1,outFile);
		fwrite(gray+i,sizeof(char),1,outFile);
	}

	fclose(outFile);
}

void readPPMInit(int id,int height, int width) {
    char magicNumber[3];
    int readWidth;
    int readHeight;
    int maxRGBValue;
	int fsize;

	printf("readPPMInit()\n");
    if((ptfile[id] = fopen(paths[id], "rb")) == NULL )
    {
        fprintf(stderr,"ERROR: Task read cannot open ppm_file '%s'\n", paths[id]);
        system("PAUSE");
        return;
    }

    // Read ppm file header
    // 1. Magic Numper
    fread(magicNumber, sizeof(char),2, ptfile[id]);
    magicNumber[2] = '\0';
    if(strcmp(magicNumber,"P6")){
        fprintf(stderr,"ERROR: PPM_file '%s' is not a valid PPM file.\n", paths[id]);
        system("PAUSE");
        return;
    }
    fseek(ptfile[id],1,SEEK_CUR); // skip space or EOL character


    // 2. Width and Height
    fscanf(ptfile[id],"%d", &readWidth);
    fscanf(ptfile[id],"%d", &readHeight);
    if(readWidth!=width || readHeight!= height){
        fprintf(stderr,"ERROR: PPM_file '%s' has an incorrect resolution.\nExpected: %dx%d\t Read: %dx%d\n", paths[id], width, height, readWidth,readHeight);
        system("PAUSE");
        return;
    }
    fseek(ptfile[id],1,SEEK_CUR); // skip space or EOL character

    // 3. Max RGB value
    fscanf(ptfile[id],"%d", &maxRGBValue);
    if(maxRGBValue > 255){
        fprintf(stderr,"ERROR: PPM_file '%s' has is coded with 32bits values, 8bits values are expected.\n", paths[id]);
        system("PAUSE");
        return;
    }
    fseek(ptfile[id],1,SEEK_CUR); // skip space or EOL character

    // Register the position of the file pointer
    imageStartPosition[id] = ftell(ptfile[id]);

    // check file size:
    fseek (ptfile[id] , 0 , SEEK_END);
    fsize = ftell (ptfile[id]) - imageStartPosition[id];
    fseek(ptfile[id],imageStartPosition[id], SEEK_SET);

    if(fsize != height*width*3)
    {
        fprintf(stderr,"ERROR: PPM_file has incorrect data size.\n\nExpected: %d\t Read: %d\n",height*width*3, fsize);
        system("PAUSE");
        return;
    }

    // Set initial clock
    tick = clock();
}

void readPPM(int id,int height, int width, unsigned char *r, unsigned char *g, unsigned char *b){
    int idxPxl;
    int rgb;
    unsigned char *readBuffer;

	if(id == 1){
		tick = clock()-tick;
		printf("\nMain: Processed in %f => %f fps\n",tick/(float)CLOCKS_PER_SEC,1/(float)tick*(float)CLOCKS_PER_SEC);
		tick = clock();
	}

    fseek(ptfile[id],imageStartPosition[id], SEEK_SET);

    readBuffer = malloc(3*height*width*sizeof(char));

    fread(readBuffer,sizeof(char), 3*width*height, ptfile[id]);
    rgb = 0;
    for(idxPxl = 0; idxPxl < 3*height*width; idxPxl++){
        switch(rgb){
        case 0:
            *(r+idxPxl/3) = *(readBuffer+idxPxl);
            break;
        case 1:
            *(g+idxPxl/3) = *(readBuffer+idxPxl);
            break;
        case 2:
            *(b+idxPxl/3) = *(readBuffer+idxPxl);
            break;
        }
        rgb = (rgb + 1)%3;
    }

	free(readBuffer);
}
