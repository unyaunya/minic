
int rslt[16];

void main() {
    int i;
    /*
    for(i = 0; i < 8; i = i + 1) {
        rslt[i] = fib(i);
    }
    while(i < 16) {
        rslt[i] = fib(i);
        i = i + 1;
    }
    */
    for(i = 0; i < 16; i = i + 1) {
        putn(fib(i));
    }
}

int fib(int n) {
    if (n < 0) {
        return -1;
    } else if (n == 0) {
        return 0;
    } else if (n == 1) {
        return 1;
    } else {
        return fib(n-1) + fib(n-2);
    }
}

#include "io.c"