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

int strlen(int* s) {
    int n = 0;
    while(s[n] != '\0') {
        n = n + 1;
    }
    return n;
}

int* strcpy(int* dst, int* src) {
    int n = 0;
    while(src[n] != '\0') {
        *(dst+n) = *(src+n);
        n = n + 1;
    }
    dst[n] = '\0';
    return dst + n;
}

void puts(int* s) {
    olen = strcpy(obuf, s) - obuf;
    _out();
}

void putn(int n) {
    olen = sputn(obuf, n) - obuf;
    _out();
}

int* sputn(int* dst, int n) {
    int buff[7];
    int idx;
    int sgn;
    int ch;
    int i;
    int d;

    if (n == -32768) {
        return strcpy(dst, "-32768");
    } else if (n == 0) {
        return strcpy(dst, "0");
    } else {
        if(n < 0) {
            sgn = 1;
            *dst = '-';
            dst = dst + 1;
            n = -n;
        } else {
            sgn = 0;
        }
        d = 10000;
        int lz = 1;
        for(i = 0; i < 5; i=i+1) {
            ch = div(n, d);
            if (ch != 0) {
                lz = 0;
            }
            if (lz == 0) {
                *dst = '0' + ch;
                dst = dst + 1;
            }
            n = n - mul(d, ch);
            d = div(d, 10);
        }
        return dst;
    }
}

int div(int a, int b) {
    int n = 0;
    int sgn = 1;
    if (a < 0) {
        a = -a;
        sgn = -sgn;
    }
    if (b < 0) {
        b = -b;
        sgn = -sgn;
    }
    while(a >= b) {
        n = n + 1;
        a = a - b;
    }
    if (sgn < 0) {
        n = -n;
    }
    return n;
}

int mul(int a, int b) {
    int tmp;
    int n;
    int sgn = 1;
    if (a < 0) {
        a = -a;
        sgn = -sgn;
    }
    if (b < 0) {
        b = -b;
        sgn = -sgn;
    }
    if(a < b) {
        tmp = a;
        a = b;
        b = tmp;
    }
    n = 0;
    while(b > 0) {
        n = n + a;
        b = b - 1;
    }
    if (sgn < 0) {
        n = -n;
    }
    return n;
}