#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <stdbool.h>

time_t t;

bool isSorted(int *arr, int size)
{
    for(int i = 0; i < size-1; i++) {
        if(arr[i] > arr[i+1]) return false;
    }
    return true;
}

void generateNumbers(int* arr, int size, int max, int min)
{
	srand(time(NULL));
	for(int i = 0; i<size; i++)
	{
		arr[i] = rand() % (max - min + 1) + min;
	}
}

void generateNumbersWithDuplicates(int* arr, int size, int max, int min)
{
	srand(time(NULL));
	for(int i = 0; i<size; i++)
	{
        if(i & 1) arr[i] = rand() % (max - min + 1) + min;
        else arr[i] = 69;
	}
}



int sumOfArray(int* arr, int size)
{
    int sum = 0;
    for(int i = 0; i<size; i++) {
        sum += arr[i];
    }
    return sum;
}

/*
Koden under er kopiert fra https://www.geeksforgeeks.org/dual-pivot-quicksort/
*/
int partition(int* arr, int low, int high, int* lp);

void swap(int* a, int* b)
{
    int temp = *a;
    *a = *b;
    *b = temp;
}
 
void DualPivotQuickSort(int* arr, int low, int high)
{
    if (low < high) {
        // lp means left pivot, and rp means right pivot.
        int lp, rp;
        rp = partition(arr, low, high, &lp);
        DualPivotQuickSort(arr, low, lp - 1);
        DualPivotQuickSort(arr, lp + 1, rp - 1);
        DualPivotQuickSort(arr, rp + 1, high);
    }
}
 
int partition(int* arr, int low, int high, int* lp)
{
    if (arr[low] > arr[high])
        swap(&arr[low], &arr[high]);
    // p is the left pivot, and q is the right pivot.
    int j = low + 1;
    int g = high - 1, k = low + 1, p = arr[low], q = arr[high];
    while (k <= g) {
 
        // if elements are less than the left pivot
        if (arr[k] < p) {
            swap(&arr[k], &arr[j]);
            j++;
        }
 
        // if elements are greater than or equal
        // to the right pivot
        else if (arr[k] >= q) {
            while (arr[g] > q && k < g)
                g--;
            swap(&arr[k], &arr[g]);
            g--;
            if (arr[k] < p) {
                swap(&arr[k], &arr[j]);
                j++;
            }
        }
        k++;
    }
    j--;
    g++;
 
    // bring pivots to their appropriate positions.
    swap(&arr[low], &arr[j]);
    swap(&arr[high], &arr[g]);
 
    // returning the indices of the pivots.
    *lp = j; // because we cannot return two elements
    // from a function.
 
    return g;
}

//Kode fra boken side 61.
int median3Sort(int *t, int v, int h) {
    int m = (v + h)/2;
    if(t[v]>t[m])swap(&t[v],&t[m]);
    if(t[m]>t[h]) {
        swap(&t[m],&t[h]);
        if(t[v]>t[m]) swap(&t[v], &t[m]);
    }
    return m;
}

//Kode fra boken side 62.
int splitt(int *t, int v, int h) {
    int iv, ih;
    int m = median3Sort(t,v,h);
    int dv = t[m];
    swap(&t[m], &t[h-1]);
    for(iv = v, ih = h-1;;){
        while(t[++iv]<dv);
        while(t[--ih]>dv);
        if(iv>=ih)break;
        swap(&t[iv], &t[ih]);
    }
    swap(&t[iv], &t[h-1]);
    return iv;
}

//Kode fra boken side 59.
void quickSort(int *t, int v, int h)
{
    if(h - v > 2) {
        int delepos = splitt(t, v, h);
        quickSort(t, v, delepos - 1);
        quickSort(t, delepos + 1, h);
    } else median3Sort(t, v, h);
}

bool testTime(int size, int maxRandomValue, void (*f)(int*, int, int), void (*d)(int*, int, int, int))
{
    int* arr = (int*) malloc(size * sizeof(int));
    (*d)(arr, size, 0, maxRandomValue);
    int sumBefore = sumOfArray(arr, size);

    t = clock();
    (*f)(arr, 0, size-1);
    t = clock() - t;
    double time = (double)t/CLOCKS_PER_SEC;
    
    if (sumBefore != sumOfArray(arr, size) || !isSorted(arr, size)){
        free(arr);
        return false;
    }

    if(size == 100000000 || size == 10000000) printf("%d\t%f\n", size, time);
    else printf("%d\t\t%f\n", size, time);

    free(arr);
    return true;
}

void generateSortedNumbers(int* arr, int size, int max, int min)
{
	srand(time(NULL));
	for(int i = 0; i<size; i++)
	{
		arr[i] = rand() % (max - min + 1) + min;
	}
    DualPivotQuickSort(arr, 0, size-1);
}

int main(int argc, char *argv[])
{
    int n = 100000000; //100 millioner

    printf("\nDual Pivot med få duplikater:\nn\t\tTime used\n");
    for(int i = 10; i<=n; i*=10){
        
        if (!testTime(i, n, DualPivotQuickSort, generateNumbers)){
            printf("FEIL");
            return -1;
        } 
    }

    printf("\nSingle Pivot med få duplikater:\nn\t\tTime used\n");
    for(int i = 10; i<=n; i*=10){
        if (!testTime(i, n, quickSort, generateNumbers)){
            printf("FEIL");
            return -1;
        } 
    }

    printf("\nDual Pivot der annen hver linje er samme:\nn\t\tTime used\n");
    for(int i = 10; i<=n; i*=10){
        
        if (!testTime(i, 100, DualPivotQuickSort, generateNumbersWithDuplicates)){
            printf("FEIL");
            return -1;
        } 
    }

    printf("\nSingle Pivot der annen hver linje er samme:\nn\t\tTime used\n");
    for(int i = 10; i<=n; i*=10){
        if (!testTime(i, 100, quickSort, generateNumbersWithDuplicates)){
            printf("FEIL");
            return -1;
        } 
    }

    printf("\nDual Pivot med sortert liste:\nn\t\tTime used\n");
    for(int i = 10; i<=n; i*=10){
        
        if (!testTime(i, 100, DualPivotQuickSort, generateSortedNumbers)){
            printf("FEIL");
            return -1;
        } 
    }

    printf("\nSingle Pivot med sortert liste:\nn\t\tTime used\n");
    for(int i = 10; i<=n; i*=10){
        if (!testTime(i, 100, quickSort, generateSortedNumbers)){
            printf("FEIL");
            return -1;
        } 
    }

    return 0;
}
