#include "io.c"
#include "atoi.c"

void main() {
    int i;
    int n;
    int buff[80];

    puts("Enter a number:");
    gets(buff);
    n = atoi(buff);
    putn(n);
    for(i = 0; i < n; i = i + 1) {
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
