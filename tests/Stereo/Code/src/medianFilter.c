
#include "medianFilter.h"
#define min(x,y) (((x)<(y))?(x):(y))
#define max(x,y) (((x)<(y))?(y):(x))


void swap(unsigned char *a, unsigned char *b){
	unsigned char buf = *a;
	*a=*b;
	*b=buf;
}

void quickSortPartition(int startIdx, int endIdx, int *pivotIdx, unsigned char *values){
	int idx;
	int swapIdx = startIdx;
	swap(values+*pivotIdx,values+endIdx);
	for(idx = startIdx; idx < endIdx; idx++){
		if(values[idx]<=values[endIdx]){
			swap(values+swapIdx,values+idx);
			swapIdx++;
		}
	}
	swap(values+swapIdx, values+endIdx);
	*pivotIdx = swapIdx;
}


void quickSort(int startIdx, int endIdx, unsigned char *values){
	if(startIdx<endIdx){
		int pivotIdx = startIdx;
		quickSortPartition(startIdx, endIdx, &pivotIdx, values);
		quickSort(startIdx,pivotIdx-1,values);
		quickSort(pivotIdx+1,endIdx,values);
	}	
}

void medianFilter (int height , int width, unsigned char *rawDisparity, unsigned char *filteredDisparity)
{	
	int i,j;
	int k,l;
	// Process pixels one by one
	for(j=0; j< height; j++){
		for(i=0;i<width;i++){
			unsigned char pixels[9];
			// output pixel is the median of a 3x3 window
			// Get the 9 pixels
			for(l=-1;l<=1;l++){
				int y = min(max(j+l,0),height-1);
				for(k=-1;k<=1;k++){
					int x = min(max(i+k,0),width-1);
					pixels[(l+1)*3+k+1] = rawDisparity[y*width+x];
				}
			}

			// Sort the 9 values
			quickSort(0, 8, pixels);
			filteredDisparity[j*width+i] = pixels[9/2];
		}
	}
}
