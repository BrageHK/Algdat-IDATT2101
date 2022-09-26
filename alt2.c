void shellsort(int n, int t[n]) {
    int s = n/2;
    while(s>0) {
        for(int i = s; i<n; ++i) {
            int j = i, flytt = t[i];
            while(j>=s && flytt<t[j-s]) {
                t[j] = t[j - s];
                j -= s;
            }
            t[j] = flytt;
        }
        s = (s == 2) ? 1 : s / 2.2;
    }
}