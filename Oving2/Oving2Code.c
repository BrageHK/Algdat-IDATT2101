#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <stdbool.h>

time_t start, end;

// O(n) algorithm
double power(double x, int n)
{
    if(n == 0) return 0;
    if(n == 1) return x;
    if(n > 0) return x * power(x, n-1);
    return -1;
}

// O(log(n)) algorithm
double power2(double x, int n)
{
    if(n == 0) return 1;
    if((n & 1)==1) return x * power2(x*x,(n-1)/2);   // even
    if((n & 1)==0) return power2(x*x,(n)/2);         // odd
    return x;
}

// Time is in seconds
void timePower(double x, int n, double time)
{
    start = clock(); // Starting clock before running the algorithm
    double num;
    int runder = 0;
    double tid;

    do
    {
        num = power(x,(n));
        end = clock();
        ++runder;
    } while ((double)(end-start)/CLOCKS_PER_SEC < time);
    
    printf("%d\t%f\t%d\n", n, ((((double)(end-start)/CLOCKS_PER_SEC)/runder))*1000, runder);
}

// Time is in seconds
void timePower2(double x, int n, double time)
{
    start = clock(); // Starting clock before running the algorithm
    double num;
    int runder = 0;
    double tid;

    do
    {
        num = power2(x,(n));
        end = clock();
        ++runder;
    } while ((double)(end-start)/CLOCKS_PER_SEC < time);
    
    printf("%d\t%f\t%d\n", n, ((((double)(end-start)/CLOCKS_PER_SEC)/runder))*1000, runder);

}

// Time is in seconds
void timePow(double x, int n, double time)
{
    start = clock(); // Starting clock before running the algorithm
    double num;
    int runder = 0;
    double tid;

    do
    {
        num = pow(x,(n));
        end = clock();
        ++runder;
    } while ((double)(end-start)/CLOCKS_PER_SEC < time);
    
    printf("%d\t%f\t%d\n", n, ((((double)(end-start)/CLOCKS_PER_SEC)/runder))*1000, runder);

}

// Checks if algorithms are working
bool checkAlgorithm(int x, int n, int answer)
{
    if(power(x,n) != answer) return false;
    if(power2(x,n) != answer) return false;
    if(pow(x,n) != answer) return false;
    return true;
}


int main(int argc, char *argv[])
{
    // Check if methods work
    if(!checkAlgorithm(2, 12, 4096)) return -1;
    if(!checkAlgorithm(3, 10, 59049)) return -1;
    
    // Testing for different values of n
    double x = 1.01;
    int n = 10;
    double time = 2;

    printf("\nUsing algorithm from 2.1-1 - O(n):\n");
    printf("N\tms per round\trounds\n");
    timePower(x, 10, time);
    timePower(x, 100, time);
    timePower(x, 1000, time);
    timePower(x, 10000, time);

    printf("\nUsing algorithm from 2.1-1 - O(log(n)):\n");  
    printf("N\tms per round\trounds\n");
    timePower2(x, 10, time);  
    timePower2(x, 100, time);  
    timePower2(x, 1000, time);  
    timePower2(x, 10000, time);  

    printf("\nUsing bult in pow method:\n");
    printf("N\tms per round\trounds\n");
    timePow(x, 10, time);
    timePow(x, 100, time);
    timePow(x, 1000, time);
    timePow(x, 10000, time);
    
    return 0;
}