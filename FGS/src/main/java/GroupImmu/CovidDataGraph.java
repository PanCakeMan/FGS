package GroupImmu;

import com.opencsv.CSVReader;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

import Infra.DataGraphBase;
import Infra.DataNode;
import Infra.RelationshipEdge;
import org.apache.jena.rdf.model.*;

import org.json.*;

import javax.xml.crypto.Data;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CovidDataGraph extends DataGraphBase {

    public HashMap<Integer, String> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public HashMap<Integer, String> getEdgeLabelMap() {
        return edgeLabelMap;
    }

    public HashMap<Integer, String> nodeLabelMap;
    public HashMap<Integer, String> edgeLabelMap;

    public CovidDataGraph(String dataGraphFilePath) throws IOException {
        super();
//        nodeLabelMap = new HashMap<>();
//        edgeLabelMap = new HashMap<>();
        loadNodeMap(dataGraphFilePath);
        loadGraph(dataGraphFilePath);
    }


    public static <String, Integer> void printMap(Map<String, Integer> map) {
        int t = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey()
                    + " Value : " + entry.getValue());
            t += (int) entry.getValue();
        }
        System.out.println(t);

    }


    private void loadNodeMap(String csvFile) throws IOException {

        try (Reader reader = Files.newBufferedReader(Path.of(csvFile))) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    if (line[12].trim().length() == 0 || line[12].trim().charAt(0) != 'P') {
                        continue;
                    }
                    System.out.println(line[12]);

                    if (line[0].length() == 1 || line[0].length() == 0) {
                        continue;
                    }

                    int nodeId = (int) Double.parseDouble(line[0]);
                    DataNode node = new DataNode(line[0]);

                    if (line[4].trim().length() == 0 || line[4].trim().length() > 3) {
                        continue;
                    }
//                    if (line[5].trim().length() == 0) {
//                        continue;
//                    }
//                    if (line[6].trim().length() == 0) {
//                        continue;
//                    }
//                    if (line[12].trim().length() == 0) {
//                        continue;
//                    }


                    HashSet<String> values = new HashSet<>();
                    values.add(line[4].trim());
                    node.attributes.put("age", values);
                    HashSet<String> values1 = new HashSet<>();
                    values1.add(line[5].trim());
                    node.attributes.put("gender", values1);

                    HashSet<String> values2 = new HashSet<>();
                    values2.add(line[6].trim());
                    node.attributes.put("dCity", values2);
                    node.types.add("Person");
                    nodeMap.put(nodeId, node);


                }

            }
        }

        System.out.println("Done Loading DBPedia Node Map!!!");
        System.out.println("DBPedia NodesMap Size: " + nodeMap.size());


    }

    private void loadGraph(String csvFile) throws IOException {


        try (Reader reader = Files.newBufferedReader(Path.of(csvFile))) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                while ((line = csvReader.readNext()) != null) {

                    if (line[12].trim().length() == 0 || line[12].trim().charAt(0) != 'P') {
                        continue;
                    }


                    if (line[0].length() == 1 || line[0].length() == 0) {
                        continue;
                    }

                    int nodeIdT = (int) Double.parseDouble(line[0]);

                    for (String s : line[12].split(",")) {
                        s = s.replace("P","").trim();

                        int nodeIdS = Integer.parseInt(s);

                        if (!nodeMap.containsKey(nodeIdS) || !nodeMap.containsKey(nodeIdT)) {
                            continue;
                        }

                        DataNode nodeT = nodeMap.get(nodeIdT);
                        DataNode nodeS = nodeMap.get(nodeIdS);
                        dataGraph.addVertex(nodeS);
                        dataGraph.addVertex(nodeT);
                        dataGraph.addEdge(nodeS, nodeT, new RelationshipEdge("contact"));

                    }








                }

            }
        }



        System.out.println("Number of Edges: " + dataGraph.edgeSet().size());
        System.out.println("Number of Nodes: " + dataGraph.vertexSet().size());


    }

    private int getDepth() throws IOException {

        int max = Integer.MIN_VALUE;
        HashSet<DataNode> dup = new HashSet<>();
        for (DataNode n : dataGraph.vertexSet()) {

            if (dataGraph.outDegreeOf(n) == 0) {
//                System.out.println("Continue");
                continue;
            }

            Queue<DataNode> queue = new LinkedList<>();
            queue.add(n);
//
            int level = 0;
            while (!queue.isEmpty()) {
                int s = queue.size();

                for (int i = 0; i < s; i++) {
                    DataNode node = queue.poll();
//                    System.out.println( dataGraph.outgoingEdgesOf(node).size()+ "--->");
                    for (RelationshipEdge r : dataGraph.outgoingEdgesOf(node)) {
                        if (!dup.contains(dataGraph.getEdgeTarget(r))) {
                            queue.add(dataGraph.getEdgeTarget(r));
                            dup.add(dataGraph.getEdgeTarget(r));
                        }

                    }
                }
                level++;
            }


            if (level > max) {
                max = level;
            }

        }

        return max;
    }

    public static void main(String args[]) throws IOException {
        CovidDataGraph dataGraph = new CovidDataGraph("patients_data.csv");
        System.out.println(dataGraph.nodeMap.size());

        System.out.println(dataGraph.getDepth());
        int m = 0;
        int f = 0;
        for (DataNode n : dataGraph.nodeMap.values()) {
//          if (n.attributes.get("gender").toArray()[0].equals("M")) {
//              m++;
//          } else if (n.attributes.get("gender").toArray()[0].equals("F")) {
//              f++;
//          }
            if (((String) n.attributes.get("age").toArray()[0]).length() > 2) {
                continue;
            }

            if (Integer.parseInt((String) n.attributes.get("age").toArray()[0]) <= 50) {
                m++;
            } else if (Integer.parseInt((String) n.attributes.get("age").toArray()[0]) > 50) {
                f++;
            }

        }
        System.out.println(m + " " + f);

    }

}


