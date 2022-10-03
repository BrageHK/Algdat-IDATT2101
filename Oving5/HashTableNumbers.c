#include <stdio.h>
#include <stdlib.h> // calloc, malloc
#include <string.h> // strcpy, strlen
#include <time.h>
#include <math.h>

#define CAPACITY 12000017 // Prime number

typedef struct ht_item_t ht_item;
typedef struct ht_t ht;

struct ht_item_t {
    unsigned int *value;
    unsigned int *key;
};

struct ht_t {
    unsigned int capacity;
    unsigned int count;
    ht_item **items;
};

ht *init_ht(unsigned int capacity) {
    ht *table = malloc(sizeof(CAPACITY));
    table->capacity = capacity;
    table->count = 0;
    table->items = (ht_item**) calloc(CAPACITY, sizeof(ht_item*));
    return table;
}

ht_item *init_ht_item(unsigned int key, unsigned int value) {
    ht_item *item = (ht_item*) malloc(sizeof(ht_item));
    item->key = (unsigned int*) malloc(sizeof(key+1));
    item->value = (unsigned int*) malloc(sizeof(value+1));

    item->key = &key;
    item->value = &value;
    return item;
}

unsigned int hash1(unsigned int key) {
    return key % CAPACITY;
}

unsigned int hash2(unsigned int key) {
    return (key % (CAPACITY - 1)) + 1;
}

// Returns the number of collisions
int insert(ht *table, unsigned int key, unsigned int value) {
    ht_item *item = init_ht_item(key, value);
    int collisions = 0;

    unsigned int index = hash1(key);
    if(table->items[index] == 0) {
        table->items[index] = item;
        return 0;
    }
    unsigned int h2 = hash2(key);
    for (;;) {
        collisions++;
        index += h2;
        index %= CAPACITY;
        if (table->items[index] == 0) { // feil på denne linjen
            table->items[index] = item;
            table->count++;
            return collisions;
        }
    }
}

unsigned int *createRandomArray(unsigned int size) {
    srand(time(0));
    unsigned int *array = (unsigned int*) malloc(size * sizeof(unsigned int));
    for (int i = 0; i < size; i++) {
        array[i] = rand();
    }
    return array;
}

int main() {
    int size = 10000000;
    int collisions = 0;
    ht *table = init_ht(CAPACITY);
    unsigned int *array = createRandomArray(size); // 10 million random numbers

    clock_t t = clock();
    for (int i = 0; i < size; i++) {
        collisions += insert(table, array[i], array[i]);
    }
    t = clock() - t;
    double time = (double)(t)/CLOCKS_PER_SEC;
    

    printf("Times used to insert %d numbers: %f seconds\n", size, time);
    printf("Number of collisions: %d\n", collisions);
    printf("Average collisions per item: %f\n", (float)collisions / size);
    printf("Load factor: %f\n",((float)table->count / table->capacity ));
    return 0;
}