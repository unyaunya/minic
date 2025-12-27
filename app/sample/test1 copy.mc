/*
 * Variables for IN/OUT macro
 */
int ilen;
int ibuf[256];
int olen;
int obuf[256];

int d[10];
int rslt;

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
