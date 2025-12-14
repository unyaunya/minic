/*
 * Variables for IN/OUT macro
 */
int ilen;
int ibuf[256];
int olen;
int obuf[256];

/**
 * main routine
 */
void main() {
    int p[5];
    int s1 = "Hello World!";
    int s2 = "Welcome!";
    int s3 = obuf;
    olen = 6;
    p = obuf;
    obuf[0] = 'H';
    obuf[1] = 'e';
    obuf[2] = 'l';
    obuf[3] = 'l';
    obuf[4] = 'o';
    obuf[5] = '!';
    obuf[6] = '\0';
    olen = strlen(obuf);
    _out();
}

int strlen(int s) {
    int n = 0;
    while(s[n] != '\0') {
        n = n + 1;
    }
    return n;
}
