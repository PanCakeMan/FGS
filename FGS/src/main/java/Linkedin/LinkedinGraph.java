package Linkedin;//package Infra;

import Infra.DataGraphBase;
import Infra.DataNode;
import Infra.RelationshipEdge;
import org.apache.jena.base.Sys;
import org.apache.jena.tdb.store.Hash;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class LinkedinGraph extends DataGraphBase {



    public HashMap<Integer, String> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public HashMap<Integer, String> getEdgeLabelMap() {
        return edgeLabelMap;
    }

    public HashMap<Integer, String> nodeLabelMap;
    public HashMap<Integer, String> edgeLabelMap;

    public LinkedinGraph () {
        super();
        nodeLabelMap = new HashMap<>();
        edgeLabelMap = new HashMap<>();
    }

    public  void loadLinkedGraph (String filePath) throws IOException {

        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        File file1 = new File(filePath);
        BufferedReader br1 = new BufferedReader(new FileReader(file1));
        HashSet<String> majors = new HashSet<>();


        int x = 0;
        String st;

        while ((st = br.readLine()) != null) {

            String[] data = st.split("\t");
            int nodeID =data[1].hashCode();
            String name = data[1];

            if (!nodeMap.containsKey(nodeID)) {
                DataNode node = new DataNode(name);
                node.types.add("User");
                nodeMap.put(nodeID,node);
            }


        }
        br.close();
        addAllVertex();
        if (!edgeLabelMap.containsKey("recommend".hashCode())) {
            edgeLabelMap.put("recommend".hashCode(),"recommend");
        }

        while ((st = br1.readLine()) != null) {


            String[] data = st.split("\t");
            int nodeID = data[1].hashCode();
            String name = data[1];

            DataNode curS = nodeMap.get(name.hashCode());

            // System.out.println(data[data.length-2]);
            String edges[] = data[data.length - 2].trim().split(":");
            if (edges.length == 1) {
                continue;
            }

            if (edges[1].length() > 2) {
                String edgesList = edges[1].substring(1, edges[1].length() - 1);
//                System.out.println(edgesList);
                String neighbors[] = edgesList.split(",");
                for (String nb : neighbors) {
                    if (nodeMap.containsKey(nb.hashCode())) {
                        dataGraph.addEdge(curS, nodeMap.get(nb.hashCode()), new RelationshipEdge("recommend"));
                    }
                }

                for (int i = 3; i < data.length; i++) {

                    String currAtt = data[i];
                    String pair = currAtt.trim();
                    if (!pair.contains(":") || !pair.contains("[") || !pair.contains("]")) {
                        continue;
                    }
                    String[] valuePair = pair.split(":");


                    String att = valuePair[0].trim();

                    String value = valuePair[1].trim();


                    if (att.equals("industry")) {
                        HashSet<String> s = new HashSet();
                        String[] ls = value.substring(1, value.length() - 1).trim().split(",");
                        for (String ss : ls) {
//                            System.out.println(ss);
                            if (ss.length() <= 1) {
                                continue;
                            }
                            s.add(ss.replace("\"", ""));
//                            System.out.println(ss.replace("\"", ""));
                        }
                        if (s.size() != 0) {
                            curS.attributes.put("Industry", s);
                        };


                    }

                    if (att.equals("skills")) {
                        HashSet<String> s = new HashSet();
                        String[] ls = value.substring(1, value.length() - 1).trim().split(",");
                        for (String ss : ls) {
//                            System.out.println(ss);
                            if (ss.length() <= 1) {
                                continue;
                            }
                            s.add(ss.replace("\"", ""));
//                            System.out.println(ss.replace("\"", ""));
                        }
                        if (s.size() != 0) {
                            curS.attributes.put("Skill", s);
                        };


                    }

                    if (att.equals("major")) {
                        HashSet<String> s = new HashSet();
                        if(value.length() > 1){
                            String[] ls = value.substring(1, value.length() - 1).trim().split(",");

                            for (String ss : ls) {
//                                System.out.println(ss);
                                if (ss.length() <= 1) {
                                    continue;
                                }
                                s.add(ss.replace("\"", ""));
                                majors.add(ss.replace("\"", ""));
                            }
                            if (s.size() != 0) {
                                curS.attributes.put("Major", s);
                            }
                        }

                    }

                    if (att.equals("degree")) {
                        HashSet<String> s = new HashSet();
                        if(value.length() > 1){
                        String[] ls = value.substring(1, value.length() - 1).trim().split(",");

                            for (String ss : ls) {
//                                System.out.println(ss);
                                if (ss.length() <= 1) {
                                    continue;
                                }
                                s.add(ss.replace("\"", ""));
//                                System.out.println(ss.replace("\"", ""));
                            }
                            if (s.size() != 0) {
                                curS.attributes.put("Degree", s);
                            }
                        }

                    }

                    if (att.equals("yearsExperience")) {
                        HashSet<String> s = new HashSet();
                        String[] ls = value.substring(1, value.length() - 1).trim().split(",");
                        for (String ss : ls) {
//                            System.out.println(ss);
                            if (ss.length() <= 1) {
                                continue;
                            }
                            s.add(ss.replace("\"", ""));
//                            System.out.println(ss.replace("\"", ""));
                        }
                        if (s.size() != 0) {
                            curS.attributes.put("Year", s);
                        }
                    }

                    if (att.equals("numberOfExperiences")) {
                        HashSet<String> s = new HashSet();
                        String[] ls = value.substring(1, value.length() - 1).trim().split(",");
                        for (String ss : ls) {
//                            System.out.println(ss);
                            if (ss.length() <= 1) {
                                continue;
                            }
                            s.add(ss.replace("\"", ""));
//                            System.out.println(ss.replace("\"", ""));
                        }
                        if (s.size() != 0) {
                            curS.attributes.put("NOWork", s);
                        }
                    }

                    if (att.equals("gender")) {
                        HashSet<String> s = new HashSet();
                        String[] ls = value.substring(1, value.length() - 1).trim().split(",");
                        for (String ss : ls) {
//                            System.out.println(ss);
                            if (ss.length() <= 1) {
                                continue;
                            }
                           ss = ss.replace("[", "");
                           ss = ss.replace("]", "");
                           s.add(ss);
//                            System.out.println(ss.replace("\"", ""));
                        }
                        if (s.size() != 0) {
                            curS.attributes.put("Gender", s);

                        }

                    }

                }


            }


        }

        for (DataNode node : nodeMap.values()) {
            String type = (String) node.types.toArray()[0];
            if (!nodeLabelMap.containsKey(type)) {
                nodeLabelMap.put(type.hashCode(), type);
            }
        }
        System.out.println("Major Size: " + majors.size());
        System.out.println("Node Size: " + dataGraph.vertexSet().size());
        System.out.println("Edge Size: " + dataGraph.edgeSet().size());

    }

    public static void main(String args[]) throws IOException {

        LinkedinGraph graph  = new LinkedinGraph();
        graph.loadLinkedGraph("C:\\Users\\Nick\\Downloads\\updated_nodes_with_gender.txt");

    }
}


