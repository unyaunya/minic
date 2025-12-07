/*
 * Variables for IN/OUT macro
 */
int ilen;
int ibuf[256];
int olen;
int obuf[256];

int d[10];
int rslt;

//void main() {
//    rslt = fib(1);
//}

/**
 * main routine
 */
void main() {
    int i;
    //int prev;

    d[0] = 1;
    i = 0;    
    while(i <= 10) {
        //prev = d[i-1];
        d[i] = fib(i);
        i = i + 1;
        //olen = 2;
        //obuf[0] = 64+i;
        //obuf[1] = 32+i;
        //_out();
    }
}

/**
 * sub routine
 */
//int plus(int a, int b) {
//    return a + b;
//}

int fib(int n) {
    if (n < 0) {
        return -1;
    }
    if (n == 0) {
        return 0;
    } else if (n == 1) {
        return 1;
    } else {
        return fib(n-1) + fib(n-2);
    }
}

//void put(int n) {
//}