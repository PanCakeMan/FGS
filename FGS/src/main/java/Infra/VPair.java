package Infra;

import java.util.HashSet;

public class VPair {

    public XVariable X;
    public double f;
    public double delta;
    public VPair ( XVariable X, double f, double delta) {
        this.X = X;
        this.f = f;
        this.delta = delta;
    }


}
