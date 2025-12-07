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
    int s1 = "Hello World!";
    int s2 = "Welcome!";
    int s3 = obuf;
    olen = 6;    
    obuf[0] = 'H';
    obuf[1] = 'e';
    obuf[2] = 'l';
    obuf[3] = 'l';
    obuf[4] = 'o';
    obuf[5] = '!';
    _out();
}
