package Linkedin;

import Infra.*;
import MovieLensGraph.MovieDataGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.VF2AbstractIsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.EdgeReversedGraph;

import java.io.IOException;
import java.util.*;

public class VF2Checker {


    private Graph<DataNode, RelationshipEdge> dataGraph;
    private VF2AbstractIsomorphismInspector<DataNode, RelationshipEdge> inspector;
    private Comparator<DataNode> myNodeComparator;
    private Comparator<RelationshipEdge> myEdgeComparator;
    protected HashMap<Integer, DataNode> nodeMap;



    ArrayList<Graph<DataNode, RelationshipEdge>> res;
    double sumP;
    HashSet<DataNode> candidates;

    public VF2Checker(LinkedinGraph dataGraph) {

        this.dataGraph = dataGraph.getDataGraph();
        this.nodeMap = dataGraph.getNodeMap();

        res = new ArrayList<>();
        candidates = new HashSet<>();

        myEdgeComparator = new Comparator<RelationshipEdge>() {
            @Override
            public int compare(RelationshipEdge e1, RelationshipEdge e2) {

                if (e1.getLabel().equals(e2.getLabel())) {
                    return 0;
                } else {
                    return 1;
                }

            }

        };

        myNodeComparator = new Comparator<DataNode>() {
            @Override
            public int compare(DataNode n1, DataNode n2) {
                if (hashIntersection(n1.types, n2.types)) {
                    for (Predicate p : n2.preds) {
                        if (!staisfyP(p, n1)) {
                            return 1;
                        }
                    }
                    return 0;
                } else {
                    return 1;
                }
            }

        };

    }

    public void getInducedGraph(Graph<DataNode, RelationshipEdge> pattern) {

        DataNode curNode = null;
        for (DataNode node : pattern.vertexSet()) {
            if (node.isRoot) {
                curNode = node;
                break;
            }
        }
        HashSet<String> types = curNode.types;
        int m = 0;
        int f = 0;
        for (DataNode rootNode : dataGraph.vertexSet()) {
            if (!rootNode.attributes.containsKey("NOWork")) {
                continue;
            }
            double value = Double.parseDouble((String) rootNode.attributes.get("NOWork").toArray()[0]);
//            System.out.println(value);
            if (value <= 10) {
                continue;
            }
            if (!rootNode.attributes.containsKey("Industry")) {
                continue;
            }

            if (!rootNode.attributes.get("Industry").contains("Information Technology and Services")) {
                continue;
            }


            if (hashIntersection(rootNode.types, types)) {
                res.add(generateSubgraph(rootNode, 2));
                candidates.add(rootNode);

            }

        }

        System.out.println(res.size() + "glsize");

        //  generate initial induced graph to control candidate size.

    }

    private Graph<DataNode, RelationshipEdge> generateSubgraph(DataNode rootNode, int d) {
        Graph<DataNode, RelationshipEdge> curGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);
        Queue<DataNode> queue = new LinkedList<>();
        queue.add(rootNode);
        int level = 0;
        while (level <= d) {

            int size = queue.size();
            for (int i = 0; i < size; i++) {
                DataNode cur = queue.poll();
                if (!curGraph.containsVertex(cur)) {
                    curGraph.addVertex(cur);
                }

                for (RelationshipEdge edge : dataGraph.outgoingEdgesOf(cur)) {
                    queue.add(dataGraph.getEdgeTarget(edge));
                    if (!curGraph.containsVertex(dataGraph.getEdgeTarget(edge))) {
                        curGraph.addVertex(dataGraph.getEdgeTarget(edge));

                    }
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
        if (n1.attributes.get(p.attr).size() == 0) {
            return false;
        }
        if (p.attrType.equals("double")) {
            if (!n1.attributes.containsKey(p.attr)) {
                return false;
            }


            double variable = 0.0;
            String valueStr = (String) n1.attributes.get(p.attr).toArray()[0];
            if (p.attr.equals("NOwork")) {
                variable = Double.parseDouble(valueStr);
            } else if (p.attr.equals("Year") ) {
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



    public HashSet<DataNode> execute(Graph<DataNode, RelationshipEdge> pattern, XVariable X) {


        for (Predicate p : X.predicates) {
            for (DataNode pnode : pattern.vertexSet()) {
                if (pnode.getNodeName().equals(p.nodeName)) {
                    pnode.preds.add(p);
                }
            }
        }

        HashSet<DataNode> currentMatch = new HashSet<>();
        long startTime = System.nanoTime();
        for (Graph<DataNode, RelationshipEdge> cur : res) {

            inspector = new VF2SubgraphIsomorphismInspector<>(cur, pattern,
                    myNodeComparator, myEdgeComparator, false);


            if (inspector.isomorphismExists()) {

                Iterator<GraphMapping<DataNode, RelationshipEdge>> iterator = inspector.getMappings();
                ArrayList<DataNode> patternTypes = new ArrayList<>();
                DataNode rootNode = null;

                for (DataNode node : pattern.vertexSet()) {
                    patternTypes.add(node);
                }

                for (DataNode node : pattern.vertexSet()) {

                    if (node.isRoot = true) {
                        rootNode = node;
//                        System.out.println(node.types);
//                        System.out.println(node.preds.get(0).value);
                        break;
                    }
                }


                while (iterator.hasNext()) {
                    GraphMapping<DataNode, RelationshipEdge> mappings = iterator.next();
                    DataNode currentMatchedNode = mappings.getVertexCorrespondence(rootNode, false);
                    currentMatch.add(currentMatchedNode);

                }


            }
        }


      System.out.println("Match Size:" + currentMatch.size());

        return currentMatch;

    }

    public double getGroup(String label, ArrayList<String> group) {


        int count = 0;

        for (int i = 0; i < group.size(); i++) {

            for (DataNode node : candidates) {
                if (!node.types.contains(label)) {
                    continue;
                }
                String type = group.get(i);
                if (!node.attributes.containsKey("Gender")) {
                    continue;
                }
                if (node.attributes.get("Gender").contains(type)) {
                    count++;
                }
            }
        }

        return count;
    }

    public double getDelta(HashSet<DataNode> set, ArrayList<String> group, ArrayList<Integer> cc) {


        ArrayList<Integer> cMatch = new ArrayList<>();
        HashSet<DataNode> duplicate = new HashSet<>();

        System.out.println(set.size() + "Delta");

        for (int i = 0; i < group.size(); i++) {
            int count = 0;
            String type = group.get(i);
            for (DataNode node : set) {
                if (!node.attributes.containsKey("Gender")) {
                    continue;
                }
                if (node.attributes.get("Gender").contains(type) && !duplicate.contains(node)) {
                   duplicate.add(node);
                   count++;
                }

            }

            cMatch.add(count);
        }
        double dist = 0;
        for (int i = 0; i < cc.size(); i++) {
            dist += cMatch.get(i) - cc.get(i);
        }

        System.out.println(sumP + "Sump");
        return dist;
    }





    public void runMaxSumGen(Graph<DataNode, RelationshipEdge> pattern) {

        ArrayList<String> group = new ArrayList<>();
        group.add("male");
        group.add("female");
        //ArrayList<ArrayList<String>> result = cardinalityResult(currentMatch, group);
// identify groups
        ArrayList<Integer> cc = new ArrayList<>();
        cc.add(50);
        cc.add(50);
// identify cardinality

        HashSet<DataNode> solution = maxSumGen(group, cc, candidates);
        System.out.println("Size Sp: " + solution.size());
//
        HashSet<DataNode> blue = solution;

        HashSet<DataNode> red = new HashSet<>();

        for (DataNode node : candidates) {
            if (!blue.contains(node)) {
                red.add(node);
            }
        }
        System.out.println("Size Blue: " + blue.size());
        System.out.println("Size Red: " + red.size());


        ArrayList<HashSet<DataNode>> sets = enumerate1(pattern);

        System.out.println(sets.size() + "m size");

        int rsize = UnionOfSets(sets).size();
        System.out.println(rsize + "rsize");

        long startTime = System.nanoTime();

        //checker.runEnum(pattern,group,cc);


        for (int i = 1; i <= rsize; i++) {
//            System.out.println("Check" + i);
            ArrayList<HashSet<DataNode>> curRB = RBCover(sets, red, blue, i);
            if (curRB == null) {
                continue;
            }

            if (curRB.size() != 0) {
                System.out.println(i + "stop");
                System.out.println("RB Size" + curRB.size());
                ArrayList<HashSet<DataNode>> curRBCopy = new ArrayList<>(curRB);
                for (HashSet<DataNode> set : curRB) {
                    if (set == null) {
                        curRBCopy.remove(set);
                        continue;
                    }
                    //System.out.println(set.size() + "...");
                }
                System.out.println(sumP);
                System.out.println(getDelta(UnionOfSets(curRBCopy), group, cc) / sumP);
                break;
            }

        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println(duration + "RBCoverTime");

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
                        if ( n.attributes.get(key).toArray().length == 0 ||
                                m.attributes.get(key).toArray().length == 0) {
                            continue;
                        }
                        i++;
                        curDiv += (1 - JaccardDist((String) n.attributes.get(key).toArray()[0],
                                (String) m.attributes.get(key).toArray()[0]));
                    }

                }
                curDiv = curDiv / i;
                div += curDiv;
            }
        }
        return div;
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
        node.types.add("User");
        node.isRoot = true;

        DataNode node1 = new DataNode("2");
        node1.types.add("User");

        DataNode node2 = new DataNode("3");
        node2.types.add("User");

        pattern.addVertex(node);
        pattern.addVertex(node1);
//        pattern.addVertex(node2);
//        pattern.addVertex(node3);

        pattern.addEdge(node, node1, new RelationshipEdge("recommend"));
//        pattern.addEdge(node, node2, new RelationshipEdge("recommend"));
//        pattern.addEdge(node, node3, new RelationshipEdge("writer"));
//        pattern.addEdge(node, node4, new RelationshipEdge("producer"));
        return pattern;

    }



    public void getRange(String attr) {

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (DataNode node : dataGraph.vertexSet()) {

            if (!node.attributes.containsKey(attr)) {
                continue;
            }

            double variable = Double.parseDouble(((String) node.attributes.get(attr).toArray()[0]));
            if (variable < min) {
                min = variable;

            }

            if (variable > max) {
                max = variable;
            }

        }

        System.out.println(min + "<----->" + max);

    }

    public ArrayList<HashSet<DataNode>> enumerate1(Graph<DataNode, RelationshipEdge> pattern) {

        ArrayList<HashSet<DataNode>> sets = new ArrayList<>();
        ArrayList<XVariable> xList = enumerateAll();
        System.out.println(xList.size());
        for (XVariable x : xList) {
            System.out.println(x.predicates.get(0).value + "V");
            HashSet<DataNode> match = execute(pattern, x);
            if (match != null || match.size() != 0) {
                sets.add(match);
            }
        }

        return sets;
    }

    private ArrayList<XVariable> enumerateAll() {
        ArrayList<XVariable> Xlist = new ArrayList<>();

        HashSet<String> rate = new HashSet<>();
        for (DataNode node: candidates) {
            if ( !node.attributes.containsKey("Rating")) {
                continue;
            }
            String r = (String) node.attributes.get("Rating").toArray()[0];
            rate.add(r);
            System.out.println(r);
        }

        double value = 1990;


        while (value <= 2000) {

//            XVariable X = new XVariable();
//            X.predicates.add(new Predicate("Year", "l", String.valueOf(value), "double", "1"));
//            Xlist.add(X);

            double rValue = 4;
            while (rValue <= 8.0) {
                XVariable X = new XVariable();
                X.predicates.add(new Predicate("Year", "l", String.valueOf(value), "double", "1"));
                X.predicates.add(new Predicate("Rating", "l", String.valueOf(rValue), "double", "1"));
                Xlist.add(X);
                rValue++;
            }
            value++;
        }

        // generate all the query instances.
        return Xlist;
    }



    public HashSet<DataNode> maxSumGen(ArrayList<String> group, ArrayList<Integer> cardinality, HashSet<DataNode> candidates) {

        System.out.println("Candidates Size:" + candidates.size());
        HashSet<DataNode> solution = new HashSet<>();
        HashSet<DataNode> dup = new HashSet<>();
        int bigcupP = 0;
        for (int i = 0; i < group.size(); i++) {
            String p = group.get(i);
            for (DataNode node : candidates) {
                if (!node.attributes.containsKey("Gender")) {
                    continue;
                }
                if (node.attributes.get("Gender").contains(p)) {
                    bigcupP++;
                }
            }

        }
        for (int i = 0; i < group.size(); i++) {

            String p = group.get(i);
            int c = cardinality.get(i);
            HashSet<DataNode> curGroup = new HashSet<>();
            for (DataNode node : candidates) {
                if (!node.attributes.containsKey("Gender")) {
                    continue;
                }
                if (node.attributes.get("Gender").contains(p) &&
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

            HashSet<DataNode> tempRi = new HashSet<>(ri);
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
                    if (((1 + (1 / bigcupP)) * curDiv) < getDiv(curDiv,tempRi,m,n)) {
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
                if (key.equals("Gender")) {
                    continue;
                }
                if (node.attributes.containsKey(key)) {
                    if ( re.attributes.get(key).toArray().length == 0 ||
                            node.attributes.get(key).toArray().length == 0) {
                        continue;
                    }
                    i++;
                    curSim +=  (1 - JaccardDist((String) node.attributes.get(key).toArray()[0],
                            (String) re.attributes.get(key).toArray()[0]));
                }

            }

            curDiv -= (curSim/i);
        }

        for (DataNode node : tempRi) {
            if (node.equals(re)) {
                continue;
            }
            int i = 0;
            double curSim = 0.0;
            for (String key : add.attributes.keySet()) {
                if (node.attributes.containsKey(key)) {
                    if ( node.attributes.get(key).toArray().length == 0 ||
                            add.attributes.get(key).toArray().length == 0) {
                        continue;
                    }
                    i++;
                    curSim +=  (1 - JaccardDist((String) node.attributes.get(key).toArray()[0],
                            (String) add.attributes.get(key).toArray()[0]));
                }

            }

            curDiv += (curSim/i);
        }


        return curDiv;

    }

    public ArrayList<HashSet<DataNode>> RBCover
            (ArrayList<HashSet<DataNode>> curSets, HashSet<DataNode> red, HashSet<DataNode> blue, int bound) {

        ArrayList<HashSet<DataNode>> sets = new ArrayList<>(curSets);

        int n = sets.size();
        for (HashSet<DataNode> set : curSets) {
//                System.out.println(NoOfIntersection(set,red));
            if (NoOfIntersection(set, red) > bound) {
//                System.out.println(NoOfIntersection(set, red));
                sets.remove(set);
            }
        }

        if (NoOfIntersection(UnionOfSets(sets), blue) != blue.size()) {
//              System.out.println("Skip" + NoOfIntersection(UnionOfSets(sets), blue) + " " + blue.size());
            return null;
        }

        double Y = Math.sqrt((n / Math.log(blue.size())));

        HashSet<DataNode> RL = new HashSet<DataNode>();
        HashSet<DataNode> RH = new HashSet<DataNode>();

        for (DataNode rNode : red) {
            int count = 0;
            for (HashSet<DataNode> set : sets) {
                if (set.contains(rNode)) {
                    count++;
                }
            }
            if ((double) count > Y) {
                RH.add(rNode);
            } else {
                RL.add(rNode);
            }
        }
        HashMap<HashSet, HashSet> setMap = new HashMap<>();
        ArrayList<HashSet<DataNode>> SX = new ArrayList<>();
        System.out.println("RH" + RH.size());
        System.out.println("RL" + RL.size());

        for (HashSet<DataNode> s : sets) {
            HashSet<DataNode> intersect = getIntersection(s, RH);
            HashSet<DataNode> curSx = new HashSet<>(s);
            curSx.remove(intersect);
            setMap.put(curSx, s);
            SX.add(curSx);
        }

        ArrayList<HashSet<DataNode>> greedySet = greedySelection(SX);
        ArrayList<HashSet<DataNode>> recovered = new ArrayList<>();
        for (HashSet<DataNode> s : greedySet) {
            recovered.add(setMap.get(s));
        }
        return recovered;
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


    public static void main(String args[]) throws IOException {
        LinkedinGraph graph  = new LinkedinGraph();
        graph.loadLinkedGraph("C:\\Users\\Nick\\Downloads\\updated_nodes_with_gender.txt");

        VF2Checker checker = new VF2Checker(graph);
        checker.getRange("Year");
        Graph<DataNode, RelationshipEdge> pattern = checker.generatePattern();
        checker.getInducedGraph(pattern);

        ArrayList<String> group = new ArrayList<>();
        group.add("male");
        group.add("female");
        //ArrayList<ArrayList<String>> result = cardinalityResult(currentMatch, group);
//        ArrayList<Integer> cc = new ArrayList<>();
//        cc.add(50);
//        cc.add(50);

        checker.sumP = checker.getGroup("User", group);

//
//        checker.executeInit(pattern);

        long startTime = System.nanoTime();

        checker.runMaxSumGen(pattern);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println(duration + "total");

    }


}