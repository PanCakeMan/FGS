package Linkedin;

import java.io.*;
import java.util.*;

import Infra.*;

import MovieLensGraph.MovieDataGraph;
import MovieLensGraph.Rangepair;
import MovieLensGraph.Triple;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.isomorphism.VF2AbstractIsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.GraphMapping;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import tkg.AlgoGSPANLnkedin;

import java.util.Comparator;

public class VFlinkedin {


    private Graph<DataNode, RelationshipEdge> dataGraph;
    private VF2AbstractIsomorphismInspector<DataNode, RelationshipEdge> inspector;
    private Comparator<DataNode> myNodeComparator;
    private Comparator<RelationshipEdge> myEdgeComparator;
    private Comparator<RelationshipEdge> myEdgeComparator2;
    protected HashMap<Integer, DataNode> nodeMap;
    protected AlgoGSPANLnkedin algoGSPAN = new  AlgoGSPANLnkedin();
    public HashMap<Integer, String> nodeLabelMap;
    public HashMap<Integer, String> edgeLabelMap;
    Graph<DataNode, RelationshipEdge> mergedGraph;

    HashMap<String, HashSet<RelationshipEdge>> summaryMap;
    HashSet<DataNode> candidates;

    public VFlinkedin(LinkedinGraph dataGraph) {

        this.nodeLabelMap = dataGraph.getNodeLabelMap();
        this.edgeLabelMap = dataGraph.getEdgeLabelMap();
        this.dataGraph = dataGraph.getDataGraph();
        this.nodeMap = dataGraph.getNodeMap();
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
//            System.out.println(dataGraph.outgoingEdgesOf(node).size() + "++++++++");
//            System.out.println(curGraph.edgeSet().size());
//            System.out.println(res.size() + "-----------");
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
                int count = 0;

                for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(cur)) {
                    count++;
                    if (!curGraph.containsVertex(dataGraph.getEdgeTarget(edge))) {
                        curGraph.addVertex(dataGraph.getEdgeTarget(edge));

                    }
                    queue.add(dataGraph.getEdgeTarget(edge));
                    curGraph.addEdge(cur, dataGraph.getEdgeTarget(edge), new RelationshipEdge(edge.getLabel()));
                    if (count > 10) {
                        break;
                    }
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

                    if (node.types.contains("User")) {
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


    private HashSet<DataNode> streamGenSolution(HashSet<DataNode> solution, DataNode node, ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc, int r, int m) {

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


    public void runMaxSumGenStreaming(ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc, int r, int m)
            throws IOException, ClassNotFoundException {



        for (DataNode node : dataGraph.vertexSet()) {
            if (node.types.contains("Film")) {
                if (node.attributes.containsKey("Category")) {
                    HashSet<String> groups = new HashSet<>(group);
                    if (!node.attributes.containsKey("Category")) {
                        continue;
                    }
                    if (groups.contains(node.attributes.get("Category").toArray()[0])) {
                        candidates.add(node);
                    }
                }

            }
        }
        System.out.println(candidates.size() + "c size");
        HashMap<Integer, Graph<DataNode, RelationshipEdge>> resultPatterns
                = new HashMap<>();


        HashSet<DataNode> streamCand = new HashSet<>();
        ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList = new ArrayList<>();
        HashMap<Integer, HashSet<DataNode>> setCover = new HashMap<>();
        HashMap<Integer, HashSet<RelationshipEdge>> setSummary = new HashMap();
        int i = 0;
        boolean flag = false;
        long startTime1 = System.nanoTime();
        int sp = 0;
        for (DataNode node : candidates) {

//            System.out.println(i + "th");
            i++;
            if (dataGraph.degreeOf(node) == 0) {
                continue;
            }


            if (flag) {
                long startTimec = System.nanoTime();
                boolean canSkip = true;
                for (DataNode snode : streamCand) {
                    if (snode.types.toArray()[0].equals(node.types.toArray()[0])) {
                        if (getN(snode) > getN(node)) {
                            canSkip = false;

                            break;

                        }
                    }
                }
//
//
//
                if (!canSkip) {

                    DataNode minNode = null;
                    int min = Integer.MAX_VALUE;
                    for (DataNode snode : streamCand) {
                        if (getN(snode) < min) {
                            min = getN(snode);
                            minNode = snode;
                        }
                    }
                    HashSet<DataNode> toRemove = new HashSet<>();
                    toRemove.add(minNode);
                    HashSet<DataNode> toAdd = new HashSet<>();
                    toAdd.add(node);


                    ArrayList<Graph<DataNode, RelationshipEdge>> graphRm = getInducedGraph(toRemove, r);
                    ArrayList<Graph<DataNode, RelationshipEdge>> graphAdd = getInducedGraph(toAdd, r);

                    for (Graph<DataNode, RelationshipEdge> g : targetGraphList) {
                        g.removeAllEdges(graphRm.get(0).edgeSet());
                    }
                    for (int key : setSummary.keySet()) {
                        setSummary.get(key).remove(graphRm.get(0).edgeSet());
                    }
                    targetGraphList.add(graphAdd.get(0));

                    ArrayList<Graph<DataNode, RelationshipEdge>> curMiningCandidates = algoGSPAN.runAlgorithm(graphAdd, "streamOut1.txt", 0,
                            false, false, 7, false, nodeLabelMap, edgeLabelMap);


                    for (int l = 0; l < curMiningCandidates.size(); l++) {

                        if (l > 10) {
                            break;
                        }

                        HashMap<Integer, HashSet<RelationshipEdge>> curSetSum = new HashMap<>(setSummary);
                        HashSet<RelationshipEdge> curSum = getSummary(curMiningCandidates.get(l), targetGraphList);
                        for (int ki : curSetSum.keySet()) {
                            System.out.print(curSetSum.size() + "hahhah");
                            if (curSum.size() > curSetSum.get(ki).size()) {
                                System.out.println("Swap !!!!!");
                            }

                        }
                    }


                    long endTimec = System.nanoTime();
                    long durationc = (endTimec - startTimec);
                    System.out.println("Processing time " + durationc);


                    System.out.println("............");


                } else {
                    System.out.println("Skip node check");
                    sp++;
                    continue;
                }


            } else {


                HashSet<DataNode> temp = new HashSet<>(streamCand);
                temp.add(node);


                if (isExtenable(group, cc, temp, m)) {
                    streamCand.add(node);


                    if (getVio(streamCand, group, cc) == 0) {
                        System.out.println(i + "th");
                        System.out.println(streamCand.size() + "reach!");
                        flag = true;
                        targetGraphList = getInducedGraph(streamCand, r);

                        ArrayList<Graph<DataNode, RelationshipEdge>> miningCandidates = algoGSPAN.runAlgorithm(targetGraphList, "streamOut.txt", 0,
                                false, false, 5, false, nodeLabelMap, edgeLabelMap);
                        System.out.println(miningCandidates.size() + "p size");
                        setCover = new HashMap<>();
                        setSummary = new HashMap();
                        HashSet<RelationshipEdge> tSummary = new HashSet<>();
                        for (int j = 0; j < miningCandidates.size(); j++) {


                            HashSet<DataNode> curResult = execute(miningCandidates.get(j), targetGraphList);


                            if (curResult.size() == 0) {
                                continue;
                            }

                            System.out.println(curResult.size());
                            setCover.put(j, curResult);

                        }
                        System.out.println(setCover.keySet().size() + " cp size");

                        for (int s : setCover.keySet()) {

                            System.out.println(getSummary(miningCandidates.get(s), targetGraphList).size());
                            setSummary.put(s, getSummary(miningCandidates.get(s), targetGraphList));
                            tSummary.addAll(setSummary.get(s));
                        }
                        int k = 5;
                        HashSet<Integer> results = FairGenPattern(setSummary, setCover, group, cc, k, m);
                        for (int index : results) {
                            resultPatterns.put(index, miningCandidates.get(index));
                        }
                        System.out.println(results.size());
                        System.out.println(results);
//
                        int size = tSummary.size();
//
//
//
                        System.out.println();
//                       System.out.println(getVio(setCover, group, cc) + "vio");
//

                        HashSet<RelationshipEdge> allGraph = new HashSet<>();
                        for (int j = 0; j < targetGraphList.size(); j++) {
                            allGraph.addAll(targetGraphList.get(j).edgeSet());
                        }
                        int totalSize = allGraph.size();

                        System.out.println("graph total size " + totalSize);
//
                        System.out.println("summary total size " + size);
                        double ratio = 1.0 - ((double) size / totalSize);
                        System.out.println("ratio " + ratio);
                        long endTime1 = System.nanoTime();
                        long duration = (endTime1 - startTime1);
                        System.out.println("Initial " + duration);
//                       break;
                        //......

                    }


                } else {
                    System.out.println("skip");
                    continue;
                }

                continue;

            }
        }

        System.out.println(sp + "skipped");


//        long startTime = System.nanoTime();
////
////
//        ArrayList<Graph<DataNode, RelationshipEdge>> miningCandidates = algoGSPAN.runAlgorithm(targetGraphList, "output.txt", 0,
//                false, false, 5, false, nodeLabelMap, edgeLabelMap);
////
//        System.out.println(miningCandidates.size() + " cp size");
////
////
//        HashMap<Integer,HashSet<DataNode>> set = new HashMap<>();
//
//        for (int i = 0; i < miningCandidates.size(); i++) {
////            System.out.println(miningCandidates.get(i).edgeSet().size());
//            if (i > 10000) {
//                break;
//            }
//            long startTime1 = System.nanoTime();
//            HashSet<DataNode> curResult = execute(miningCandidates.get(i), targetGraphList);
//
//
//            long endTime1 = System.nanoTime();
//            long duration = (endTime1 - startTime1);
//            System.out.println("e time " + duration);
//
//
//
//            if (curResult.size() == 0) {
//                continue;
//            }
//
//
//            set.put(i,curResult);
//
////            cMtaches.add(curResult);
//
////            correctionSize.add(getCorrection(miningCandidates.get(i), targetGraphList));
//        }
//        System.out.println(set.keySet().size() + " cp size");
//
//
//
//
//
//        for (int i : set.keySet()) {
//            totalSummaryMap.put(i,getSummary(miningCandidates.get(i), targetGraphList));
//        }
//        int k = 20;
//
//        HashSet<Integer> results = FairGenPattern(totalSummaryMap, set, group, cc, k,m);
////
////        //...........
////
////        //.......
////
////
////        HashSet<DataNode> result = new HashSet<>();
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        HashSet<RelationshipEdge> totalSummary = new HashSet<>();
//        System.out.println(results.size() + " p size!");
////
//        for (int i : results) {
//            System.out.println(i + " in");
////            for (DataNode pNode : miningCandidates.get(i).vertexSet()) {
////                System.out.println(pNode.getNodeName());
////                System.out.println(pNode.types);
////            }
////            for (RelationshipEdge pEdge : miningCandidates.get(i).edgeSet()) {
////                System.out.println(miningCandidates.get(i).getEdgeSource(pEdge).getNodeName());
////                System.out.println(miningCandidates.get(i).getEdgeTarget(pEdge).getNodeName());
////                System.out.println(pEdge.getLabel());
////            }
//            totalSummary.addAll(totalSummaryMap.get(i));
//            System.out.println(set.get(i));
//
//        }
////
////
//        int size = 0;
//        for (int j = 0; j < targetGraphList.size(); j++) {
//            size += targetGraphList.get(j).edgeSet().size();
//        }
//        System.out.println();
//        System.out.println(getVio(set, group, cc) + "vio");
////
//        System.out.println("graph total size " + size);
//        int totalSize =  totalSummary.size();
////
//        System.out.println("summary total size " + totalSize);
//        double ratio = 1.0 - ((double) totalSize / size);
//        System.out.println("ratio " + ratio);
////
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
//        System.out.println(duration + "partial");
////
////        ArrayList<Graph<DataNode, RelationshipEdge>> patterns = evaluateGramiOutput("C:\\Users\\Nick\\Downloads\\GraMi-master\\Output.txt",
////                nodeLabelMap, targetGraphList);
////        HashSet<DataNode> cMtachesGrami = new HashSet<>();
////        for (int i = 0; i < patterns.size(); i++) {
////
////            HashSet<DataNode> curResult = execute2(patterns.get(i), targetGraphList);
////            cMtachesGrami.addAll(curResult);
////            System.out.println(curResult.size() + " Add");
////        }
////
////        int c1 = 0;
////        int c2 = 0;
////        for (DataNode n : cMtachesGrami) {
////            if (n.attributes.containsKey("Category")) {
////                continue;
////            }
////            if (n.attributes.get("Category").contains("Comedy")) {
////                c1++;
////            } else if (n.attributes.get("Category").contains("Action")) {
////                c2++;
////            }
////        }
////
////        System.out.println(c1 + " " + c2 + " Grami");


//        HashSet<DataNode> solution = new HashSet<>();
//        for (DataNode node : dataGraph.vertexSet()) {
//            long startTime = System.nanoTime();
//            if (node.types.contains("Film")) {
//                if (node.attributes.containsKey("Category")) {
//                    if (hashIntersection(node.attributes.get("Category"), new HashSet<>(group))) {
//                        HashSet<DataNode> updatedSolution = streamGenSolution(solution, node, group, cc, r, m);
//
//                        if (updatedSolution.size() == 0) {
//                            continue;
//                        }
//                        System.out.println("Number of Violations!!!!!!:" + getNOOfViolation(updatedSolution, group, cc));
//                        System.out.println("current solution size " + updatedSolution.size());
//
//                        DataNode newAdded = getSetMinus(solution, updatedSolution);
//                        ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList = getInducedGraph(updatedSolution, r);
//                        ArrayList<Graph<DataNode, RelationshipEdge>> miningCandidates = algoGSPAN.runAlgorithm(targetGraphList, "output.txt", 0,
//                                false, false, 4, false, nodeLabelMap, edgeLabelMap);
//                        ArrayList<HashSet<DataNode>> cMtaches = new ArrayList<>();
//                        HashSet<Integer> set = new HashSet<>();
//                        for (int i = 0; i < miningCandidates.size(); i++) {
//                            HashSet<DataNode> curResult = execute(miningCandidates.get(i), targetGraphList);
//                            set.add(i);
//                            cMtaches.add(curResult);
//                            if (i > 3000) {
//                                break;
//                            }
//                        }
//                        System.out.println(cMtaches.size() + " cp size");
//                        HashSet<Integer> setI = new HashSet<>();
//
//                        ArrayList<Integer> setSC = new ArrayList<>();
//
//                        for (int i = 0; i < miningCandidates.size(); i++) {
//                            setSC.add(getSummary(miningCandidates.get(i), targetGraphList).size());
//                        }
//
//                        while (setI.size() < 50) {
//                            int index = -1;
//                            int numM = -1;
//                            HashSet<Integer> curSet = set;
//                            for (int i : curSet) {
//                                int cs = setSC.get(i);
//                                if (numM < cs) {
//                                    index = i;
//                                    numM = cs;
//                                }
//                            }
//                            setI.add(index);
//                            set.remove(index);
//                        }
//                        HashSet<DataNode> result = new HashSet<>();
//                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                        HashSet<RelationshipEdge> totalSummary = new HashSet<>();
//                        for (int i : setI) {
////                            System.out.println(i + " in");
//                            HashSet<DataNode> curResult = cMtaches.get(i);
////                            System.out.println(curResult.size() + "cs");
//                            result.addAll(getIntersection(curResult, solution));
////                              for (DataNode pNode : miningCandidates.get(i).vertexSet()) {
////                                System.out.println(pNode.getNodeName());
////                                System.out.println(pNode.types);
////                            }
////                            for (RelationshipEdge pEdge : miningCandidates.get(i).edgeSet()) {
////                                System.out.println(miningCandidates.get(i).getEdgeSource(pEdge).getNodeName());
////                                System.out.println(miningCandidates.get(i).getEdgeTarget(pEdge).getNodeName());
////                                System.out.println(pEdge.getLabel());
////                            }
//
//                        }
//
//
//                        int size = 0;
//                        for (int j = 0; j < targetGraphList.size(); j++) {
//                            size += targetGraphList.get(j).edgeSet().size();
//                        }
//
//                        System.out.println("graph total size " + size);
//                        System.out.println("summary total size " + totalSummary.size());
//                        double ratio = (double) totalSummary.size() / size;
//                        System.out.println("ratio " + ratio);
//
//
//                        solution = updatedSolution;
//                    }
//
//                }
//
//            }
//            long endTime = System.nanoTime();
//            long duration = (endTime - startTime);
//            System.out.println(duration + " 1 processing!");
//        }


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

    private int getNOOfViolation(HashSet<DataNode> updatedSolution, ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc) {
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

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
    public void runMaxSumGen(ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc, int r, int m)
            throws IOException, ClassNotFoundException {

        long start = System.nanoTime();

        for (DataNode node : dataGraph.vertexSet()) {
            if (node.types.contains("User")) {
                if (node.attributes.containsKey("Gender")) {
                    HashSet<String> groups = new HashSet<>(group);
                    if (groups.contains(node.attributes.get("Gender").toArray()[0])) {

//                        if (node.attributes.containsKey("Industry")) {
//                            if (node.attributes.get("Industry").contains("Financial Services") ||
//                                    node.attributes.get("Industry").contains("Information Technology and Services")) {
//                                candidates.add(node);
//                            }
//                        }
                        candidates.add(node);
                        if (candidates.size() > 10000) {
                            break;
                        }
                    }

                }

            }
        }
//        HashMap<String,Integer> map = new HashMap<>();
//
//        for (DataNode node : candidates) {
//            System.out.println(node.attributes);
//            if (node.attributes.containsKey("Industry")) {
//                String key = (String) node.attributes.get("Industry").toArray()[0];
//                if (map.containsKey(key)) {
//                    int currI = map.get(key);
//                    map.put(key, currI+1);
//                } else {
//                    map.put(key,1);
//                }
//            }
//        }
//
//        HashMap<String,Integer> maps = sortByValue(map);
//        for (Map.Entry<String, Integer> en : maps.entrySet()) {
//            System.out.println("Key = " + en.getKey() +
//                    ", Value = " + en.getValue());
//        }


        System.out.println(candidates.size() + "c size");


        HashSet<DataNode> solution = FairGen(group, cc, candidates, m);
        System.out.println("Size Sp: " + solution.size());
        System.out.println(getObj2(solution, null) + " o value");
        System.out.println(getVio(solution, group, cc) + " vio value");

//        int c1 = 0;
//        int c2 = 0;
//        for (DataNode n : solution) {
//            System.out.println(n.attributes.get("Category"));
//            if (n.attributes.get("Gender").toArray()[0].equals(group.get(0))) {
//                c1++;
//                continue;
//            }
//            if (n.attributes.get("Gender").toArray()[0].equals(group.get(1))) {
//                c2++;
//                continue;
//            }
//        }
//        System.out.println(c1 + " " + c2);

        ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList = getInducedGraph(solution, r);
        ArrayList<Graph<DataNode, RelationshipEdge>> allGraphList = getInducedGraph(candidates, r);
       HashSet<RelationshipEdge> edges = new HashSet<>();
        for (Graph<DataNode, RelationshipEdge> g: targetGraphList) {
            edges.addAll(g.edgeSet());
        }

        System.out.println(edges.size()+ "+++++++++++");
//        OutputGraph(targetGraphList, solution, "outputSubgraphLinkedin.txt");
        OutputGraphGrami(allGraphList, "outputSubgraphLinkedin.txt");
//        for (DataNode node : solution) {
//            for (String key : node.attributes.keySet()) {
//                System.out.println(key + node.attributes.get(key));
//            }
//            System.out.println("--------------------------------");
//        }



//
//
        ArrayList<Graph<DataNode, RelationshipEdge>> miningCandidates = algoGSPAN.runAlgorithm(targetGraphList, "LinkediOutput.txt", 0,
                false, false, 6, false, nodeLabelMap, edgeLabelMap);
//
        System.out.println(miningCandidates.size() + " cp size");
//
        long endTime = System.nanoTime();
        long duration = (endTime - start);
        System.out.println(duration + " stotal");
//

        HashMap<Integer, HashSet<DataNode>> totalSetCover = new HashMap<>();
        HashMap<Integer, HashSet<RelationshipEdge>> totalSummaryMap = new HashMap<>();
        for (int i = 0; i < miningCandidates.size(); i++) {
//            System.out.println(miningCandidates.get(i).edgeSet().size());
            if (i > 10000) {
                break;
            }

            HashSet<DataNode> curResult = execute(miningCandidates.get(i), targetGraphList);
            if (curResult.size() == 0) {
                continue;
            }
            totalSetCover.put(i, curResult);
        }

        System.out.println(totalSetCover.keySet().size() + " cp size");


        for (int i : totalSetCover.keySet()) {
            totalSummaryMap.put(i, getSummary(miningCandidates.get(i), targetGraphList));
        }
        int k = 2;
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

        for (int j = 0; j < targetGraphList.size(); j++) {
            totalInducedGraph.addAll(targetGraphList.get(j).edgeSet());
        }
        System.out.println();
        System.out.println(getVio(totalSetCover, results, group, cc) + "vio");
//
        int size = totalInducedGraph.size();
        System.out.println("graph total size " + size);
        int totalSize = totalSummary.size();
//
        System.out.println("summary total size " + totalSize);
        double ratio = ((double) totalSize / size);
        System.out.println("ratio " + ratio);
//

        System.out.println(duration + "partial");
//
//        ArrayList<Graph<DataNode, RelationshipEdge>> patterns = evaluateGramiOutput("C:\\Users\\Nick\\Downloads\\GraMi-master\\Output.txt",
//                nodeLabelMap, targetGraphList);
//        HashSet<DataNode> cMtachesGrami = new HashSet<>();
//        for (int i = 0; i < patterns.size(); i++) {
//
//            HashSet<DataNode> curResult = execute2(patterns.get(i), targetGraphList);
//            cMtachesGrami.addAll(curResult);
//            System.out.println(curResult.size() + " Add");
//        }
//
//        int c1 = 0;
//        int c2 = 0;
//        for (DataNode n : cMtachesGrami) {
//            if (n.attributes.containsKey("Category")) {
//                continue;
//            }
//            if (n.attributes.get("Category").contains("Comedy")) {
//                c1++;
//            } else if (n.attributes.get("Category").contains("Action")) {
//                c2++;
//            }
//        }
//
//        System.out.println(c1 + " " + c2 + " Grami");


    }

    private int getVio(HashMap<Integer, HashSet<DataNode>> set, HashSet<Integer> results, ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc) {

        int vio = 0;


        for (int i = 0; i < group.size(); i++) {
            int cnt = 0;
            for (int j : results) {

                for (DataNode node : set.get(j)) {
                    if (!node.attributes.containsKey("Category")) {
                        continue;
                    }
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

    private int getVio(HashSet<DataNode> set, ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc) {

        int vio = 0;
        for (int i = 0; i < group.size(); i++) {
            int cnt = 0;
            for (DataNode node : set) {
                if (node.attributes.get("Gender").toArray()[0].equals(group.get(i))) {
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
                                            ArrayList<MovieLensGraph.Rangepair> cc, int k, int m) {


        HashSet<Integer> S = new HashSet<>();

        System.out.println(setCover.size() + " candidate pattern size");
        HashSet<Integer> indexSet = new HashSet<>();
        for (int index : setCover.keySet()) {
            indexSet.add(index);
        }

        System.out.println(indexSet + "total size");


        while (S.size() < k) {

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
                System.out.println(maxIndex + " add!");
            } else {
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

    private boolean isExtenableP(ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc, HashSet<Integer> temp,
                                 int m, HashMap<Integer, HashSet<DataNode>> setCover) {

        HashMap<Integer, Integer> cs = new HashMap<>();
        for (int i = 0; i < group.size(); i++) {
            cs.put(i, 0);
        }

        for (int i = 0; i < group.size(); i++) {

            for (int index : temp) {
                HashSet<DataNode> curMatch = setCover.get(index);
                for (DataNode node : curMatch) {
                    if (node == null) {
                        continue;
                    }
                    if (node.attributes.get("Gender").toArray()[0].equals(group.get(i))) {
                        int curCount = cs.get(i);
                        cs.put(i, curCount + 1);
                    }
                }
            }
        }

//        System.out.println(cs);
        boolean flag = true;
        for (int i = 0; i < group.size(); i++) {
            if (cs.get(i) > cc.get(i).u) {
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


//    private void OutputGraphGrami(ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList, HashSet<DataNode> solution) throws IOException {
//        int i = 0;
//
//        HashMap<Integer, String> map = new HashMap();
//        ArrayList<Triple> triples = new ArrayList<>();
//        BufferedWriter writer = new BufferedWriter(new FileWriter("currentGraphGrami.txt"));
//        HashSet<Integer> sl = new HashSet<>();
//        HashMap<String, DataNode> nodeMap = new HashMap<>();
//        HashMap<DataNode, String> reverseNodeMap = new HashMap<>();
//        HashMap<String, String> nodeLabelMap = new HashMap<>();
//
//        int nodeId = 0;
//        int nodeLabelId = 0;
//        HashSet<DataNode> dup = new HashSet<>();
//        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
//            for (DataNode node : graph.vertexSet()) {
//                if (dup.contains(node)) {
//                    continue;
//                }
//                nodeMap.put(String.valueOf(nodeId), node);
//                reverseNodeMap.put(node, String.valueOf(nodeId));
//                nodeId++;
//                if (!nodeLabelMap.containsKey(node.types.toArray()[0])) {
//                    nodeLabelMap.put((String) node.types.toArray()[0], String.valueOf(nodeLabelId));
//                    nodeLabelId++;
//                }
//                dup.add(node);
//
//            }
//        }
//        writer.write("# t 1" + "\n");
//        for (int j = 0; j < nodeId; j++) {
//            writer.write("v" + " " + j + " " + nodeMap.get(String.valueOf(j)).types.toArray()[0].hashCode() + "\n");
//        }
//
//
//        HashSet<Integer> set = new HashSet<>();
//        for (Graph<DataNode, RelationshipEdge> graph : targetGraphList) {
//            for (RelationshipEdge edge : graph.edgeSet()) {
//
//                int hash1 = graph.getEdgeSource(edge).getNodeName().hashCode() * 33 + graph.getEdgeTarget(edge).getNodeName().hashCode();
//                int hash2 = graph.getEdgeTarget(edge).getNodeName().hashCode() * 33 + graph.getEdgeSource(edge).getNodeName().hashCode();
//                if (set.contains(hash1)) {
//                    continue;
//                }
//                if (set.contains(hash2)) {
//                    continue;
//                }
//                set.add(hash1);
//                set.add(hash2);
//
//                String s = reverseNodeMap.get(graph.getEdgeSource(edge));
//                String t = reverseNodeMap.get(graph.getEdgeTarget(edge));
//                writer.write("e" + " " + s + " " + t + " " + edge.getLabel().hashCode() + "\n");
//            }
//
//        }
//
//
//        for (int j = 0; j < triples.size(); j++) {
//
//            writer.write(triples.get(j).s);
//            writer.write("\t");
//            writer.write(triples.get(j).t);
//            writer.write("\t");
//            writer.write(1 + "\n");
//
//        }
//
//
//        writer.close();
//        System.out.print(sl + "sssssssssssssssssssssssssssssssssssssssssssss");
//
//
//    }

    private void OutputGraph(ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList, HashSet<DataNode> solutions, String path) throws IOException {

        int i = 0;
        HashMap<Integer, String> map = new HashMap();
        ArrayList<MovieLensGraph.Triple> triples = new ArrayList<>();
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

    private HashSet<RelationshipEdge> getSummary(Graph<DataNode, RelationshipEdge> pattern, ArrayList<Graph<DataNode, RelationshipEdge>> targetGraphList) {


        HashSet<RelationshipEdge> summary = new HashSet<>();
        for (RelationshipEdge edge : pattern.edgeSet()) {
            DataNode s = pattern.getEdgeSource(edge);
            DataNode t = pattern.getEdgeTarget(edge);
            String key = (String) s.types.toArray()[0] + (String) t.types.toArray()[0] + edge.getLabel();
            HashSet<RelationshipEdge> csummary;
//            System.out.println(key);
            if (summaryMap.containsKey(key)) {
                csummary = summaryMap.get(key);
                summary.addAll(csummary);

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

    private HashSet<DataNode> FairGen(ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc, HashSet<DataNode> candidates, int m) {

        HashSet<DataNode> S = new HashSet<>();
        HashSet<DataNode> dup = new HashSet<>();
        System.out.println(candidates.size());
        HashSet<DataNode> cansub = new HashSet<>();
        for (DataNode n : candidates) {
            cansub.add(n);
//            if (cansub.size() > 10000) {
//                break;
//            }
        }

        while (S.size() < m) {
            HashSet<DataNode> U = new HashSet<>();
            for (DataNode e : cansub) {
                if (!e.attributes.containsKey("Gender")) {
                    continue;
                }
                if (S.contains(e)) {
                    continue;
                }
                boolean flag = false;
                for (String g : group) {
                    if (e.attributes.get("Gender").contains(g)) {
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


    private boolean isExtenable(ArrayList<String> group, ArrayList<MovieLensGraph.Rangepair> cc, HashSet<DataNode> temp, int m) {


        ArrayList<Integer> cm = new ArrayList<>();

        for (int i = 0; i < group.size(); i++) {
            int count = 0;
            String[] gs = group.get(i).split(":");
            for (DataNode node : temp) {
                if (!node.attributes.containsKey("Gender")||!node.attributes.containsKey("Industry")) {
                    continue;
                }
                if (node.attributes.get("Gender").toArray()[0].equals(group.get(i))) {
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
        System.out.println(cm);
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

    public void motivation () throws IOException {

        Graph<DataNode, RelationshipEdge> maleGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);
        int i = 0;
        int j = 0;
        for (DataNode node : dataGraph.vertexSet()) {
            if (!node.attributes.containsKey("Industry")) {
                continue;
            }
            //System.out.println(node.attributes.get("Industry").toArray()[0]);

            if (!node.attributes.get("Industry").toArray()[0].equals("Internet")) {
                continue;
            }

            if (node.attributes.containsKey("Gender")) {
                System.out.println(node.attributes.get("Gender").toArray()[0]);
                if (node.attributes.get("Gender").toArray()[0].equals("male")) {
                    j++;
                } else {
                    i++;
                }

            }
        }



        System.out.println(i);
        System.out.println(j);


//        for (RelationshipEdge edge : dataGraph.edgeSet()) {
//
//            DataNode s = dataGraph.getEdgeSource(edge);
//            DataNode t = dataGraph.getEdgeTarget(edge);
//            if (!t.attributes.containsKey("Gender") || !s.attributes.containsKey("Gender")){
//                continue;
//            }
////            if (s.attributes.get("Gender").toArray()[0].equals("female") && t.attributes.get("Gender").toArray()[0].equals("female") ) {
//                if (!maleGraph.containsVertex(s)) {
//                    maleGraph.addVertex(s);
//                }
//                if (!maleGraph.containsVertex(t)) {
//                    maleGraph.addVertex(t);
//                }
//               maleGraph.addEdge(s,t, new RelationshipEdge(edge.getLabel()));
//                if (maleGraph.edgeSet().size() > 10000) {
//                    break;
//                }
////           }
//
//
//        }
//        int m = 0;
//        int f = 0;
//        int mc = 0;
//        int fc = 0;
//        for (DataNode node : dataGraph.vertexSet()) {
//            if (!node.attributes.containsKey("Gender")){
//                continue;
//            }
//            if (!node.attributes.containsKey("Industry")){
//                continue;
//            }
//            if (node.attributes.get("Industry").toArray()[0].equals("Marketing and Advertising")){
//                f++;
//                if (node.attributes.get("Gender").toArray()[0].equals("female")) {
//
//                      fc++;
//                }
//                if (node.attributes.get("Gender").toArray()[0].equals("male")) {
//
//                    mc++;
//                }
//            }
//
//
////            if (node.attributes.get("Gender").toArray()[0].equals("male")) {
////                if (!node.attributes.containsKey("Year")){
////                    continue;
////                }
////
////                m += Integer.parseInt((String) node.attributes.get("Year").toArray()[0]);
////                if (Integer.parseInt((String) node.attributes.get("Year").toArray()[0]) == 10) {
////                    mc++;
////                }
////            }
//
//
//        }
//        System.out.println(f + " " + fc + " " + " " + m + " " + mc);
//        //System.out.println(f/fc + " " + m/mc);
//
//
////        System.out.println(maleGraph.edgeSet().size() + "edge size");
////        ArrayList<Graph<DataNode, RelationshipEdge>> graphList = new ArrayList<>();
////        graphList.add(maleGraph);
////        OutputGraphGrami(graphList, "female.txt");


    }

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        LinkedinGraph graph = new LinkedinGraph();
        graph.loadLinkedGraph("C:\\Users\\Nick\\Downloads\\updated_nodes_with_gender.txt");
        VFlinkedin checker = new VFlinkedin(graph);
        checker.motivation();

//        ArrayList<String> group = new ArrayList<>();
//        group.add("male");
//        group.add("female");
////
//
//        ArrayList<MovieLensGraph.Rangepair> cc = new ArrayList<>();
//        cc.add(new MovieLensGraph.Rangepair(20, 30));
//        cc.add(new Rangepair(20, 30));
//
//        long startTime = System.nanoTime();
//        checker.runMaxSumGen(group, cc, 2, 50);
//
//
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
//        System.out.println(duration + "total");


    }



}