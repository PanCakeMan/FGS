package GroupImmu;

import java.util.ArrayList;

public class Node implements Comparable<Node>
{
    public int nodeId;
    public ArrayList<Node> neighbors = new ArrayList<Node>();
    private boolean b_isActive = false;
    private double threshold = 0.0;

    public Node(int nodeId, double threshold)
    {
        this.nodeId = nodeId;
        this.threshold = threshold;
    }

    public void setThreshold (double d) {
        this.threshold = d;
    }

    public int neighbors_num()
    {
        return this.neighbors.size();
    }

    public void setActive(boolean isActive)
    {
        this.b_isActive = isActive;
    }

    public boolean isActive(){
        return this.b_isActive;
    }
    public double getThreshold(){
        return this.threshold;
    }
    // Sort nodes by (out)degree
    public int compareTo(Node anotherNode)
    {
        if (this.neighbors != null && anotherNode.neighbors != null)
        {
            // reverse order
            return anotherNode.neighbors_num() - this.neighbors_num();
            // positive order
            // return this.neighbors_num()-anotherNode.neighbors_num();
        }
        return 0;
    }
}