package GroupImmu;

import Infra.DataNode;
import Infra.RelationshipEdge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LTModel {


    private ArrayList<Node> dimensions;

    public ArrayList<Node> graph;

    public LTModel () throws IOException {

        CovidDataGraph dataGraph = new CovidDataGraph("patients_data.csv");
        HashMap<Integer,Node> map = new HashMap<>();
        graph = new ArrayList<>();
        dimensions = new ArrayList<>();


        for (DataNode node : dataGraph.getDataGraph().vertexSet()) {
            Node newNode = new Node(node.getNodeName().hashCode(),0.0);
//            newNode.setActive(false);
            map.put(node.getNodeName().hashCode(), newNode);

        }

        for (DataNode node : dataGraph.getDataGraph().vertexSet()) {

            int inNum = dataGraph.getDataGraph().incomingEdgesOf(node).size();
            int outNum = dataGraph.getDataGraph().outgoingEdgesOf(node).size();

            Node curNode = map.get(node.getNodeName().hashCode());
            if (inNum == 0 ) {

                curNode.setThreshold(1.0);


            }
            if (dimensions.size()  < 10) {
                dimensions.add(curNode);
            }

            graph.add(curNode);
            Set<RelationshipEdge> oEdges = dataGraph.getDataGraph().outgoingEdgesOf(node);
            if (oEdges.size() == 0) {
                continue;
            }

            for (RelationshipEdge edge : dataGraph.getDataGraph().outgoingEdgesOf(node)) {
                System.out.println("add");
                DataNode t = dataGraph.getDataGraph().getEdgeTarget(edge);
                Node nei = map.get(t.getNodeName().hashCode());
                nei.setThreshold((double)1/oEdges.size());
                curNode.neighbors.add(nei);
            }

        }

    }




    public int beginDiffusionProcess(ArrayList<Node> graph, ArrayList<Integer> activeNodeIds, int lastInfSpread)
    {
        //Mark the active neighbors of each node.
        for(Node nd:graph)
        {
            for(Node n:nd.neighbors)
            {
                if(activeNodeIds.contains(n.nodeId))
                {
                    n.setActive(true);
                }
            }
        }

        //Determine whether each node is activated or not.
        for(Node nd:graph)
        {
            int activeNeighbor_Num=0;
            for(Node n:nd.neighbors)
            {
                if(n.isActive())
                {
                    activeNeighbor_Num++;
                }
            }
            if (activeNeighbor_Num/(nd.neighbors.size()*1.0)>=nd.getThreshold())//如果是带权图，这里要修改
            {
                nd.setActive(true);
                activeNodeIds.add(nd.nodeId);
            }
        }
        //Get the influence spread of the current step.
        int infSpread=0;
        for(Node n:graph)
        {
            if(n.isActive())
            {
                infSpread++;
            }
        }
        //If it converges,stop the diffusion process,else continue the next step.
        if(lastInfSpread==infSpread)
            return infSpread;
        else
            return beginDiffusionProcess(graph,activeNodeIds,infSpread);
    }

    public int GetInfSpread(ArrayList<Node> graph)

    {
        ArrayList<Integer> activeNodeIds=new ArrayList<Integer>();
        for(Node n:this.dimensions)
        {
            activeNodeIds.add(n.nodeId);
        }
        int lastInfSpread=0;
        return beginDiffusionProcess(graph, activeNodeIds,lastInfSpread);
    }

    public static void main (String args[]) throws IOException {
        LTModel lt = new LTModel();
        System.out.println(lt.graph.size());
        System.out.println(lt.dimensions.size());

        System.out.println(lt.GetInfSpread(lt.graph));
    }


}
