/* I denne oppgaven har jeg sammarbeidet litt med Nicolai, men jeg har ikke gjort alt sammen med han */
#include <stdlib.h>     // malloc, free
#include <stdint.h>     // uint8_t type
#include <stdio.h>      // fprintf, printf
#include <stdbool.h> 	// carry
#include <string.h> 	// strlen

typedef uint8_t u8;

typedef struct node_t Node;
typedef struct doubly_linked_list_t LinkedList;

struct node_t {
    u8 value;
    Node *next;
    Node *prev;
};

struct doubly_linked_list_t {
    Node *head;
    Node *tail;
    size_t size;
};


Node *node_malloc(u8 value)
{
    Node *n = malloc(sizeof(Node));
    n->value = value;
    return n;
}

inline void node_free(Node *n)
{
    free(n);
}

void linked_list_append(LinkedList *l, u8 value)
{
    Node *n = node_malloc(value);

    if (l->size == 0) {
        l->tail = n;
    } else {
        /* point old tail to new tail */
        l->tail->next = n;
        n->prev = l->tail;
        /* update the LinkedList's reference to the new tail */
        l->tail = n;
    }
    l->size++;
}

void linked_list_prepend(LinkedList *l, u8 value)
{
    Node *n = node_malloc(value);

    if (l->size == 0) {
        l->head = n;
    } else {
        /* point old head to new head */
        l->head->prev = n;
        n->next = l->head;
        /* update the LinkedList's reference to the new head */
        l->head = n;
    }
    l->size++;
}

void node_add_head(Node **ref, u8 value)
{
    Node *n = node_malloc(value);
    (*ref)->prev = n;
    n->next = *ref;
    *ref = n;
}

LinkedList *parse(char *str)
{
    char c;
    LinkedList *l = malloc(sizeof(LinkedList));

    while ((c = *str++) != 0)
        linked_list_append(l, (u8)c - '0');

    return l;
}

void print(LinkedList *l)
{
    Node *n = l->head;

    for (Node *n = l->head; n != NULL; n = n->next)
        printf("%d", n->value);

    putchar('\n');
}

LinkedList *add(LinkedList *a, LinkedList *b)
{
    u8 sum;
	bool carry = false;
    LinkedList *res = malloc(sizeof(LinkedList));
    Node *a_ref = a->tail, *b_ref = b->tail;

   	while(a_ref != NULL || b_ref != NULL) {
		sum = carry;

		if (a_ref != NULL) {
			sum += a_ref->value;
		}
		if (b_ref != NULL) {
			sum += b_ref->value;
		}

		if(sum>9) {
			linked_list_prepend(res, sum-10);
			carry = true;
		}else {
			linked_list_prepend(res, sum);
		}
		if (a_ref != NULL) {
			a_ref = a_ref->prev;
		}
		if (b_ref != NULL) {
			b_ref = b_ref->prev;
		}
    }

	if(carry) {
		linked_list_prepend(res, 1);
	}

    return res;
}

LinkedList *sub(LinkedList *a, LinkedList *b)
{
	u8 sum;
	bool carry = false;
	LinkedList *res = malloc(sizeof(LinkedList));
    Node *a_ref = a->tail, *b_ref = b->tail;

	while(a_ref != NULL && b_ref != NULL) {
		u8 sum = 0;
		
		// If the last b-number was larger than the last b-number, then this a-number is subtracted by 1.
		if(carry){
			if(a_ref->value != 0){
				a_ref->value -= 1;
				carry = false;
			} else {
				a_ref->value = 9;
			}
		}
		
		if(a_ref->value >= b_ref->value) {
			sum = a_ref->value - b_ref->value;
		} else {
			sum = 10 + a_ref->value - b_ref->value;
			carry = true;
		}

		linked_list_prepend(res, sum);
		
		a_ref = a_ref->prev;
		b_ref = b_ref->prev;
		
	}

	return res;
}

int main(int argc, char *argv[])
{
    /*
     * command line input should be on the form of:
     * int operator (+ or -) int
     * examples:
     * 100 + 20
	 * 100 - 001
	 * 2234 - 1999
	 * 60 - 07
     */
    if (argc != 4) {
        fprintf(stderr, "Expected 3 arguments, got %d\n", argc - 1);
        return 1;
    }

	LinkedList *a = parse(argv[1]);
    LinkedList *b = parse(argv[3]);
	LinkedList *res;

	if(*argv[2] == '+') {
		res = add(a, b);
	} else if(*argv[2] == '-') {
		if(strlen(argv[1]) == strlen(argv[3])) {
			res = sub(a,b);
		} else {
			fprintf(stderr, "Both numbers must be of equal length. Example: 1000 - 0001.\n");
		}
	} else {
		fprintf(stderr, "Expected - or + as a parameter.\n");
	}

    print(res);    
}