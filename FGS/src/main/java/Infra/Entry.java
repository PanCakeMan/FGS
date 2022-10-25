package Infra;

import java.util.HashSet;

public class Entry {


 public HashSet<DataNode> set;
 public double f;
 public double delta;

 public Entry(HashSet set, double f, double delta) {
     this.set = set;
     this.f = f;
     this.delta = delta;
 }

}
