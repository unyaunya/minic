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