package Infra;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.ArrayList;
import java.util.HashSet;

public class Pair {

    public XVariable X;
    public Graph<DataNode, RelationshipEdge> pattern;
    public HashSet<DataNode> set;
    public Pair ( XVariable X, Graph<DataNode, RelationshipEdge> pattern, HashSet<DataNode> set) {
        this.X = X;
        this.set = set;
        this.pattern = pattern;
    }

}
