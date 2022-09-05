#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int buyPrice = 10000;
int sellPrice = 10000;
int timeToBuy, timeToSell = 0;
int difference = 0;
int priceDifference[1000];
int finalSell;
int finalBuy;

time_t start, end;

//Generates random numbers.
void GenerateNumbers(int max, int min)
{
	srand(time(NULL));
	for(int i = 0; i<sizeof(priceDifference)/sizeof(int); i++)
	{
		priceDifference[i] = rand() % (max - min + 1) + min;
	}
}

//Prints priceDifference.
void PrintArray()
{
	printf("{");
	for(int i = 0; i<sizeof(priceDifference)/sizeof(int); i++)
	{
		if(i == sizeof(priceDifference)/sizeof(int)-1)
		{
			printf("%d", priceDifference[i]);

		}else {
			printf("%d,", priceDifference[i]);
		}
	}
	printf("}\n");
}

//The algorigth to find the best day to sell and buy.
void Oppgave1()
{

	for(int i = 0; i<sizeof(priceDifference)/sizeof(int); i++)
	{
		for(int j = i; j<sizeof(priceDifference)/sizeof(int); j++)
		{
			sellPrice += priceDifference[j];

			if(sellPrice - buyPrice > difference)
			{
				difference = sellPrice - buyPrice;
				finalSell = sellPrice;
				timeToSell = j+2;
				finalBuy = buyPrice;
				timeToBuy = i+1;
			}
		}

		buyPrice += priceDifference[i];
		sellPrice = buyPrice;
	} 
	
}

int main(int argc, char *argv[])
{
	GenerateNumbers(10,-10);
	//PrintArray();
	
	start = clock();//Starting clock before running the algorithm
	Oppgave1();
	end = clock();//Ending clock after running the algorithm

	printf("\nBuy at day %d, where the price is %d and sell at day, %d where the price is %d. Price difference: %d.\n",
	timeToBuy, finalBuy, timeToSell, finalSell, difference);
	printf("Time used to run program: %f\n", (double)(end-start)/CLOCKS_PER_SEC);

	return 0;
}





