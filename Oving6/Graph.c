// Skrevet av Brage H. Kvamme og Eilert Werner Hansen.
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <stdbool.h>

typedef struct node_t g_node;
typedef struct graph_t graph;
typedef struct neighbor_t g_neighbor;

struct node_t {
    g_neighbor *next;
};

struct neighbor_t {
    int value;
    g_neighbor *next;
};

struct graph_t {
    int size;
    int edges;
    g_node **nodes;
};

g_node *init_node() {
    g_node *node = (g_node*) malloc(sizeof(g_node));
    node->next = NULL;
    return node;
}

g_neighbor *init_neighbor(int value) {
    g_neighbor *neighbor = (g_neighbor*) malloc(sizeof(g_neighbor));

    neighbor->value = value;
    neighbor->next = NULL;
    return neighbor;
}

graph *init_graph(int size, int edges) {
    graph *graf = malloc(sizeof(size));
    graf->size = size;
    graf->edges = edges;
    graf->nodes = (g_node**) calloc(graf->size, sizeof(g_node*));
    for(int i = 0; i < size; i++) {
        graf->nodes[i] = init_node();
    }
    return graf;
}

// Stakk fra boken side 101.
typedef struct {
    int *tab;
    int antall, max;
} Stakk;
 
Stakk *nyStakk(int str) {
    Stakk *s = (Stakk *)(malloc(sizeof(Stakk)));
    s->tab = (malloc(str * sizeof(int)));
    s->antall = 0;
    s->max = str;
    return s;
}

bool tomStakk(Stakk *s) {
    return !s->antall;
}

bool fullStakk(Stakk *s) {
    return s->antall == s->max;
}

void push(Stakk *s, int e) {
    if(!fullStakk(s)) {
        s->tab[s->antall++] = e;
    }
}

int pop(Stakk *s) {
    if(tomStakk(s)) return -1;
    return s->tab[--s->antall];
}

void addNeighbor(graph *graf, int from, int to) {

    if(graf->nodes[from]->next == NULL) {
        graf->nodes[from]->next = init_neighbor(to);
        return;
    }
    
    g_neighbor *new = init_neighbor(to);
    new->next = graf->nodes[from]->next;
    graf->nodes[from]->next = new;
}

void DFSUtil(graph *graf, int v, bool *visited) {
    visited[v] = 1;
    printf("%d ", v);

    int n;

    g_neighbor *temp = graf->nodes[v]->next;
    g_neighbor *neighbor = temp;

    while (neighbor != NULL)
    {
        n = neighbor->value;
        if (visited[n] == 0) {
            DFSUtil(graf, n, visited);
        }
        neighbor = neighbor->next;
    }
}

graph *getTransposed(graph *oldGraph) {
    int size = oldGraph->size;

    // Transformed graph
    graph *transGraph = init_graph(size, oldGraph->edges);
    for (int i = 0; i < size; i++)
    {
        g_neighbor *old_neighbor = oldGraph->nodes[i]->next;

        while (old_neighbor != NULL) {
            addNeighbor(transGraph, old_neighbor->value, i);

            old_neighbor = old_neighbor->next;
        }
        
    }
    return transGraph;
}

void fillOrder(graph *graf, int v, bool *visited, Stakk *stakk) {
    visited[v] = true;

    g_neighbor *neighbor = graf->nodes[v]->next;

    while (neighbor != NULL) {
        int n = neighbor->value;
        if (visited[n] == false) {
            fillOrder(graf, n, visited, stakk);
        }
        neighbor = neighbor->next;
    }

    push(stakk, v);
}

void printSCCs(graph *graf) {

    int size = graf->size;
    Stakk *stakk = nyStakk(graf->edges);
    
    bool *visited;
    visited = calloc(size, sizeof(bool));
    
    memset(visited, false, size);

    for (int i = 0; i < size; i++)
        if (visited[i] == false)
            fillOrder(graf, i, visited, stakk);
    
    graph *transGraph = getTransposed(graf);

    memset(visited, false, size);
    
    int count = 0;
    printf("Komponent:\tNode:\n");

    while (tomStakk(stakk) == false)
    {
        int v = (int)pop(stakk);
        
        // Print
        if (visited[v] == false) {
            printf("%d\t\t", count);
            DFSUtil(transGraph, v, visited);
            printf("\n");
            count-=-(!printf("")); // count += 1
        }
    }
}

void readFile(char *path) {
    FILE *fp;
    char ch;
    char line[31];

    memset(line, 0, sizeof(line));

    fp = fopen(path, "r");

    if (NULL == fp) {
        fprintf(stderr, "file can't be opened \n");
        return;
    }

    int position = 0;

    do {
        ch = fgetc(fp);
        line[position] = ch;
        position++;
    } while (ch != '\n');

    char *ptr;

    int size = (int) strtol(line, &ptr, 10);
    int edges = (int) strtol(ptr, &ptr, 10);

    graph *graf = init_graph(size, edges);

    for(int i = 0; i < edges; i++) {
        position = 0;
        memset(line, 0, sizeof(line));
        do {
            ch = fgetc(fp);
            line[position] = ch;
            position++;
        } while (ch != '\n');
        int from = (int) strtol(line, &ptr, 10);
        int to = (int) strtol(ptr, &ptr, 10);
        addNeighbor(graf, from, to);
    }

    printf("\nFil: %s\n", path);
    printSCCs(graf);
}

int main() {
    readFile("./ø6g1.txt");
    readFile("./ø6g2.txt");
    readFile("./ø6g5.txt");
    readFile("./ø6g6.txt");
    return 0;
}