#include "io.c"

/**
 * main routine
 */
void main() {
    putn(12345);
    int* s[3];
    s[0] = "Hello World!";
    s[1] = "Welcome!";
    s[2] = "Jumbo!";
    int i;
    int* q = &i;
    putn((int)q);
    q = s[0];
    putn((int)q);
    q = s[1];
    putn((int)q);
    q = s[2];
    putn((int)q);
    //int* p = strcpy(obuf, "&i=");
    //p = sputn(obuf, &i);
    for(i = 0; i < 3; i=i+1) {
        puts(s[i]);
        putn(strlen(s[i]));
    }
}