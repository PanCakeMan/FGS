package Infra;

import java.util.HashSet;

public class Range {
    String type;
    public double low;
    public double high;
    HashSet<String> domain;
    public Range(double low, double high,String type) {
        this.low= low;
        this.high = high;
        this.type = type;
        this.domain = null;
    }
    public Range(HashSet<String> domain, String type) {
        this.low= 0;
        this.high = 0;
        this.type = type;
        this.domain = domain;
    }
}
