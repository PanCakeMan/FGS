package Infra;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.*;

public class DataGraphBase {

    protected Graph<DataNode, RelationshipEdge> dataGraph;
    protected HashMap<Integer,DataNode> nodeMap;
    protected HashMap<Integer,DataNode> literalNodeMap;

    public DataGraphBase( ) {

        nodeMap  = new HashMap<>();
        literalNodeMap = new HashMap<>();
        dataGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);

    }

    protected void addAllVertex() {

        Iterator nodeMapIterator = nodeMap.entrySet().iterator();

        while (nodeMapIterator.hasNext()) {
            Map.Entry currentNodeEntry = (Map.Entry) nodeMapIterator.next();
            dataGraph.addVertex((DataNode) currentNodeEntry.getValue());
        }


    }

    public HashMap<Integer, DataNode> getNodeMap() {
        return nodeMap;
    }
    public HashMap<Integer, DataNode> getLiteralNodeMap() { return literalNodeMap; }
    public Graph<DataNode, RelationshipEdge> getDataGraph() { return dataGraph; }

}
