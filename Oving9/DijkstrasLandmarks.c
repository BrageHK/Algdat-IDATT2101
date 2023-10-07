#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <stdbool.h>

#define INF 0x7FFFFFFF;

typedef struct vertex_t g_vertex;

struct vertex_t {
    int value;
    int distance;
    bool isFound;
    g_vertex *link;
};

g_vertex *init_vertex(int value, int distance) {
    g_vertex *vertex = (g_vertex*) malloc(sizeof(g_vertex));
    vertex->distance = distance;
    vertex->value = value;
    vertex->isFound = false;
    vertex->link = NULL;
    return vertex;
}

g_vertex *init_vertex(int value) {
    g_vertex *vertex = (g_vertex*) malloc(sizeof(g_vertex));
    vertex->distance = INF;
    vertex->value = value;
    vertex->isFound = false;
    vertex->link = NULL;
    return vertex;
}

typedef struct graph_t graph;

struct graph_t {
    int numberOfNodes;
    int numberOfPaths;
    g_vertex **linkedVertecies
};

graph *init_graph(int numberOfNodes, int numberOfPaths) {
    graph *graf = (graph*) malloc(sizeof(graph));
    graf->numberOfNodes = numberOfNodes;
    graf->numberOfPaths = numberOfPaths;
    graf->linkedVertecies = NULL;
    return graf;
}

void addEdge(graph *graf, int from, int to, int weight) {
    g_edge *curr_edge = graf->nodes[from]->edge;

    g_edge *new = init_edge(to, weight);
    new->next = graf->nodes[from]->edge;
    graf->nodes[from]->edge = new;    
}

typedef struct HeapEdge_t {
    int num;
    int weight;
} HeapEdge;

typedef struct MinHeap_t {
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


int main() {


    return 0;
}