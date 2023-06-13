// Skrevet av Brage H. Kvamme and Eilert W. Hansen
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <stdbool.h>

// Uendelig
#define INF 0x7FFFFFFF;

typedef struct edge2_t {
    int num;
    int weight;
} HeapEdge;

typedef struct MinHeap {
    HeapEdge **arr;
    int size;
    int capacity;
} MinHeap;

MinHeap *init_minheap(int capacity) {
    MinHeap *heap = (MinHeap*) malloc(sizeof(MinHeap));
    heap->arr = (HeapEdge**) malloc(capacity * sizeof(HeapEdge));
    heap->size = 0;
    heap->capacity = capacity;
    return heap;
}

HeapEdge *init_heap_edge(int num, int weight) {
    HeapEdge *edge = (HeapEdge*) malloc(sizeof(HeapEdge));
    edge->num = num;
    edge->weight = weight;
    return edge;
}

int getLeftChildIndex(int k) {
    return (k << 1) + 1;
}

int getRightChildIndex(int k) {
    return (k + 1) << 1;
}

int getParentIndex(int k) {
    return (k - 1) >> 1;
}

void swap(MinHeap *heap, int i, int j) {
    HeapEdge *temp = heap->arr[i];
    heap->arr[i] = heap->arr[j];
    heap->arr[j] = temp;
}

MinHeap *insert_heap(MinHeap *heap, int num, int weight) {
    // Hvis heap er full
    if(heap->size == heap->capacity) {
        fprintf(stderr, "Heap is full! Can't add %d.\n", num);
        return heap;
    }

    HeapEdge *edge = init_heap_edge(num, weight);
    heap->size++;
    heap->arr[heap->size-1] = edge;

    // Setter inn bakerst
    int curr_index = heap->size - 1;

    while(curr_index > 0 && heap->arr[getParentIndex(curr_index)]->weight > heap->arr[curr_index]->weight) {
        swap(heap, curr_index, getParentIndex(curr_index));

        curr_index = getParentIndex(curr_index);
    }
    return heap;
}

void fix_heap(int index, MinHeap *heap) {
    int m = getLeftChildIndex(index);
    if(m < heap->size) {
        int h = m + 1;
        if (h < heap->size && heap->arr[h]->weight < heap->arr[m]->weight) m = h;
        if (heap->arr[m]->weight < heap->arr[index]->weight) {
            swap(heap, index, m);
            fix_heap(m, heap);
        }
    }
}

int extractMinIndex(MinHeap *heap) {
    if(heap->size == 0) {
        fprintf(stderr, "Heap is empty! Can't extract minimum value.");
        return 0;
    }

    int minIndex = heap->arr[0]->num;

    int size = heap->size;
    HeapEdge *last_element = heap->arr[size-1];
    
    // Update root value with the last element
    heap->arr[0] = last_element;

    // Now remove the last element, by decreasing the size
    heap->size--;
    size--;

    // We need to call heapify(), to maintain the min-heap
    // property
    fix_heap(0, heap);
    return minIndex;
}

typedef struct node_t g_node;
typedef struct graph_t graph;
typedef struct edge_t g_edge;
typedef struct node_info_t node_info;
typedef struct node_info_list_t node_info_list;

struct node_t {
    int prev;
    int distance;
    g_edge *edge;
};

struct edge_t {
    int index;
    int weight;
    g_edge *next;
};

struct graph_t {
    int size;
    int edges;
    g_node **nodes;
};

struct node_info {
    int node_index;
    int cost_from_start;
};

struct shortest_path {
    int last_index;
    int total_cost;
};

struct node_info_t {
    int weight; // vekt på forskjellig index
    int to;
};

struct node_info_list_t {
    int *node_info;
};

node_info *init_node_info(int edges) {
    node_info *info = (node_info*) malloc(sizeof(node_info));
    //info->weight = calloc(edges, sizeof(int));
    return info;
}

node_info_list *init_info_list(int edges ) {
    node_info_list *list = malloc(sizeof(node_info_list));
    list->node_info = calloc(edges, sizeof(node_info));
    return list;
}

void addInfo(node_info_list *info, int from, int to, int weight) {
    //info->node_info[from];
}

g_node *init_node() {
    g_node *node = (g_node*) malloc(sizeof(g_node));
    node->distance = INF;
    node->edge = NULL;
    return node;
}

g_edge *init_edge(int index, int weight) {
    g_edge *edge = malloc(sizeof(g_edge));
    edge->next = NULL;
    edge->index = index;
    edge->weight = weight;
    return edge;
}

graph *init_graph(int size, int edges) {
    graph *graf = malloc(sizeof(g_node));
    graf->size = size;
    graf->edges = edges;
    graf->nodes = (g_node**) calloc(graf->size, sizeof(g_node*));
    for(int i = 0; i < size; i++) {
        graf->nodes[i] = init_node();
    }
    return graf;
}

void addEdge(graph *graf, int from, int to, int weight) {
    g_edge *curr_edge = graf->nodes[from]->edge;

    g_edge *new = init_edge(to, weight);
    new->next = graf->nodes[from]->edge;
    graf->nodes[from]->edge = new;    
}

void printResult(struct shortest_path *shortest_path, graph *graf, int start) {
    printf("Node\tforgjenger\tdistanse\n");
    for(int i = 0; i < graf->size; i++) {
        printf("%d\t", i);
        if(i == start) {
            printf("Start\t\t%d\n", shortest_path[i].total_cost); // Denne skal alltid bli 0.
        } else if(shortest_path[i].last_index == -1) { 
            printf("\t\tnåes ikke\n");
        } else {
            printf("%d\t\t%d\n", shortest_path[i].last_index, shortest_path[i].total_cost);
        }
    }
    //printf("%d\t\t%d\n", shortest_path[102331].last_index, shortest_path[102331].total_cost);
}

struct shortest_path *dijkstra(graph *graf, int startNode) {
    
    bool visited[graf->size];
    memset(visited, false, sizeof(bool) * graf->size);

    struct shortest_path *shortest_path = malloc(sizeof(struct shortest_path) * graf->size);

    MinHeap *heap = init_minheap(graf->edges);


    for(int i = 0; i < graf->size; i++) {
        g_node *node = graf->nodes[i];
        if(i != startNode) {
            shortest_path[i].total_cost = INF;
            shortest_path[i].last_index = -1;
        }
    }

    shortest_path[startNode].total_cost = 0;
    shortest_path[startNode].last_index = -1;

    insert_heap(heap, startNode, 0);

    while (heap->size != 0) {
        int index = extractMinIndex(heap); // Henter minste node i kø
        g_node *u = graf->nodes[index];         // Finner denne noden i grafen

        g_edge *v = u->edge;
        visited[index] = true;
        while (v != NULL) {
            if(!visited[v->index]) {
                int new_cost = shortest_path[index].total_cost + v->weight;
                if(new_cost < shortest_path[v->index].total_cost) {
                    shortest_path[v->index].total_cost = new_cost;
                    shortest_path[v->index].last_index = index;
                    insert_heap(heap, v->index, new_cost);
                }
            }
            
            v = v->next;
        }
    }
    return shortest_path;
}

void printDijkstra(graph *graf, int startNode) {
    struct shortest_path *shortest_path = dijkstra(graf, startNode);
    printResult(shortest_path, graf, startNode);
}

void readFile(char *path, int startNode, int printGraph) {
    clock_t t;
    double cpu_time_used;
    t = clock();

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
        int weight = (int) strtol(ptr, &ptr, 10);
        addEdge(graf, from, to, weight);

    }  

    fclose(fp);

    t = clock() - t;


    printf("File name: %s Start position: %d\n", path, startNode);
    printf("Reading file took %f seconds\n", ((double)t)/CLOCKS_PER_SEC);
   
    t = clock();    
    struct shortest_path *shortest_path = dijkstra(graf, startNode);
    t = clock()-t;
    cpu_time_used = ((double)t) / CLOCKS_PER_SEC;
    
    if(printGraph) {
        printResult(shortest_path, graf, startNode);
    }
   
    printf("Tid brukt på algoritme (ikke printing): %f\n\n", cpu_time_used);
}

int main() {
    // true => print graph
    // false => don't print graph
    readFile("./vg1", 1, true);
    readFile("./vg5", 1, true);
    readFile("./vg2", 1, true);
    readFile("./vg3", 1, false);
    readFile("./vg4", 1, false);
    readFile("./vgSkandinavia", 1, false);

    //readFile("island_kanter.txt", 0, true);
    

    return 0;
}