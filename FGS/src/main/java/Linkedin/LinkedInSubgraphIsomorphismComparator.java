//package Linkedin;
//
//import org.apache.commons.lang3.tuple.Pair;
//import org.jgrapht.Graph;
//import org.jgrapht.GraphMapping;
//import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
//import org.jgrapht.graph.DefaultDirectedGraph;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.util.*;
//
//public class LinkedInSubgraphIsomorphismComparator {
//
//    private DefaultDirectedGraph<LinkedInNode, LinkedInEdge> dataGraph;
//    private HashMap<String, LinkedInNode> nodeMap;
//    private Comparator<LinkedInNode> myNodeComparator;
//    private Comparator<LinkedInEdge> myEdgeComparator;
//
//
//    public LinkedInSubgraphIsomorphismComparator(LinkedInGraphBase graph) {
//        this.dataGraph = graph.getDataGraph();
//        this.nodeMap = graph.getNodeMap();
//
//        myEdgeComparator = new Comparator<LinkedInEdge>() {
//            @Override
//            public int compare(LinkedInEdge e1, LinkedInEdge e2) {
//
//                if (e1.getSource().equals(e2.getSource()) && e1.getTarget().equals(e2.getTarget())) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//
//            }
//
//        };
//
//        myNodeComparator = new Comparator<LinkedInNode>() {
//            @Override
//            public int compare(LinkedInNode n1, LinkedInNode n2) {
//
//                boolean flag = true;
//
//                if (n1.getNodeIndex() == n2.getNodeIndex() && n1.getNodeID().equals(n2.getNodeID()) &&
//                        hashIntersection(n1.getAttributes().values(), n2.getAttributes().values())) {
//                    return 0;
//                } else {
//
//                    return 1;
//
//                }
//
//            }
//
//        };
//
//    }
//
//    private boolean hashIntersection(Collection<ArrayList> types1, Collection<ArrayList> types2) {
//        for (ArrayList type : types1) {
//            if (!types2.contains(type)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public void execute(LinkedInGraphBase pattern) {
//
//        VF2SubgraphIsomorphismInspector inspector = new
//                VF2SubgraphIsomorphismInspector<>(this.dataGraph, pattern.getDataGraph(),
//                myNodeComparator, myEdgeComparator, false);
//
//        if (inspector.isomorphismExists()) {
//
//            Iterator<GraphMapping<LinkedInNode, LinkedInEdge>> iterator = inspector.getMappings();
//            ArrayList<LinkedInNode> patternTypes = new ArrayList<>();
//
//            for (Object node : pattern.getNodeMap().values()) {
//                patternTypes.add((LinkedInNode) node);
//            }
//
//            ArrayList<LinkedInNode> currentMatch = new ArrayList<>();
//            int count = 0;
//            int count1 = 0;
//            while (iterator.hasNext()) {
//
//                org.jgrapht.GraphMapping<LinkedInNode, LinkedInEdge> mappings = iterator.next();
//
//                for (Object node : pattern.getNodeMap().values()) {
//
//                    LinkedInNode currentMatchedNode = mappings.getVertexCorrespondence((LinkedInNode) node, false);
//
//                    if (currentMatchedNode != null) {
//                        currentMatch.add(currentMatchedNode);
//                    }
//                }
//            }
//
//            int total = 0;
//            int htotal = 0;
//
//            for (LinkedInNode node : currentMatch) {
//                total++;
//
//                if(node.getAttributes().keySet().size() > 0) {
//                    htotal++;
//                }
//            }
//
//            System.out.println(count + " " + count1 + " " +
//                    "distribution");
//            System.out.println(total + " " + htotal + " " +
//                    "distribution");
//
//        } else {
//
//            System.out.println("No Matches for the query!");
//        }
//    }
//
//    public boolean isValidNode(LinkedInNode node, String attributeName, String[] attributeValue) {
//        if (node.getAttributes().keySet().contains(attributeName)) {
//            for (String possibleAttr : attributeValue) {
//                if (node.getAttribute(attributeName).contains(possibleAttr)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//}
//
//    /**
//     * find nodes in the graph with the specified attribute value for the specified attribute
//     * @param graph- graph structure to find nodes
//     * @param attributeName- the name of the attribute that is being found
//     * @param attributeValue- the value that attribute takes
//     * @return- list of nodes that have the common attribute
//     */
//    public ArrayList<LinkedInNode> getNodesWithAttr(LinkedInGraphBase graph, String attributeName, String[] attributeValue) {
//        ArrayList<LinkedInNode> validNodes = new ArrayList<>();
//        for (LinkedInNode node: graph.getNodeMap().values()) {
//            if (isValidNode(node, attributeName, attributeValue)) {
//                validNodes.add(node);
//            }
//        }
//        return validNodes;
//    }
//
//    /**
//     * count the number of occurences of a list of strings
//     * @param list- the list of strings to be counted
//     * @return- hashmap of the values and their occurences
//     */
//    public HashMap<String, Integer> counter(ArrayList<String> list) {
//        HashMap<String, Integer> counter = new HashMap<>();
//        for (String id: list) {
//            Integer j = counter.get(id);
//            counter.put(id, (j == null) ? 1 : j + 1);
//        }
//        return counter;
//    }
//
//    /**
//     * for the specified attribute and its value, find the distribution of the protected class (gender in this case)
//     * where the source nodes that have the attribute value and they share a viewed node. In other words,
//     * two nodes with the specified value viewed the same node, and the gender of the new node is found
//     * @param graph- graph structure to find nodes
//     * @param attributeName- the name of the attribute that is being found
//     * @param attributeValue- the value that attribute takes
//     */
//    public void getDistributionUserWithPhD(LinkedInGraphBase graph, String attributeName, String[] attributeValue) {
//
//        // get nodes that have the specified attribute and value
//        ArrayList<LinkedInNode> validNodes = getNodesWithAttr(graph, attributeName, attributeValue);
//
//        ArrayList<String> edgeList = new ArrayList<>();
//        ArrayList<String> gender = new ArrayList<>();
//
//        // for each of the nodes with the value, get their id and the target node
//        for (LinkedInNode node: validNodes) {
//            String id = node.getNodeID();
//            ArrayList<String> targets = graph.getAllNodeNeighbors(id);
//            edgeList.addAll(targets);
//            }
//
//        // get a count of how many times each node occurs in the targets and remove them if they
//        // do not occur multiple times
//        HashMap<String, Integer> idCounter = counter(edgeList);
//        idCounter.values().removeIf(val -> 1 >= val);
//
//        // get the gender of all remaining nodes
//        for (String id: idCounter.keySet()) {
//            LinkedInNode node = graph.getNodeMap().get(id);
//            gender.add(node.getAttribute("gender").toString());
//        }
//        HashMap<String, Integer> genderCounter = counter(gender);
//        System.out.println("Male: " + genderCounter.get("[male]") + "\nFemale: " + genderCounter.get("[female]"));
//    }
//
//    public void getDistributionBothValue(LinkedInGraphBase graph, String attributeName, String[] attributeValue) {
//
//        ArrayList<LinkedInNode> validNodes = getNodesWithSubAttr(graph, attributeName, attributeValue);
//
//        ArrayList<String> gender = new ArrayList<>();
//
////        try {
////            FileWriter writer = new FileWriter("phd_nodes.txt");
//
//            // for each of the nodes with the value, get their id and the target node
//            for (LinkedInNode node : validNodes) {
//
//                ArrayList<String> targets = graph.getAllNodeNeighbors(node.getNodeID());
//
//                for (String target : targets) {
//                    LinkedInNode targetNode = graph.getNodeMap().get(target);
//                    if (isValidNodeSubAttr(targetNode, attributeName, attributeValue)) {
//                        if (node.getAttributes().keySet().contains("gender")) {
//                            gender.add(node.getAttribute("gender").toString());
////                            writer.write(node.getNodeID() + "\t" + node.getAttributes().toString() + "\n");
//                    }
//                        break;
//                    }
//                }
//            }
////            writer.close();
////        }
////        catch (IOException e) {
////            e.printStackTrace();
////        }
//
//        displayGenderDistribution(gender);
//
//    }
//
//    public void displayGenderDistribution(ArrayList<String> gender) {
//        HashMap<String, Integer> genderCounter = counter(gender);
//        double male = genderCounter.get("[male]");
//        double female = genderCounter.get("[female]");
//        double dist = male / (male + female) * 100;
//        System.out.println("Male: " + male + "\nFemale: " + female);
//        System.out.println("Distribution: " + dist + "%");
//    }
//
//    public static boolean isValidNodeSubAttr(LinkedInNode node, String attributeName, String[] attributeValue) {
//        if (node.getAttributes().keySet().contains(attributeName)) {
//            int counter = 0;
//            for (String possibleAttrValue : attributeValue) {
//                for (String attrValues : (ArrayList<String>) node.getAttribute(attributeName)) {
//                    if (attrValues.matches(".*(?i)"+possibleAttrValue+".*")) {
//                        counter++;
//                        break;
//                    }
//                }
//            }
//            if (counter == attributeValue.length) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public ArrayList<LinkedInNode> getNodesWithSubAttr(LinkedInGraphBase graph, String attributeName, String[] attributeValue) {
//
//        ArrayList<LinkedInNode> validNodes = new ArrayList<>();
//        for (LinkedInNode node: graph.getNodeMap().values()) {
//            if (isValidNodeSubAttr(node, attributeName, attributeValue)) {
//                validNodes.add(node);
//            }
//        }
//        return validNodes;
//    }
//
//    public void getDistributionSubValue(LinkedInGraphBase graph, String attributeName, String[] attributeValue) {
//
//        ArrayList<LinkedInNode> validNodes = getNodesWithSubAttr(graph, attributeName, attributeValue);
//
//        ArrayList<List<LinkedInNode>> edgeList = new ArrayList<>();
//        ArrayList<String> gender = new ArrayList<>();
//
//            // for each of the nodes with the value, get their id and the target node
//            for (LinkedInNode node : validNodes) {
//                if (node.getAttributes().keySet().contains("gender")) {
//                    gender.add(node.getAttribute("gender").toString());
//                }
//            }
//
//        displayGenderDistribution(gender);
//
//    }
//
//    public void getDistributionCoViewedSubValue(LinkedInGraphBase graph, String attributeName, String[] attributeValue) {
//
//        ArrayList<String> gender = new ArrayList<>();
//
//        for (LinkedInNode node: graph.getNodeMap().values()) {
//
//            ArrayList<String> targets = graph.getAllNodeNeighbors(node.getNodeID());
//
//            for (String target : targets) {
//                LinkedInNode targetNode = graph.getNodeMap().get(target);
//                if (isValidNodeSubAttr(targetNode, attributeName, attributeValue)) {
//                    if (node.getAttributes().keySet().contains("gender")) {
//                        gender.add(node.getAttribute("gender").toString());
////                            writer.write(node.getNodeID() + "\t" + node.getAttributes().toString() + "\n");
//                    }
//                    break;
//                }
//            }
//        }
//
//        displayGenderDistribution(gender);
//    }
//
//    public boolean isValidQueriedNode(LinkedInNode node, HashMap<String, ArrayList<String>> attributes) {
//        int counter = 0;
//        for (Map.Entry<String, ArrayList<String>> entry: attributes.entrySet()) {
//            if (isValidNodeSubAttr(node, entry.getKey(), entry.getValue().toArray(new String[0]))) {
//                counter += 1;
//            }
//        }
//        if (counter == attributes.size()) {
//            return true;
//        }
//        return false;
//    }
//
//    public ArrayList<LinkedInNode> getQueriedNodes(LinkedInGraphBase graph, HashMap<String, ArrayList<String>> attributes) {
//        ArrayList<LinkedInNode> validNodes = new ArrayList<>();
//        for (LinkedInNode node: graph.getNodeMap().values()) {
//            if (isValidQueriedNode(node, attributes)) {
//                validNodes.add(node);
//            }
//        }
//        return validNodes;
//    }
//
//    public void querySameAttributes(LinkedInGraphBase graph, HashMap<String, ArrayList<String>> attributes) {
//
//        ArrayList<LinkedInNode> validNodes = getQueriedNodes(graph, attributes);
//
//        ArrayList<String> gender = new ArrayList<>();
//
//        for (LinkedInNode node: validNodes) {
//
//            ArrayList<String> targets = graph.getAllNodeNeighbors(node.getNodeID());
//
//            for (String target : targets) {
//                LinkedInNode targetNode = graph.getNodeMap().get(target);
//
//                if (isValidQueriedNode(targetNode, attributes)) {
//                    if (node.getAttributes().keySet().contains("gender")) {
//                        gender.add(node.getAttribute("gender").toString());
////                            writer.write(node.getNodeID() + "\t" + node.getAttributes().toString() + "\n");
//                    }
//                    break;
//                }
//            }
//        }
//
//        displayGenderDistribution(gender);
//    }
//
//    public void queryDistinctAttributesCoviewedDist(LinkedInGraphBase graph, HashMap<String, ArrayList<String>> nodeAttributes, HashMap<String, ArrayList<String>> coviewedAttributes) {
//
//        ArrayList<LinkedInNode> validNodes = getQueriedNodes(graph, nodeAttributes);
//
//        ArrayList<String> gender = new ArrayList<>();
//
//        for (LinkedInNode node: validNodes) {
//
//            ArrayList<String> targets = graph.getAllNodeNeighbors(node.getNodeID());
//
//            for (String target : targets) {
//                LinkedInNode targetNode = graph.getNodeMap().get(target);
//
//                if (isValidQueriedNode(targetNode, coviewedAttributes)) {
//                    if (targetNode.getAttributes().keySet().contains("gender")) {
//                        gender.add(targetNode.getAttribute("gender").toString());
////                            writer.write(node.getNodeID() + "\t" + node.getAttributes().toString() + "\n");
//                    }
//                }
//            }
//        }
//
//        displayGenderDistribution(gender);
//    }
//
//    public void queryDistinctAttributesNodeDistMultiViews(LinkedInGraphBase graph, HashMap<String, ArrayList<String>> nodeAttributes, HashMap<String, ArrayList<String>> coviewedAttributes, int necessaryViews) {
//
//        ArrayList<LinkedInNode> validNodes = getQueriedNodes(graph, nodeAttributes);
//
//        ArrayList<String> gender = new ArrayList<>();
//
//        for (LinkedInNode node: validNodes) {
//
//            ArrayList<String> targets = graph.getAllNodeNeighbors(node.getNodeID());
//
//            int counter = 0;
//
//            for (String target : targets) {
//                LinkedInNode targetNode = graph.getNodeMap().get(target);
//
//                if (isValidQueriedNode(targetNode, coviewedAttributes)) {
//                    counter +=1;
//                    if (counter == necessaryViews) {
//                        if (node.getAttributes().keySet().contains("gender")) {
//                            if (targetNode.getAttribute("gender") != null) {
//                                gender.add(targetNode.getAttribute("gender").toString());
////                            writer.write(node.getNodeID() + "\t" + node.getAttributes().toString() + "\n");
//                            }
//
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//
//        displayGenderDistribution(gender);
//    }
//
//        public void genderDist(LinkedInGraphBase graph) {
//        ArrayList<String> gender = new ArrayList<>();
//
//        for (LinkedInNode node: graph.getNodeMap().values()) {
//            if (node.getAttributes().keySet().contains("gender")) {
//                gender.add(node.getAttribute("gender").toString());
//            }
//        }
//
//        displayGenderDistribution(gender);
//    }
//
//
//    public static void main(String args[]) {
//        LinkedInGraphBase graph = LinkedInGraphBase.execute("\\Users\\Nick\\Downloads\\updated_nodes_with_gender.txt",200000);
//        LinkedInSubgraphIsomorphismComparator checker = new LinkedInSubgraphIsomorphismComparator(graph);
//
//        HashMap<String, ArrayList<String>> nodeAttr = new HashMap<>();
//        HashMap<String, ArrayList<String>> coAttr = new HashMap<>();
//
////        ArrayList<String> nodeTitle = new ArrayList<>();
////        ArrayList<String> coTitle = new ArrayList<>();
//
////        nodeTitle.add("Senior");
////        nodeTitle.add("Media");
////        nodeAttr.put("title", nodeTitle);
//
////        coTitle.add("Senior");
////        coTitle.add("Media");
////        coAttr.put("title", coTitle);
//
//        ArrayList<String> nodeSkills = new ArrayList<>();
//        ArrayList<String> coSkills = new ArrayList<>();
//
//        nodeSkills.add("Business");
//        nodeAttr.put("skills", nodeSkills);
//
//        coSkills.add("Writing");
//        coSkills.add("Business");
//        coAttr.put("skills", coSkills);
//
//
//        checker.queryDistinctAttributesNodeDistMultiViews(graph, nodeAttr, coAttr, 2);
//
////        checker.queryDistinctAttributesCoviewedDist(graph, nodeAttr, coAttr);
//    }
//}
