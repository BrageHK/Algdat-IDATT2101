#include <stdio.h>
#include <stdlib.h> // calloc, malloc
#include <string.h> // strcpy, strlen
#include <time.h>

#define CAPACITY 140

typedef struct ht_item_t ht_item;
typedef struct ht_t ht;

struct ht_item_t {
    char *value;
    char *key;
    ht_item *next;
};

struct ht_t {
    int capacity;
    int count;
    ht_item **items;
};

ht *init_ht(int capacity) {
    ht *table = malloc(sizeof(capacity));
    table->capacity = capacity;
    table->count = 0;
    table->items = (ht_item**) calloc (table->capacity, sizeof(ht_item*));
    return table;
}

ht_item *init_ht_item(char *key, char *value) {
    ht_item *item = (ht_item*) malloc(sizeof(ht_item));
    item->key = (char*) malloc(strlen(key) + 1);
    item->value = (char*) malloc(strlen(value) + 1);
    item->next = NULL;

    strcpy(item->key, key);
    strcpy(item->value, value);
    return item;
}

unsigned int hashString(char *str) {
    unsigned int hash = 0;
    for (int i = 0; i < strlen(str); i++) {
        hash += (unsigned int)((hash << 5) + str[i]);
    }
    return hash % CAPACITY;
}

// Returns the number of collisions
int insert(ht *table, char *key, char *value) {
    ht_item *item = init_ht_item(key, value);
    unsigned int index = hashString(key);
    ht_item *current_item = table->items[index];
    ht_item *prev_item = NULL;

    int collisions = 0;
    if(current_item == NULL) {
        table->items[index] = item;
    }
    else {
        while(current_item != NULL) {
            if(strcmp(current_item->key, key) == 0) {
                strcpy(current_item->value, value);
                return collisions;
            }
            prev_item = current_item;
            current_item = current_item->next;
            collisions++;
            printf("%s collided at index %d.\n", key, index); // Ikke optimalt å printe fra funksjonen, men det er lettere.
        }
        prev_item->next = item;
    }

    table->count++;
    return collisions;
}


char *search(ht *table, char *key) {
    unsigned int index = hashString(key);
    ht_item *item = table->items[index];
    while (item != NULL) {
        if (strcmp(item->key, key) == 0) {
            return item->value;
        }
        item = item->next;
    }
    return NULL;
}

int insertFromFile(char *path, ht *table)
{
    int size = 40;
    int collisions = 0;
    
    FILE *fp;
    char ch;
    char name[size];

    memset(name, 0, sizeof(name));

    fp = fopen(path, "r");

    if (NULL == fp) {
        fprintf(stderr, "file can't be opened \n");
        return -1;
    }

    int position = 0;
    do {
        ch = fgetc(fp);
        if(ch == '\n') {
            name[position] = '\0';
            collisions += insert(table, name, name);
            position = 0;
        }else {
            name[position] = ch;
            position++;
        }
        

    } while(ch != EOF);

    return collisions;
}
    

int main()
{
    int collisions = 0;
    ht *table = init_ht(CAPACITY);

    collisions += insertFromFile("./navn.txt", table);


    printf("\n\nNumber of collisions: %d\n", collisions);
    printf("Collisions per item: %f\n", (float)collisions / CAPACITY);
    printf("Load factor: %f\n",((float)table->count / table->capacity ));

    printf("My name: %s\n", search(table, "Brage Halvorsen Kvamme"));
    return 0;
}