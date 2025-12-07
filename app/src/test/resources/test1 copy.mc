int ilen;
int ibuf[256];
int olen;
int obuf[256];


void main() {
    int d[10];
    int i;
    int prev;

    d[0] = 1;
    i = 1;    
    while(i < 10) {
        prev = d[i-1];
        d[i] = plus(prev, prev);
        i = i + 1;
        olen = 1;
        obuf[0] = 63+i;
        _out();
    }
}

int plus(int a, int b) {
    return a + b;
}