package MovieLensGraph;
import java.io.*;
import java.util.*;
import Infra.*;
import Linkedin.LinkedinGraph;
import org.apache.jena.atlas.data.AbstractDataBag;
import org.apache.jena.base.Sys;
import org.apache.jena.tdb.store.Hash;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.isomorphism.VF2AbstractIsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.GraphMapping;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import tkg.AlgoGSPAN;

import javax.xml.crypto.Data;
import java.util.Comparator;

public class VF2Checker {


    private Graph<DataNode, RelationshipEdge> dataGraph;
    private VF2AbstractIsomorphismInspector<DataNode, RelationshipEdge> inspector;
    private Comparator<DataNode> myNodeComparator;
    private Comparator<RelationshipEdge> myEdgeComparator;
    private Comparator<RelationshipEdge> myEdgeComparator2;
    protected HashMap<Integer, DataNode> nodeMap;
    protected AlgoGSPAN algoGSPAN = new AlgoGSPAN();
    public HashMap<Integer, String> nodeLabelMap;
    public HashMap<Integer, String> edgeLabelMap;
    Graph<DataNode, RelationshipEdge> mergedGraph;

    HashMap<String, HashSet<RelationshipEdge>> summaryMap;
    HashSet<DataNode> candidates;

    public VF2Checker(MovieDataGraph linkedinGraph) {

        this.nodeLabelMap = linkedinGraph.getNodeLabelMap();
        this.edgeLabelMap = linkedinGraph.getEdgeLabelMap();
        this.dataGraph = linkedinGraph.getDataGraph();
        this.nodeMap = linkedinGraph.getNodeMap();
        mergedGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);
        candidates = new HashSet<>();
        summaryMap = new HashMap<>();


        myEdgeComparator = new Comparator<RelationshipEdge>() {
            @Override
            public int compare(RelationshipEdge e1, RelationshipEdge e2) {
//                System.out.println(e1.getLabel() + "----" + e2.getLabel());
                if (e1.getLabel().equals(e2.getLabel())) {

                    return 0;
                } else {
                    return 1;
                }

            }

        };

        myEdgeComparator2 = new Comparator<RelationshipEdge>() {
            @Override
            public int compare(RelationshipEdge e1, RelationshipEdge e2) {
//                System.out.println(e1.getLabel() + "----" + e2.getLabel());
                if (e1.getLabel().equals(e2.getLabel())) {

                    return 0;
                } else {
                    return 0;
                }


            }

        };

        myNodeComparator = new Comparator<DataNode>() {
            @Override
            public int compare(DataNode n1, DataNode n2) {
                if (hashIntersection(n1.types, n2.types)) {
//                    System.out.println(n1.types + " " + n2.types);
//                    for (Predicate p : n2.preds) {
//                        if (!staisfyP(p, n1)) {
//                            return 1;
//                        }
//                    }
                    return 0;
                } else {
                    return 1;
                }
            }

        };

    }

    public ArrayList<Graph<DataNode, RelationshipEdge>> getInducedGraph(HashSet<DataNode> solution, int r) {

        ArrayList<Graph<DataNode, RelationshipEdge>> res = new ArrayList<>();

        for (DataNode node : solution) {

            Graph<DataNode, RelationshipEdge> curGraph = generateSubgraph(node, r);
            System.out.println(dataGraph.outgoingEdgesOf(node).size() + "++++++++");
            System.out.println(curGraph.edgeSet().size());
            System.out.println(res.size() + "-----------");
            res.add(curGraph);
        }

        return res;

    }

    public ArrayList<Graph<DataNode, RelationshipEdge>> getInducedGraph1(HashSet<DataNode> solution, int r) {

        ArrayList<Graph<DataNode, RelationshipEdge>> res = new ArrayList<>();
        Graph<DataNode, RelationshipEdge> graph = new DefaultDirectedGraph<>(RelationshipEdge.class);
        for (DataNode node : solution) {

            Graph<DataNode, RelationshipEdge> curGraph = generateSubgraph(node, r);
            Graphs.addGraph(graph, curGraph);
//            System.out.println(dataGraph.outgoingEdgesOf(node).size() + "++++++++");
//            System.out.println(curGraph.edgeSet().size());
            System.out.println(res.size() + "-----------");

        }
        res.add(graph);
        return res;

    }

    public ArrayList<Graph<DataNode, RelationshipEdge>> getInducedGraph2(HashSet<DataNode> solution, int r) {

        ArrayList<Graph<DataNode, RelationshipEdge>> res = new ArrayList<>();
        Graph<DataNode, RelationshipEdge> graph = new DefaultDirectedGraph<>(RelationshipEdge.class);
        for (DataNode node : solution) {

            Graph<DataNode, RelationshipEdge> curGraph = generateSubgraph(node, r);
            Graphs.addGraph(graph, curGraph);
            System.out.println(dataGraph.outgoingEdgesOf(node).size() + "++++++++");
            System.out.println(curGraph.edgeSet().size());
            System.out.println(res.size() + "-----------");

        }
        res.add(graph);
        return res;

    }


    private Graph<DataNode, RelationshipEdge> generateSubgraph(DataNode rootNode, int r) {
        Graph<DataNode, RelationshipEdge> curGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);
        Queue<DataNode> queue = new LinkedList<>();
        queue.add(rootNode);
//        System.out.println(rootNode.types.toArray()[0]);

        int level = 0;
        while (level <= r) {

            int size = queue.size();
            for (int i = 0; i < size; i++) {
                DataNode cur = queue.poll();
                if (!curGraph.containsVertex(cur)) {
                    curGraph.addVertex(cur);
                }

                for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(cur)) {

                    if (!curGraph.containsVertex(dataGraph.getEdgeTarget(edge))) {
                        curGraph.addVertex(dataGraph.getEdgeTarget(edge));

                    }
                    queue.add(dataGraph.getEdgeTarget(edge));
                    curGraph.addEdge(cur, dataGraph.getEdgeTarget(edge), new RelationshipEdge(edge.getLabel()));
                }
            }
            level++;
        }

        return curGraph;
    }

    private boolean staisfyP(Predicate p, DataNode n1) {

        if (!n1.attributes.containsKey(p.attr)) {
            return false;
        }
        if (p.attrType.equals("double")) {
            double variable = 0.0;
            String valueStr = (String) n1.attributes.get(p.attr).toArray()[0];
            if (p.attr.equals("Year")) {
                variable = Double.parseDouble(valueStr.substring(0, 4));
            } else if (p.attr.equals("Rating")) {
                variable = Double.parseDouble(valueStr);
            } else {
                variable = Double.parseDouble((String) n1.attributes.get(p.attr).toArray()[0]);
            }
            double value = Double.parseDouble(p.value);
            if (p.op.equals("l")) {
                return (variable > value);
            } else if (p.op.equals("le")) {
                return (variable > value || variable == value);
            } else if (p.op.equals("s")) {
                return (variable < value);
            } else if (p.op.equals("se")) {
                return (variable < value || variable == value);
            } else {
//                System.out.println(variable + " " + value);
//                System.out.println(variable==value);
                return (variable == value);

            }
        } else {
            return (n1.attributes.get(p.attr).contains(p.value));
        }
    }

    private boolean hashIntersection(HashSet<String> types, HashSet<String> types1) {
        for (String type : types) {
            if (types1.contains(type)) {
                return true;
            }

        }
        return false;
    }


    public HashSet<DataNode> execute2(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<DataNode> res = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            inspector = new VF2SubgraphIsomorphismInspector<>(graph, pattern,
                    myNodeComparator, myEdgeComparator2, false);

            HashSet<DataNode> currentMatch = new HashSet<>();
            if (inspector.isomorphismExists()) {

                Iterator<GraphMapping<DataNode, RelationshipEdge>> iterator = inspector.getMappings();
                ArrayList<DataNode> patternTypes = new ArrayList<>();
                DataNode rootNode = null;

//                for (DataNode node : pattern.vertexSet()) {
//                    patternTypes.add(node);
//                }

                for (DataNode node : pattern.vertexSet()) {

                    if (node.types.contains("Film")) {
                        rootNode = node;
                        break;
                    }
                }

                while (iterator.hasNext()) {
                    org.jgrapht.GraphMapping<DataNode, RelationshipEdge> mappings = iterator.next();
                    DataNode currentMatchedNode = mappings.getVertexCorrespondence(rootNode, false);
                    currentMatch.add(currentMatchedNode);
                }
                res.addAll(currentMatch);
            }


        }
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
//        System.out.println(duration);
//        System.out.println("Match Size:" + res.size());
        return res;

    }


    public HashSet<DataNode> execute(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<DataNode> res = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            inspector = new VF2SubgraphIsomorphismInspector<>(graph, pattern,
                    myNodeComparator, myEdgeComparator, false);

            HashSet<DataNode> currentMatch = new HashSet<>();
            if (inspector.isomorphismExists()) {

                Iterator<GraphMapping<DataNode, RelationshipEdge>> iterator = inspector.getMappings();
                ArrayList<DataNode> patternTypes = new ArrayList<>();
                DataNode rootNode = null;

//                for (DataNode node : pattern.vertexSet()) {
//                    patternTypes.add(node);
//                }

                for (DataNode node : pattern.vertexSet()) {

                    if (node.types.contains("Film")) {
                        rootNode = node;
                        break;
                    }
                }

                while (iterator.hasNext()) {
                    org.jgrapht.GraphMapping<DataNode, RelationshipEdge> mappings = iterator.next();
                    DataNode currentMatchedNode = mappings.getVertexCorrespondence(rootNode, false);
                    currentMatch.add(currentMatchedNode);
                }
                res.addAll(currentMatch);
            }


        }
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
//        System.out.println(duration);
//        System.out.println("Match Size:" + res.size());
        return res;

    }

    public HashSet<RelationshipEdge> executeEdge(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<RelationshipEdge> res = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            inspector = new VF2SubgraphIsomorphismInspector<>(graph, pattern,
                    myNodeComparator, myEdgeComparator, false);

//            HashSet<RelationshipEdge> currentMatch = new HashSet<>();

            if (inspector.isomorphismExists()) {

                Iterator<GraphMapping<DataNode, RelationshipEdge>> iterator = inspector.getMappings();

                while (iterator.hasNext()) {
                    org.jgrapht.GraphMapping<DataNode, RelationshipEdge> mappings = iterator.next();
                    RelationshipEdge edge = mappings.getEdgeCorrespondence((RelationshipEdge) pattern.edgeSet().toArray()[0], false);
                    if (!res.contains(edge)) {
//                        System.out.println("New Match");
                        res.add(edge);
                    } else {
//                        System.out.println("Old");
                    }
                }

            }


        }
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
//        System.out.println(duration);
//        System.out.println("Match Size:" + res.size());
        return res;

    }

    public HashSet<RelationshipEdge> executeEdge2(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<RelationshipEdge> res = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            inspector = new VF2SubgraphIsomorphismInspector<>(graph, pattern,
                    myNodeComparator, myEdgeComparator2, false);

//            HashSet<RelationshipEdge> currentMatch = new HashSet<>();

            if (inspector.isomorphismExists()) {

                Iterator<GraphMapping<DataNode, RelationshipEdge>> iterator = inspector.getMappings();


                while (iterator.hasNext()) {
                    org.jgrapht.GraphMapping<DataNode, RelationshipEdge> mappings = iterator.next();
                    for (RelationshipEdge e : pattern.edgeSet()) {

                        RelationshipEdge edge = mappings.getEdgeCorrespondence(e, false);
                        if (!res.contains(edge)) {
//                        System.out.println("New Match");
                            res.add(edge);
                        } else {
//                        System.out.println("Old");
                        }
                    }
                }
            }


        }
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
//        System.out.println(duration);
//        System.out.println("Match Size:" + res.size());
        return res;

    }


    public double getGroup(String label, ArrayList<String> group) {


        int count = 0;
        HashSet<DataNode> dup = new HashSet<>();

        for (int i = 0; i < group.size(); i++) {

            for (DataNode node : candidates) {
                if (!node.types.contains(label)) {
                    continue;
                }
                String type = group.get(i);
                if (!node.attributes.containsKey("Category")) {
                    continue;
                }
                if (node.attributes.get("Category").contains(type)) {
                    count++;
                }
            }
        }

        return count;
    }

    private boolean isValid(HashSet<DataNode> set, ArrayList<String> group) {

        for (int i = 0; i < group.size(); i++) {
            String g = group.get(i);
            for (DataNode node : set) {
                if (node.attributes.containsKey("Category")) {
                    if (node.attributes.get("Category").contains(g)) {
                        return true;
                    }

                }

            }

        }
        return false;
    }


    private HashSet<DataNode> streamGenSolution(HashSet<DataNode> solution, DataNode node, ArrayList<String> group, ArrayList<Rangepair> cc, int r, int m) {

        HashSet<DataNode> curSolution = new HashSet<>(solution);
        String curGroup = (String) node.attributes.get("Category").toArray()[0];
        int index = -1;

        for (int i = 0; i < group.size(); i++) {
            if (group.get(i).equals(curGroup)) {
                index = i;
            }
        }
        if (index == -1) {
            return solution;
        }

        int curCount = 0;
        for (DataNode tnode : solution) {
            if (((String) tnode.attributes.get("Category").toArray()[0]).equals(curGroup)) {
                curCount++;
            }
        }


        if (curCount < cc.get(index).u) {
            curSolution.add(node);
            System.out.println("add");
            return curSolution;
        } else {

            HashSet<DataNode> temp = new HashSet<>(solution);
            HashSet<DataNode> temp1 = new HashSet<>(solution);
            temp1.add(node);


            double min = Double.MAX_VALUE;
            DataNode toBeUpdate = null;

            for (DataNode curNode : curSolution) {

                HashSet<DataNode> temp2 = new HashSet<>(curSolution);
                HashSet<DataNode> temp3 = new HashSet<>(curSolution);
                temp2.remove(curNode);


            }


        }


        return curSolution;
    }

    private DataNode getSetMinus(HashSet<DataNode> solution, HashSet<DataNode> updatedSolution) {

        for (DataNode node : updatedSolution) {
            if (!solution.contains(node)) {
                return node;
            }
        }

        return null;

    }


    public void runMaxSumGenStreaming(ArrayList<String> group, ArrayList<Rangepair> cc, int r, int m)
            throws IOException, ClassNotFoundException {


        for (DataNode node : dataGraph.vertexSet()) {
            if (node.types.contains("Film")) {
                if (node.attributes.containsKey("Category")) {

                    if (node.attributes.containsKey("Nation")) {
                        if (node.attributes.get("Nation").toArray()[0].equals("USA")) {
                            HashSet<String> groups = new HashSet<>(group);
                            if (groups.contains(node.attributes.get("Category").toArray()[0])) {
                                candidates.add(node);
                                if (candidates.size() > 400) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }


        System.out.println(candidates.size() + "c size");
        ArrayList<Graph<DataNode, RelationshipEdge>> allGraphList = getInducedGraph(candidates, r);
        HashMap<Integer, HashSet<RelationshipEdge>> totalSummaryMap = new HashMap<>();
        HashSet<DataNode> S = new HashSet<>();
        HashSet<Integer> resultPatterns = new HashSet<>();

        int k = 20;

        for (DataNode node : candidates) {
            System.out.println(node.getNodeName());
            DataNode tbAdd = null;
            HashSet<DataNode> M = new HashSet<>(S);
            M.add(node);
            if (isExtenable(group, cc, M, m)) {

                tbAdd = node;

            } else {

                System.out.println("NO!!!!!!!");
                HashSet<DataNode> temp = new HashSet<>(S);
                HashSet<DataNode> temp2;
                temp.add(node);
                double min = Double.MAX_VALUE;
                DataNode minNode = null;

                for (DataNode e : S) {
                    temp2 = new HashSet<>(S);
                    temp2.remove(e);
                    temp2.add(node);
                    if (isExtenable(group, cc, temp2, m)){

                        if (min > getObj2(temp2, e)) {
                            min = getObj2(temp2, e);
                            System.out.println("identify");
                            minNode = e;
                        }
                    }

                }

                if (minNode != null) {
                    HashSet<DataNode> temp3 = new HashSet<>(S);
                    temp3.remove(minNode);
                    if (getObj2(temp3,node) >=   2 * getObj2(temp3,minNode)) {
                        temp3.add(node);
                        tbAdd = node;
                        System.out.println("switch");

                    } else {
                        temp3.add(minNode);
                    }
                    S = temp3;

                }


            }

             if (tbAdd != null) {
                 System.out.println("process" + tbAdd.getNodeName());
                 S.add(tbAdd);
                 HashSet<DataNode> single = new HashSet<>();
                 single.add(tbAdd);

                 ArrayList<Graph<DataNode, RelationshipEdge>> singleTargetGraphList = getInducedGraph(single, r);

                 ArrayList<Graph<DataNode, RelationshipEdge>> minedPatterns = algoGSPAN.runAlgorithm(singleTargetGraphList, "outputPattern.txt", 1,
                         false, false, 5, false, nodeLabelMap, edgeLabelMap);

                 int pos = 0;
                 while (pos < minedPatterns.size()) {

                     if (resultPatterns.size() == k) {
                         break;
                     }

                     if (resultPatterns.size() < k) {
                         int index = minedPatterns.get(pos).hashCode();
                         totalSummaryMap.put(index, getSummary(minedPatterns.get(pos), allGraphList));
                         resultPatterns.add(index);
                         pos++;
                     }

                 }

                 System.out.println(resultPatterns);
                 if (pos == minedPatterns.size()) {
                     continue;
                 }
                 int bound = Math.min(8,minedPatterns.size());
                 for (int i = pos; i < bound; i++) {

                     HashSet<Integer> tempResultPatterns = new HashSet<>(resultPatterns);
                     for (int index : tempResultPatterns) {
                         // 1
                         HashSet<Integer> tempResult = new HashSet<>(tempResultPatterns);
                         HashSet<RelationshipEdge> currSum = new HashSet<>();
                         for (int j : tempResult) {
//                             System.out.println("get" + j);
                             currSum.addAll(totalSummaryMap.get(j));
                         }
                         int size = currSum.size();
                         System.out.println(size);

                         HashSet<RelationshipEdge> currSum2 = new HashSet<>();
                         tempResult.remove(index);

                         for (int j : tempResult) {
                             currSum2.addAll(totalSummaryMap.get(j));
                         }

                         currSum2.addAll(getSummary(minedPatterns.get(i), allGraphList));

                         int size2 = currSum2.size();
                         System.out.println(size2);

                         if (size2 > size) {
                             resultPatterns.remove(index);
                             resultPatterns.add(minedPatterns.get(i).hashCode());
                             totalSummaryMap.put(minedPatterns.get(i).hashCode(), getSummary(minedPatterns.get(i), allGraphList));
                             System.out.println("Update!!!!!");
                         }

                     }

                 }

                 HashSet<RelationshipEdge> totalSummary = new HashSet<>();
                 for (int i : resultPatterns) {
                     System.out.println(i + "selected pattern");
                     totalSummary.addAll(totalSummaryMap.get(i));
                 }
//
//
                 HashSet<RelationshipEdge> totalInducedGraph = new HashSet<>();
                 for (int j = 0; j < allGraphList.size(); j++) {
                     totalInducedGraph.addAll(allGraphList.get(j).edgeSet());
                 }

                 int totalSize = totalSummary.size();
                 int sumSize = totalInducedGraph.size();
                 System.out.println("summary total size " + totalSize);
                 double ratio = 1.0 - ((double) totalSize / sumSize);
                 System.out.println("ratio " + ratio);

             }
        }



        for (DataNode s : S) {
            if (s == null) {
                continue;
            }

            System.out.println((String)(s.attributes.get("Category").toArray()[0]));
        }
        System.out.println(S.size());
        System.out.println(getObj(S));

    }

    private int getN(DataNode node) {
        HashSet<DataNode> ns = new HashSet<>();


        for (RelationshipEdge edge : dataGraph.incomingEdgesOf(node)) {
            ns.add(dataGraph.getEdgeSource(edge));
        }
        for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(node)) {
            ns.add(dataGraph.getEdgeTarget(edge));
        }
        return ns.size();
    }

    private int getNOOfViolation(HashSet<DataNode> updatedSolution, ArrayList<String> group, ArrayList<Rangepair> cc) {
        int total = 0;
        for (int i = 0; i < group.size(); i++) {

            int count = 0;
            for (DataNode node : updatedSolution) {
                String curGroup = (String) node.attributes.get("Category").toArray()[0];
                if (curGroup.equals(group.get(i))) {
                    count++;
                }
            }

            int du = count - cc.get(i).u;
            int dl = cc.get(i).l - count;
            int curTotalD = Math.max(du, dl);
            curTotalD = Math.max(curTotalD, 0);
            total += curTotalD;

        }
        return total;
    }


    private double getObj(HashSet<DataNode> temp) {


        HashSet<DataNode> ns = new HashSet<>();


        for (DataNode n : temp) {
            for (RelationshipEdge edge : dataGraph.incomingEdgesOf(n)) {
                ns.add(dataGraph.getEdgeSource(edge));
            }
            for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(n)) {
                ns.add(dataGraph.getEdgeTarget(edge));
            }
        }

        double total1 = ns.size();

        return total1;

    }

    public void runMaxSumGen(ArrayList<String> group, ArrayList<Rangepair> cc, int r, int m)
            throws IOException, ClassNotFoundException {

        long s = System.nanoTime();

        for (DataNode node : dataGraph.vertexSet()) {
            if (node.types.contains("Film")) {
                if (node.attributes.containsKey("Category")) {
//                    System.out.println(node.attributes);
                    if (node.attributes.containsKey("Nation")) {
                        if (node.attributes.get("Nation").toArray()[0].equals("USA")) {
                            HashSet<String> groups = new HashSet<>(group);
                            if (groups.contains(node.attributes.get("Category").toArray()[0])) {
                                candidates.add(node);
                                if (candidates.size() > 800) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println(candidates.size() + "c size");
        HashSet<DataNode> solution = FairGen(group, cc, candidates, m);
        System.out.println("Size Sp: " + solution.size());
        System.out.println(getObj(solution));

        System.out.println(getObj2(solution, null) + " o value");
        System.out.println(getVio(solution, group, cc) + " vio value");





        long endTime = System.nanoTime();
        long duration = (endTime - s);
        System.out.println(duration + "s partial");

        ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList = getInducedGraph(solution, r);
        ArrayList<Graph<DataNode, RelationshipEdge>> allGraphList = getInducedGraph(candidates, r);

//
//        OutputGraph(targetGraphList, solution, "streamTest1.txt");
//        OutputGraphGrami(targetGraphList, "gramiG1.txt");
//        OutputGraphGrami(allGraphList, "allGraphG1.text");
////
//        ArrayList<Graph<DataNode, RelationshipEdge>> patterns = evaluateGramiOutput("C:\\Users\\Nick\\Downloads\\GraMi-master\\Output1.txt",
//                nodeLabelMap, allGraphList);
//        HashSet<DataNode> cMtachesGrami = new HashSet<>();
////
//        int total = 0;
//
//        for (int i = 0; i < patterns.size(); i++) {
//            HashSet<DataNode> curResult = execute2(patterns.get(i), allGraphList);
//
//            cMtachesGrami.addAll(curResult);
//            System.out.println(curResult.size() + " Add");
//        }
////
//        int c1 = 0;
//        int c2 = 0;
//        int c3 = 0;
//
//        for (DataNode n : cMtachesGrami) {
//            if (n == null) {
//                continue;
//            }
//            if (n.attributes == null) {
//                continue;
//            }
//            if (!n.attributes.containsKey("Category")) {
//                continue;
//            }
//            if (n.attributes.get("Category").toArray()[0].equals("Comedy")) {
//                c1++;
//                continue;
//            } else if (n.attributes.get("Category").toArray()[0].equals("Action")) {
//
//                c2++;
//                continue;
//            } else if (n.attributes.get("Category").toArray()[0].equals("Romance")) {
//                c3++;
//                continue;
//            }
//        }
//
//        System.out.println(c1 + " " + c2 + " " + c3 + "" + "Grami");
//
//        System.out.println(patterns.size() + " gramicp size");
////
////
////
//        HashMap<Integer, HashSet<RelationshipEdge>> allSummaryMap = new HashMap<>();
//
//        HashSet<RelationshipEdge> allSummary = new HashSet<>();
//
//
//        for (int i = 0; i < patterns.size(); i++) {
//            System.out.println(getSummary(patterns.get(i), allGraphList));
//            allSummaryMap.put(i, getSummary(patterns.get(i), allGraphList));
//        }
//
////
//
//        for (int j : allSummaryMap.keySet()) {
//            System.out.println(j + "selected pattern");
////            for (DataNode pNode : miningCandidates.get(i).vertexSet()) {
////                System.out.println(pNode.getNodeName());
////                System.out.println(pNode.types);
////            }
////            for (RelationshipEdge pEdge : miningCandidates.get(i).edgeSet()) {
////                System.out.println(miningCandidates.get(i).getEdgeSource(pEdge).getNodeName());
////                System.out.println(miningCandidates.get(i).getEdgeTarget(pEdge).getNodeName());
////                System.out.println(pEdge.getLabel());
////            }
//            allSummary.addAll(allSummaryMap.get(j));
//
//        }
////
//        HashSet<RelationshipEdge> allInducedGraph = new HashSet<>();
//
//        for (int j = 0; j < allGraphList.size(); j++) {
//            allInducedGraph.addAll(allGraphList.get(j).edgeSet());
//        }
//        System.out.println();
//
////
//        int size1 = allInducedGraph.size();
//        System.out.println("graph total size " + size1);
//        int totalSize1 = allSummary.size();
////
//        System.out.println("summary total size " + totalSize1);
//        double ratio1 = 1.0 - ((double) totalSize1 / size1);
//        System.out.println("ratio " + ratio1);
//

        long startTime = System.nanoTime();
//
//
        ArrayList<Graph<DataNode, RelationshipEdge>> miningCandidates = algoGSPAN.runAlgorithm(targetGraphList, "output_mining.txt", 0,
                false, false, 5, false, nodeLabelMap, edgeLabelMap);

//
        System.out.println(miningCandidates.size() + " cp size");
//
//
        HashMap<Integer, HashSet<DataNode>> totalSetCover = new HashMap<>();
        HashMap<Integer, HashSet<RelationshipEdge>> totalSummaryMap = new HashMap<>();
        for (int i = 0; i < miningCandidates.size(); i++) {
//            System.out.println(miningCandidates.get(i).edgeSet().size());
//            if (i > 2000) {
//                break;
//            }
//            long startTime1 = System.nanoTime();
            HashSet<DataNode> curResult = execute(miningCandidates.get(i), targetGraphList);

//
//            long endTime1 = System.nanoTime();
//            long duration = (endTime1 - startTime1);
//            System.out.println("e time " + duration);


            if (curResult.size() == 0) {
                continue;
            }
            System.out.println(curResult.size() + "mmmmmm");

            totalSetCover.put(i, curResult);

//            cMtaches.add(curResult);

//            correctionSize.add(getCorrection(miningCandidates.get(i), targetGraphList));
        }
        System.out.println(totalSetCover.keySet().size() + " cover size");


        for (int i : totalSetCover.keySet()) {
            totalSummaryMap.put(i, getSummary(miningCandidates.get(i), allGraphList));
        }
        int k = 20;
        HashSet<Integer> results = FairGenPattern(totalSummaryMap, totalSetCover, group, cc, k, m);

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(results + " p!");
        System.out.println(results.size() + " p size!");
//
        HashSet<RelationshipEdge> totalSummary = new HashSet<>();
        for (int i : results) {
            System.out.println(i + "selected pattern");
//            for (DataNode pNode : miningCandidates.get(i).vertexSet()) {
//                System.out.println(pNode.getNodeName());
//                System.out.println(pNode.types);
//            }
//            for (RelationshipEdge pEdge : miningCandidates.get(i).edgeSet()) {
//                System.out.println(miningCandidates.get(i).getEdgeSource(pEdge).getNodeName());
//                System.out.println(miningCandidates.get(i).getEdgeTarget(pEdge).getNodeName());
//                System.out.println(pEdge.getLabel());
//            }
            totalSummary.addAll(totalSummaryMap.get(i));
            System.out.println(totalSetCover.get(i));
        }
//
//
        HashSet<RelationshipEdge> totalInducedGraph = new HashSet<>();

        for (int j = 0; j < allGraphList.size(); j++) {
            totalInducedGraph.addAll(allGraphList.get(j).edgeSet());
        }
        System.out.println();
//        System.out.println(getVio(totalSetCover,results, group, cc) + "vio");
//
        int size = totalInducedGraph.size();
        System.out.println("graph total size " + size);
        int totalSize = totalSummary.size();
//
        System.out.println("summary total size " + totalSize);
        double ratio = 1.0 - ((double) totalSize / size);
        System.out.println("ratio " + ratio);
//
        long endTime1 = System.nanoTime();
        long duration1 = (endTime1 - startTime);
        System.out.println(duration1 + "partial");


    }

    private int getVio(HashMap<Integer, HashSet<DataNode>> set, HashSet<Integer> results, ArrayList<String> group, ArrayList<Rangepair> cc) {

        int vio = 0;


        for (int i = 0; i < group.size(); i++) {
            int cnt = 0;
            for (int j : results) {

                for (DataNode node : set.get(j)) {
                    if (node.attributes.get("Category").toArray()[0].equals(group.get(i))) {
                        cnt++;
                    }
                }
            }
            System.out.println(cnt);
            int m1 = cnt - cc.get(i).u;
            int m2 = cc.get(i).l - cnt;
            System.out.println(m1 + "...." + m2);
            int max = Math.max(m1, m2);
            int maxV = Math.max(max, 0);
            vio += maxV;
        }

        return vio;
    }

    private int getVio(HashSet<DataNode> set, ArrayList<String> group, ArrayList<Rangepair> cc) {

        int vio = 0;
        for (int i = 0; i < group.size(); i++) {
            int cnt = 0;
            for (DataNode node : set) {
                if (node.attributes.get("Category").toArray()[0].equals(group.get(i))) {
                    cnt++;
                }
            }
            System.out.println(cnt);
            int m1 = cnt - cc.get(i).u;
            int m2 = cc.get(i).l - cnt;
            System.out.println(m1 + "...." + m2);
            int max = Math.max(m1, m2);
            int maxV = Math.max(max, 0);
            vio += maxV;
        }
        return vio;
    }


    private HashSet<Integer> FairGenPattern(HashMap<Integer, HashSet<RelationshipEdge>> setSummary,
                                            HashMap<Integer, HashSet<DataNode>> setCover, ArrayList<String> group,
                                            ArrayList<Rangepair> cc, int k, int m) {


        HashSet<Integer> S = new HashSet<>();

        System.out.println(setCover.size() + " candidate pattern size");
        HashSet<Integer> indexSet = new HashSet<>();
        for (int index : setCover.keySet()) {
            indexSet.add(index);
        }

        System.out.println(indexSet + "total size");

        int prevSize = 0;
        while (S.size() < k) {
            prevSize = S.size();
            System.out.println(S.size() + "cur size");
            HashSet<Integer> U = new HashSet<>();
            for (Integer i : indexSet) {
                HashSet<Integer> temp = new HashSet<>(S);
                temp.add(i);
                boolean f = isExtenableP(group, cc, temp, m, setCover);
//                System.out.println(f);
                if (f) {
                    U.add(i);
                }
            }
            System.out.println(U.size());
            if (U.size() == 0) {
                break;
            }

            double maxGain = Double.MIN_VALUE;
            int maxIndex = -1;

            for (int tempIndex : U) {


                double currObj = getObj1(tempIndex, S, setSummary);
//                System.out.println(currObj);
                if (currObj > maxGain) {
                    maxGain = currObj;
                    maxIndex = tempIndex;

                }
            }
//
            if (maxIndex != -1) {
                S.add(maxIndex);
                indexSet.remove(maxIndex);
                System.out.println(maxIndex + " add!");
            }
            if (prevSize ==  S.size()) {
                 break;
            }
        }

        return S;
    }


    private double getObj1(int temp, HashSet<Integer> s, HashMap<Integer, HashSet<RelationshipEdge>> setSummary) {

        HashSet<RelationshipEdge> totalEdge = new HashSet<>();
        for (int index : s) {
            totalEdge.addAll(setSummary.get(index));
        }
        double total1 = totalEdge.size();
        totalEdge.addAll(setSummary.get(temp));
        double total2 = totalEdge.size();
        return total2 - total1;
    }

    private boolean isExtenableP(ArrayList<String> group, ArrayList<Rangepair> cc, HashSet<Integer> temp,
                                 int m, HashMap<Integer, HashSet<DataNode>> setCover) {

        HashMap<Integer, Integer> cs = new HashMap<>();

        for (int i = 0; i < group.size(); i++) {
            cs.put(i, 0);
        }

        for (int i = 0; i < group.size(); i++) {

            for (int index : temp) {
                HashSet<DataNode> curMatch = setCover.get(index);
                for (DataNode node : curMatch) {
                    if (!node.attributes.containsKey("Category")) {
                        continue;
                    }
                    if (node.attributes.get("Category").toArray()[0].equals(group.get(i))) {
                        int curCount = cs.get(i);
                        cs.put(i, curCount + 1);
                    }
                }
            }
        }

        System.out.println(cs + "++++++++++");
        boolean flag = true;
        for (int i = 0; i < group.size(); i++) {
            if (cs.get(i) >= cc.get(i).u) {
                flag = false;
                break;
            }
        }


        int sumc = 0;
        for (int i = 0; i < group.size(); i++) {
            sumc += Math.max(cc.get(i).l, cs.get(i));
        }

        return flag && sumc <= m;


    }


    private void OutputGraphGrami(ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList, String path) throws IOException {
        int i = 0;

        HashMap<Integer, String> map = new HashMap();
        ArrayList<Triple> triples = new ArrayList<>();
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        HashSet<Integer> sl = new HashSet<>();
        HashMap<String, DataNode> nodeMap = new HashMap<>();
        HashMap<DataNode, String> reverseNodeMap = new HashMap<>();
        HashMap<String, String> nodeLabelMap = new HashMap<>();

        int nodeId = 0;
        int nodeLabelId = 0;
        HashSet<DataNode> dup = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            for (DataNode node : graph.vertexSet()) {
                if (dup.contains(node)) {
                    continue;
                }
                nodeMap.put(String.valueOf(nodeId), node);
                reverseNodeMap.put(node, String.valueOf(nodeId));
                nodeId++;
                if (!nodeLabelMap.containsKey(node.types.toArray()[0])) {
                    nodeLabelMap.put((String) node.types.toArray()[0], String.valueOf(nodeLabelId));
                    nodeLabelId++;
                }
                dup.add(node);

            }
        }
        writer.write("# t 1" + "\n");
        for (int j = 0; j < nodeId; j++) {
            writer.write("v" + " " + j + " " + nodeMap.get(String.valueOf(j)).types.toArray()[0].hashCode() + "\n");
        }


        HashSet<Integer> set = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            for (RelationshipEdge edge : graph.edgeSet()) {

                int hash1 = graph.getEdgeSource(edge).getNodeName().hashCode() * 33 + graph.getEdgeTarget(edge).getNodeName().hashCode();
                int hash2 = graph.getEdgeTarget(edge).getNodeName().hashCode() * 33 + graph.getEdgeSource(edge).getNodeName().hashCode();
                if (set.contains(hash1)) {
                    continue;
                }
                if (set.contains(hash2)) {
                    continue;
                }
                set.add(hash1);
                set.add(hash2);

                String s = reverseNodeMap.get(graph.getEdgeSource(edge));
                String t = reverseNodeMap.get(graph.getEdgeTarget(edge));
                writer.write("e" + " " + s + " " + t + " " + edge.getLabel().hashCode() + "\n");
            }

        }


        for (int j = 0; j < triples.size(); j++) {

            writer.write(triples.get(j).s);
            writer.write("\t");
            writer.write(triples.get(j).t);
            writer.write("\t");
            writer.write(1 + "\n");

        }


        writer.close();
        System.out.print(sl + "sssssssssssssssssssssssssssssssssssssssssssss");


    }

    private void OutputGraph(ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList, HashSet<DataNode> solutions, String path) throws IOException {

        int i = 0;
        HashMap<Integer, String> map = new HashMap();
        ArrayList<Triple> triples = new ArrayList<>();
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        HashSet<Integer> sl = new HashSet<>();

        HashSet<Integer> set = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
            for (RelationshipEdge edge : graph.edgeSet()) {

                int hash1 = graph.getEdgeSource(edge).getNodeName().hashCode() * 33 + graph.getEdgeTarget(edge).getNodeName().hashCode();
                int hash2 = graph.getEdgeTarget(edge).getNodeName().hashCode() * 33 + graph.getEdgeSource(edge).getNodeName().hashCode();
                if (set.contains(hash1)) {
                    continue;
                }
                if (set.contains(hash2)) {
                    continue;
                }
                set.add(hash1);
                set.add(hash2);
                int s = graph.getEdgeSource(edge).getNodeName().hashCode();
                String ss;
                if (map.containsKey(s)) {
                    ss = map.get(s);
                } else {
                    ss = String.valueOf(i);
                    map.put(s, String.valueOf(i));
                    if (solutions.contains(graph.getEdgeSource(edge))) {
                        sl.add(i);
                        System.out.println("add");
                    }
                    i++;
                }

                int t = graph.getEdgeTarget(edge).getNodeName().hashCode();
                String st;
                if (map.containsKey(t)) {
                    st = map.get(t);
                } else {
                    st = String.valueOf(i);
                    map.put(t, String.valueOf(i));
                    if (solutions.contains(graph.getEdgeTarget(edge))) {
                        sl.add(i);
                        System.out.println("add");
                    }
                    i++;

                }


                triples.add(new Triple(ss, st, "1"));
            }

        }

        for (int j = 0; j < triples.size(); j++) {
            writer.write(triples.get(j).s);
            writer.write("\t");
            writer.write(triples.get(j).t);
            writer.write("\t");
            writer.write(1 + "\n");
        }

        writer.close();
        System.out.print(sl + "sssssssssssssssssssssssssssssssssssssssssssss");

    }

    private HashSet<RelationshipEdge> getSummary2(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<RelationshipEdge> summary = new HashSet<>();
        for (RelationshipEdge edge : pattern.edgeSet()) {
            DataNode s = pattern.getEdgeSource(edge);
            DataNode t = pattern.getEdgeTarget(edge);
            String key = (String) s.types.toArray()[0] + (String) t.types.toArray()[0] + edge.getLabel();
            HashSet<RelationshipEdge> csummary;
            System.out.println(key);
            if (summaryMap.containsKey(key)) {
                csummary = summaryMap.get(key);
                summary.addAll(csummary);

            } else {
                Graph<DataNode, RelationshipEdge> curPatterEdge = new DefaultDirectedGraph<>(RelationshipEdge.class);
                curPatterEdge.addVertex(s);
                curPatterEdge.addVertex(t);
                curPatterEdge.addEdge(s, t, new RelationshipEdge(edge.getLabel()));
                csummary = executeEdge2(curPatterEdge, targetGraphList);
                summaryMap.put(key, csummary);

            }


            summary.addAll(csummary);

        }


        return summary;
    }

    private HashSet<RelationshipEdge> getSummary(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<RelationshipEdge> summary = new HashSet<>();
        for (RelationshipEdge edge : pattern.edgeSet()) {
            DataNode s = pattern.getEdgeSource(edge);
            DataNode t = pattern.getEdgeTarget(edge);
            String key = (String) s.types.toArray()[0] + (String) t.types.toArray()[0] + edge.getLabel();
            HashSet<RelationshipEdge> csummary;
            System.out.println(key);
            if (summaryMap.containsKey(key)) {
                csummary = summaryMap.get(key);
                summary.addAll(csummary);
                continue;

            } else {
                Graph<DataNode, RelationshipEdge> curPatterEdge = new DefaultDirectedGraph<>(RelationshipEdge.class);
                curPatterEdge.addVertex(s);
                curPatterEdge.addVertex(t);
                curPatterEdge.addEdge(s, t, new RelationshipEdge(edge.getLabel()));
                csummary = executeEdge(curPatterEdge, targetGraphList);
                summaryMap.put(key, csummary);

            }


            summary.addAll(csummary);

        }


        return summary;
    }

    private Graph<DataNode, RelationshipEdge> getInducedGraph2(Graph<DataNode, RelationshipEdge> dataNodeRelationshipEdgeGraph, DataNode root, int r) {


        Graph<DataNode, RelationshipEdge> curGraph = generateSubgraph(root, r);
        return curGraph;
    }

    private DataNode getRoot(Graph<DataNode, RelationshipEdge> dataNodeRelationshipEdgeGraph) {

        for (DataNode node : dataNodeRelationshipEdgeGraph.vertexSet()) {
            if (node.types.contains("Film")) {
                return node;
            }
        }

        return null;
    }

    private HashSet<DataNode> FairGen(ArrayList<String> group, ArrayList<Rangepair> cc, HashSet<DataNode> candidates, int m) {

        HashSet<DataNode> S = new HashSet<>();
        HashSet<DataNode> dup = new HashSet<>();
        System.out.println(candidates.size());
        HashSet<DataNode> cansub = new HashSet<>();
        for (DataNode n : candidates) {
            cansub.add(n);
//            if (cansub.size() > 1000) {
//                break;
//            }
        }

        while (S.size() < m) {
            HashSet<DataNode> U = new HashSet<>();
            for (DataNode e : cansub) {
                if (!e.attributes.containsKey("Category")) {
                    continue;
                }
                if (S.contains(e)) {
                    continue;
                }
                boolean flag = false;
                for (String g : group) {
                    if (e.attributes.get("Category").contains(g)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    continue;
                }

                HashSet<DataNode> temp = new HashSet<>(S);
                temp.add(e);


                if (isExtenable(group, cc, temp, m)) {
                    U.add(e);
                }

            }

            double maxGain = Double.MIN_VALUE;
            DataNode maxNode = null;

            for (DataNode node : U) {
                if (dup.contains(node)) {
                    continue;
                }
                HashSet<DataNode> temp = new HashSet<>(S);

                double currObj = getObj2(temp, node);
                if (currObj > maxGain) {
                    maxGain = currObj;
                    maxNode = node;

                }
            }

            if (maxNode != null) {
                S.add(maxNode);
                System.out.println("add");
                dup.add(maxNode);
                candidates.remove(maxNode);
            }
        }

        return S;

    }

    private double getObj2(HashSet<DataNode> temp, DataNode node) {


        HashSet<DataNode> ns = new HashSet<>();


        for (DataNode n : temp) {
            for (RelationshipEdge edge : dataGraph.incomingEdgesOf(n)) {
                ns.add(dataGraph.getEdgeSource(edge));
            }
            for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(n)) {
                ns.add(dataGraph.getEdgeTarget(edge));
            }
        }

        double total1 = ns.size();

        if (node == null) {
            return total1;
        }

        for (RelationshipEdge edge : dataGraph.incomingEdgesOf(node)) {
            ns.add(dataGraph.getEdgeSource(edge));
        }
        for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(node)) {
            ns.add(dataGraph.getEdgeTarget(edge));
        }

        double total2 = ns.size();
        return total2 - total1;

    }


    private boolean isExtenable(ArrayList<String> group, ArrayList<Rangepair> cc, HashSet<DataNode> temp, int m) {


        ArrayList<Integer> cm = new ArrayList<>();

        for (int i = 0; i < group.size(); i++) {
            int count = 0;
            for (DataNode node : temp) {
                if(node == null) {
                    continue;
                }
                if (!node.attributes.containsKey("Category")) {
                    continue;
                }
                if (node.attributes.get("Category").toArray()[0].equals(group.get(i))) {
                    count++;
                }
            }
            cm.add(count);

        }

        boolean flag = true;
        for (int i = 0; i < group.size(); i++) {
            if (cm.get(i) > cc.get(i).u) {
                flag = false;
                break;
            }
        }


        int totalSum = 0;

        for (int i = 0; i < group.size(); i++) {
            totalSum += Math.max(cm.get(i), cc.get(i).l);
        }
//        System.out.println(cm);
        return (totalSum <= m) && flag;


    }


    private Graph<DataNode, RelationshipEdge> generatePattern1() {
        Graph<DataNode, RelationshipEdge> pattern = new DefaultDirectedGraph<>(RelationshipEdge.class);
        DataNode node = new DataNode("1");
        node.types.add("Film");
        node.isRoot = true;

        DataNode node1 = new DataNode("2");
        node1.types.add("Person");

        DataNode node2 = new DataNode("3");
        node2.types.add("Person");


        DataNode node3 = new DataNode("4");
        node3.types.add("Person");

        DataNode node4 = new DataNode("5");
        node4.types.add("Person");


        pattern.addVertex(node);
        pattern.addVertex(node1);
        pattern.addVertex(node2);
//        pattern.addVertex(node3);
//        pattern.addVertex(node4);
        pattern.addEdge(node, node1, new RelationshipEdge("starring"));
        pattern.addEdge(node, node2, new RelationshipEdge("director"));
//        pattern.addEdge(node, node3, new RelationshipEdge("writer"));
//        pattern.addEdge(node, node4, new RelationshipEdge("producer"));
        return pattern;

    }

    public double getDelta(HashSet<DataNode> set, ArrayList<String> group, ArrayList<Integer> cc) {


        ArrayList<Integer> cMatch = new ArrayList<>();
        HashSet<DataNode> duplicate = new HashSet<>();


        for (int i = 0; i < group.size(); i++) {
            int count = 0;
            String type = group.get(i);
            for (DataNode node : set) {
                if (!node.attributes.containsKey("Category")) {
                    continue;
                }
                if (node.attributes.get("Category").contains(type) && !duplicate.contains(node)) {
                    duplicate.add(node);
                    count++;
                }
            }

            cMatch.add(count);
        }
        double dist = 0;
        for (int i = 0; i < cc.size(); i++) {
            dist += (cMatch.get(i) - cc.get(i));
        }

        System.out.println(dist + " dist");
        return dist;
    }


    public HashSet<DataNode> estimate(Graph<DataNode, RelationshipEdge> pattern) {

        DataNode curNode = null;
        for (DataNode node : pattern.vertexSet()) {
            if (node.isRoot) {
                curNode = node;
                break;
            }
        }

        Stack<DataNode> stack = new Stack<DataNode>();
        stack.add(curNode);


        return null;
    }


    public double getDist(ArrayList<Integer> matchCardinality, ArrayList<Integer> cardnality) {
        double dist = 0.0;
        for (int i = 0; i < matchCardinality.size() - 2; i++) {
            int curMC = matchCardinality.get(i);
            int curCC = cardnality.get(i);
            dist += Math.abs(curMC - curCC);
        }
        return dist;
    }

    public double getDiv(HashSet<DataNode> currentMatch) {
        double div = 0.0;
        for (DataNode n : currentMatch) {
            for (DataNode m : currentMatch) {
                if (m.equals(n)) {
                    continue;
                }
                int i = 0;
                double curDiv = 0.0;
                for (String key : n.attributes.keySet()) {
                    if (m.attributes.containsKey(key)) {
                        i++;
                        curDiv += (1 - JaccardDist((String) n.attributes.get(key).toArray()[0],
                                (String) m.attributes.get(key).toArray()[0]));
                    }

                }
                curDiv = curDiv / i;
                div += curDiv;
            }
        }

        return (2 * div) / (candidates.size() - 1);
    }

    private double JaccardDist(String str1, String str2) {
        Set<Character> s1 = new HashSet<>();
        Set<Character> s2 = new HashSet<>();

        for (int i = 0; i < str1.length(); i++) {
            s1.add(str1.charAt(i));
        }
        for (int j = 0; j < str2.length(); j++) {
            s2.add(str2.charAt(j));
        }

        double mergeNum = 0;//Number of union elements
        double commonNum = 0;//Number of same elements (intersection)

        for (Character ch1 : s1) {
            for (Character ch2 : s2) {
                if (ch1.equals(ch2)) {
                    commonNum++;
                }
            }
        }

        mergeNum = s1.size() + s2.size() - commonNum;
        double jaccard = commonNum / mergeNum;
        return jaccard;
    }


    public Graph<DataNode, RelationshipEdge> generatePattern() {

        Graph<DataNode, RelationshipEdge> pattern = new DefaultDirectedGraph<>(RelationshipEdge.class);
        DataNode node = new DataNode("1");
        node.types.add("Film");
        node.isRoot = true;

        DataNode node1 = new DataNode("2");
        node1.types.add("Person");

        DataNode node2 = new DataNode("3");
        node2.types.add("Person");


        DataNode node3 = new DataNode("4");
        node3.types.add("Person");

        DataNode node4 = new DataNode("5");
        node4.types.add("Person");


        pattern.addVertex(node);
//        pattern.addVertex(node1);
//        pattern.addVertex(node2);
//        pattern.addVertex(node3);
//        pattern.addVertex(node4);
//        pattern.addEdge(node, node1, new RelationshipEdge("starring"));
//        pattern.addEdge(node, node2, new RelationshipEdge("director"));
//        pattern.addEdge(node, node3, new RelationshipEdge("writer"));
//        pattern.addEdge(node, node4, new RelationshipEdge("producer"));
        return pattern;

    }


    private ArrayList<XVariable> enumerateAll() {
        ArrayList<XVariable> Xlist = new ArrayList<>();
        double value = 1916;
        while (value <= 2019) {

            XVariable X = new XVariable();
            X.predicates.add(new Predicate("Year", "l", String.valueOf(value), "double", "1"));
            Xlist.add(X);

            double rValue = 1;
//            while (rValue <= 5) {
//                XVariable X = new XVariable();
//                X.predicates.add(new Predicate("Year", "l", String.valueOf(value), "double", "1"));
//                X.predicates.add(new Predicate("Rating", "l", String.valueOf(rValue), "double", "1"));
//                Xlist.add(X);
//                rValue++;
//            }
            value++;
        }
//        for (XVariable x : Xlist) {
//            System.out.println(x.predicates.get(0).value+"++++++++++"+ x.predicates.get(1).value);
//        }


        return Xlist;
    }

    public ArrayList<HashSet<DataNode>> enumerate3(Graph<DataNode, RelationshipEdge> pattern, Range range, String attr, String pnodeName) {

        DataNode curNode = null;
        for (DataNode node : pattern.vertexSet()) {
            if (node.isRoot) {
                curNode = node;
                System.out.println(node.preds.get(0).attr + node.preds.get(0).value);
                break;
            }
        }
        ArrayList<HashSet<DataNode>> sets = new ArrayList<>();
        double value = range.low;
        while (value < range.high) {

            curNode.preds.remove(0);


//            sets.add(execute(pattern, X));
            value = value + 1.0;

        }

        return sets;
    }

    public ArrayList<Graph<DataNode, RelationshipEdge>> enumerate2(Graph<DataNode,
            RelationshipEdge> pattern, Range range, String attr, String pnodeName) {

        DataNode curNode = null;
        ArrayList<Graph<DataNode, RelationshipEdge>> patterns = new ArrayList<>();
        double value = range.low;
        while (value < range.high) {

            Graph<DataNode, RelationshipEdge> curPattern =
                    new DefaultDirectedGraph<DataNode, RelationshipEdge>(RelationshipEdge.class);
            for (DataNode node : pattern.vertexSet()) {
                curPattern.addVertex(node);
            }

            for (RelationshipEdge edge : pattern.edgeSet()) {
                curPattern.addEdge(dataGraph.getEdgeSource(edge),
                        dataGraph.getEdgeTarget(edge), new RelationshipEdge(edge.getLabel()));
            }

            for (DataNode node : curPattern.vertexSet()) {
                if (node.getNodeName().equals(pnodeName)) {
                    System.out.println(node.getNodeName());
                    curNode = node;
                    break;
                }
            }
//            curNode.preds.get(0).value = String.valueOf(value);

            curNode.preds.remove(0);

            EdgeReversedGraph<DataNode, RelationshipEdge> revGraph = new EdgeReversedGraph<DataNode, RelationshipEdge>(curPattern);
            EdgeReversedGraph<DataNode, RelationshipEdge> graphCopy = new EdgeReversedGraph<DataNode, RelationshipEdge>(revGraph);

            patterns.add(graphCopy);
            value = value + 1.0;

        }

        for (Graph<DataNode, RelationshipEdge> p : patterns) {
            for (DataNode node : p.vertexSet()) {
                if (node.isRoot) {
                    System.out.println(node.preds.get(0).attr + node.preds.get(0).value);
                }
            }
        }
        System.out.println(patterns.size());

        return patterns;
    }

    public HashSet<DataNode> maxSumGen(ArrayList<String> group, ArrayList<Integer> cardinality, HashSet<DataNode> candidates) {

        System.out.println("Candidates Size:" + candidates.size());
        HashSet<DataNode> solution = new HashSet<>();
        HashSet<DataNode> dup = new HashSet<>();
        int bigcupP = 0;
        for (int i = 0; i < group.size(); i++) {
            String p = group.get(i);
            for (DataNode node : candidates) {
                if (!node.attributes.containsKey("Category")) {
                    continue;
                }
                if (node.attributes.get("Category").contains(p)) {
                    bigcupP++;
                }
            }

        }
        for (int i = 0; i < group.size(); i++) {

            String p = group.get(i);
            int c = cardinality.get(i);
            HashSet<DataNode> curGroup = new HashSet<>();
            for (DataNode node : candidates) {
                if (!node.attributes.containsKey("Category")) {
                    continue;
                }
                if (node.attributes.get("Category").contains(p) &&
                        !dup.contains(node)) {
                    dup.add(node);
                    curGroup.add(node);
                }
            }

            System.out.println("Group Size:" + curGroup.size());
            HashSet<DataNode> ri = new HashSet<>();

            for (int j = 0; j < c; j++) {
                DataNode temp = (DataNode) curGroup.toArray()[j];
                ri.add(temp);
            }

            for (DataNode node : ri) {
                curGroup.remove(node);
            }
            System.out.println("Group Size:" + curGroup.size());
            System.out.println("Select Size:" + ri.size());
            HashSet<DataNode> tempRiCopy;
            HashSet<DataNode> tempRi;


            tempRiCopy = new HashSet<>(ri);
            tempRi = new HashSet<>(ri);
            for (DataNode n : ri) {

                double curDiv = getDiv(tempRi);
//                System.out.println(tempRi.size());
//                System.out.println(getDiv(tempRi));
//                System.out.println(curDiv);
                HashSet<DataNode> tempCur = new HashSet<>(curGroup);
                for (DataNode m : tempCur) {

                    if (!tempCur.contains(m) || !tempRi.contains(n)) {
                        continue;
                    }

                    //System.out.println("cur: " + getDiv(tempRi));
                    if (((1 + (1 / bigcupP)) * curDiv) < getDiv(curDiv, tempRi, m, n)) {
                        tempRi.remove(n);
                        tempRi.add(m);
                        curGroup.remove(m);
//                        System.out.println(getDiv(curDiv,tempRi,m,n));
                    }

                }

            }


            System.out.println(tempRi.size() + "........................");
            solution.addAll(tempRi);

        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (DataNode node : solution) {

            if (!node.attributes.containsKey("Year")) {
                continue;
            }

            double variable = Double.parseDouble(((String) node.attributes.get("Year").toArray()[0]).substring(0, 4));
            System.out.println(variable);
            if (variable < min) {
                min = variable;

            }

            if (variable > max) {
                max = variable;
            }

        }

        System.out.println(min + "hAHA" + max);

        return solution;

    }

    private double getDiv(double curDiv, HashSet<DataNode> tempRi, DataNode add, DataNode re) {

        for (DataNode node : tempRi) {
            if (node.equals(re)) {
                continue;
            }
            int i = 0;
            double curSim = 0.0;
            for (String key : re.attributes.keySet()) {
                if (key.equals("Category")) {
                    continue;
                }
                if (node.attributes.containsKey(key)) {
                    i++;
                    curSim += (1 - JaccardDist((String) node.attributes.get(key).toArray()[0],
                            (String) re.attributes.get(key).toArray()[0]));
                }

            }

            curDiv -= (curSim / i);
        }

        for (DataNode node : tempRi) {
            if (node.equals(re)) {
                continue;
            }
            int i = 0;
            double curSim = 0.0;
            for (String key : add.attributes.keySet()) {
                if (node.attributes.containsKey(key)) {
                    i++;
                    curSim += (1 - JaccardDist((String) node.attributes.get(key).toArray()[0],
                            (String) add.attributes.get(key).toArray()[0]));
                }

            }

            curDiv += (curSim / i);
        }


        return curDiv;

    }


    private ArrayList<HashSet<DataNode>> greedySelection(ArrayList<HashSet<DataNode>> curSets) {

        int total = UnionOfSets(curSets).size();

        ArrayList<HashSet<DataNode>> res = new ArrayList<>();


        while (UnionOfSets(res).size() < total) {


            HashSet<DataNode> curBest = new HashSet<>();
            HashSet<DataNode> union = UnionOfSets(res);

            for (HashSet<DataNode> s : curSets) {
                if (s.size() == 0) {
                    continue;
                }
                double min = Double.MAX_VALUE;
                HashSet<DataNode> temp = new HashSet<>(union);
                int originalSize = temp.size();
                temp.addAll(s);
                if (temp.size() == originalSize) {
                    continue;
                }
                if (s.size() / (temp.size() - originalSize) < min) {
                    min = s.size() / (temp.size() - originalSize);
                    curBest = s;
                }

                temp.removeAll(s);

            }
            res.add(curBest);
            curSets.remove(curBest);
//            System.out.println("add");
        }

        System.out.println(res.size() + "greedy select Size");
        return res;
    }


    private HashSet<DataNode> getIntersection(HashSet<DataNode> s, HashSet<DataNode> rh) {

        HashSet<DataNode> intersect = new HashSet<>();
        for (DataNode node : s) {
            if (rh.contains(node)) {
                intersect.add(node);
            }
        }
        return intersect;
    }

    private HashSet<DataNode> UnionOfSets(ArrayList<HashSet<DataNode>> sets) {
        HashSet<DataNode> union = new HashSet<>();
        for (HashSet<DataNode> set : sets) {
            if (set == null) {
                continue;
            }
            union.addAll(set);
        }
        return union;
    }

    private int NoOfIntersection(HashSet<DataNode> set, HashSet<DataNode> red) {

        int count = 0;

        for (DataNode node : set) {
            if (red.contains(node)) {
                count++;
            }
        }

        return count;
    }

    private ArrayList<Graph<DataNode, RelationshipEdge>> evaluateGramiOutput(String path, HashMap<Integer, String> nodeLabelMap, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) throws IOException {

        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        ArrayList<Graph<DataNode, RelationshipEdge>> patterns = new ArrayList<>();
        String st;
        Graph<DataNode, RelationshipEdge> curGraph = null;
        while ((st = br.readLine()) != null) {
            String[] triples = st.split(" ");
            if (triples.length == 1 && triples[0].indexOf(":") == -1) {
                continue;
            }
            if (triples.length == 1 && triples[0].indexOf(":") != -1) {
                if (curGraph != null) {
                    patterns.add(curGraph);
                }
                curGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);
                continue;
            }

            if (triples[0].equals("v")) {
                System.out.println("add");
                DataNode node = new DataNode(triples[1]);
                node.types.add(nodeLabelMap.get(Integer.parseInt(triples[2])));
                curGraph.addVertex(node);
            }

            if (triples[0].equals("e")) {
                DataNode s = null;
                DataNode t = null;
                for (DataNode node : curGraph.vertexSet()) {
                    if (node.getNodeName().equals(triples[1])) {
                        s = node;
                        break;
                    }
                }
                for (DataNode node : curGraph.vertexSet()) {
                    if (node.getNodeName().equals(triples[2])) {
                        t = node;
                        break;
                    }
                }
                curGraph.addEdge(s, t, new RelationshipEdge("label"));

            }


        }
        System.out.println(patterns.size());
        for (Graph<DataNode, RelationshipEdge> g : patterns) {

            for (DataNode n : g.vertexSet()) {
                System.out.println(n.getNodeName() + " " + n.types);
            }
            for (RelationshipEdge e : g.edgeSet()) {
                System.out.print(g.getEdgeSource(e).getNodeName() + " ");
                System.out.print(g.getEdgeTarget(e).getNodeName() + " ");
                System.out.println(e.getLabel());
            }

            System.out.println("--------------------------");
        }

        return patterns;

    }

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        MovieDataGraph graph = new MovieDataGraph("C:\\Users\\Nick\\Downloads\\fairness\\newType.ttl",
                "C:\\Users\\Nick\\Downloads\\Film_dataset\\Film_dataset\\processed_dataset\\mix.dbpedia.graph");
        graph.enhencedIMDB("C:\\Users\\Nick\\Downloads\\Film_dataset\\Film_dataset\\processed_dataset\\film.imdb.json");

        VF2Checker checker = new VF2Checker(graph);

        ArrayList<String> group = new ArrayList<>();
        group.add("Comedy");
        group.add("Action");
//        group.add("Romance");
        ArrayList<Rangepair> cc = new ArrayList<>();
        cc.add(new Rangepair(20, 30));
        cc.add(new Rangepair(20, 30));
//        cc.add(new Rangepair(20, 40));

        long startTime = System.nanoTime();
//        checker.runMaxSumGenStreaming(group, cc, 1, 50);
        checker.runMaxSumGenStreaming(group, cc, 1, 50);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println(duration + "total");


    }


}

class TComparator implements Comparator<Triple> {

    // override the compare() method
    public int compare(Triple s1, Triple s2) {
        int t1 = Integer.parseInt(s1.s);
        int t2 = Integer.parseInt(s2.s);
        if (t1 == t2)
            return 0;
        else if (t1 > t2)
            return 1;
        else
            return -1;
    }

}