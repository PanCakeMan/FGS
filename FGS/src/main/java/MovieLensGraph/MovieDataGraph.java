package MovieLensGraph;

import Infra.DataGraphBase;
import Infra.DataNode;
import Infra.RelationshipEdge;
import org.apache.jena.rdf.model.*;

import org.json.*;

import java.io.*;
import java.util.*;

public class MovieDataGraph extends DataGraphBase {

    public HashMap<Integer, String> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public HashMap<Integer, String> getEdgeLabelMap() {
        return edgeLabelMap;
    }

    public HashMap<Integer, String> nodeLabelMap;
    public HashMap<Integer, String> edgeLabelMap;

    public MovieDataGraph(String nodeTypesFilePath, String dataGraphFilePath, String enrichFile) throws IOException {
        super();
        nodeLabelMap = new HashMap<>();
        edgeLabelMap = new HashMap<>();
        loadNodeMap(nodeTypesFilePath);
        addAllVertex()  ;
        loadGraph(dataGraphFilePath);
        enhencedIMDB(enrichFile);
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


    private void loadNodeMap(String nodeTypesFilePath) throws IOException {

        if (nodeTypesFilePath == null || nodeTypesFilePath.length() == 0) {
            System.out.println("No Input Node Types File Path!");
            return;
        }
        System.out.println("Start Loading DBPedia Node Map...");

        Model model = ModelFactory.createDefaultModel();
        System.out.println("Loading Node Types...");
        model.read(nodeTypesFilePath);
        StmtIterator typeTriples = model.listStatements();


        while (typeTriples.hasNext()) {

            Statement stmt = typeTriples.nextStatement();
            String subject = stmt.getSubject().getURI();

            if (subject.length() > 28) {
                subject = subject.substring(28);
            }

            DataNode dataNode;

            String object = stmt.getObject().asResource().getLocalName();
//            System.out.println(stmt.getObject().asResource().getNameSpace()+ "++++++++++++++++++++++");

            int nodeId = subject.hashCode();

            if (!nodeMap.containsKey(nodeId)) {
                dataNode = new DataNode(subject);
                dataNode.types.add(object);


                nodeMap.put(nodeId, dataNode);
                //System.out.println(subject + " " + object);

            } else {
                nodeMap.get(nodeId).types.add(object);
            }
        }
        for (DataNode node : nodeMap.values()) {
          String type = (String) node.types.toArray()[0];
            if (!nodeLabelMap.containsKey(type)) {
                nodeLabelMap.put(type.hashCode(), type);
            }
        }


        System.out.println("Done Loading DBPedia Node Map!!!");
        System.out.println("DBPedia NodesMap Size: " + nodeMap.size());


    }

    private void loadGraph(String dataGraphFilePath) throws IOException {

        if (dataGraphFilePath == null || dataGraphFilePath.length() == 0) {
            System.out.println("No Input Graph Data File Path!");
            return;
        }

        System.out.println("Loading DBPedia Graph...");

        File file = new File(dataGraphFilePath);
        File file1 = new File(dataGraphFilePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedReader br1 = new BufferedReader(new FileReader(file1));

        String st;
        String st1;
        HashMap<String, Integer> count1 = new HashMap<>();
        HashMap<String, Integer> count2 = new HashMap<>();


        int i = 0;

        while ((st = br.readLine()) != null) {

            i++;

            String[] triples = st.split("\t");

            String subject = triples[0];
            String predicate = triples[1];
            String object = triples[2];

            String subjectString = subject.substring(1, triples[0].length() - 1);
            String objectString = object.substring(1, triples[2].length() - 1);

            if (predicate.equals("<abstract>") || predicate.equals("<substract>")
                    || predicate.equals("<alias>") || predicate.equals("<digitalChannel>")
                    || predicate.equals("<militaryCommand>")) {
                continue;
            }

            if (subjectString.contains("Category:") || objectString.contains("Category:")) {
                //System.out.println("Cate Detect!");
                continue;
            }

            if (predicate.equals("<country>") || predicate.equals("<genre>") || predicate.equals("<birthPlace>")) {
                if (!nodeMap.containsKey(subjectString.hashCode())) {
                    //System.out.println("No Add");
                    continue;
                }


                DataNode curr = nodeMap.get(subjectString.hashCode());

                if (!curr.attributes.containsKey(predicate.substring(1, predicate.length() - 1))) {

                    HashSet<String> newSet = new HashSet<>();
                    newSet.add(objectString);
                    curr.attributes.put(predicate.substring(1, predicate.length() - 1), newSet);

                } else {
                    curr.attributes.get(predicate.substring(1, predicate.length() - 1)).add(objectString);
                }


//                System.out.println("Add");
                //System.out.println(predicate.substring(1,predicate.length()-1));
                //System.out.println(nodeMap.get(subjectString.hashCode()).getType());
                //System.out.println(nodeMap.get(subjectString.hashCode()).getNodeName());
                //System.out.println("Add");

            }


            if (object.charAt(0) != '<') {
                if (object.indexOf('\"') >= object.lastIndexOf('\"')) {
                    continue;
                }

//                System.out.println(object);
//                System.out.println(object.indexOf('\"') + " " + object.lastIndexOf('\"'));
                String attribute = object.substring(object.indexOf('\"') + 1, object.lastIndexOf('\"'));
//                System.out.println(attribute);
                if (!nodeMap.containsKey(subjectString.hashCode())) {
                    continue;
                }
                DataNode curr = nodeMap.get(subjectString.hashCode());

                if (!curr.attributes.containsKey(predicate.substring(1, predicate.length() - 1))) {

                    HashSet<String> newSet = new HashSet<>();
                    newSet.add(attribute);
                    curr.attributes.put(predicate.substring(1, predicate.length() - 1), newSet);

                } else {
                    curr.attributes.get(predicate.substring(1, predicate.length() - 1)).add(attribute);
                }
//                System.out.println("Add attr!");
            }

        }
//        System.out.println(i + " haha");
//        Map<String, Integer> m1 = sortByValue(count1);
//        printMap(m1);
//        System.out.println("-----------------------------");
//        Map<String, Integer> m2 = sortByValue(count2);
//        printMap(m2);
//        System.out.println(count1.size() + " " + count2.size());

        br.close();

        int count = 0;
        int count4 = 0;


        while ((st1 = br1.readLine()) != null) {

            String[] triples = st1.split("\t");
            String subject = triples[0];
            String predicate = triples[1];
            String object = triples[2];
            String subjectString = triples[0].substring(1, triples[0].length() - 1);
            String predicateString = triples[1].substring(1, triples[1].length() - 1);
            String objectString = triples[2].substring(1, triples[2].length() - 1);

            if (predicate.equals("<abstract>")) {
                continue;
            }
            //System.out.println(object);

            if (object.charAt(0) == '<') {

                if (!nodeMap.containsKey(subjectString.hashCode())) {
                    //System.out.println(subjectString + "1------");

                    count++;

                    continue;
                }

                if (!nodeMap.containsKey(objectString.hashCode())) {
                    //System.out.println(objectString + "----!");
                    count4++;

                    continue;

                }

                DataNode snode = nodeMap.get(subjectString.hashCode());
                DataNode tnode = nodeMap.get(objectString.hashCode());
                dataGraph.addVertex(snode);
                dataGraph.addVertex(tnode);
                dataGraph.addEdge(snode, tnode, new RelationshipEdge(predicateString));
                if (!edgeLabelMap.containsKey(predicateString.hashCode())) {
                    edgeLabelMap.put(predicateString.hashCode(), predicate);
                }
            }

        }
        br1.close();
        System.out.println("MissingS!" + count);
        System.out.println("MissingT!" + count4);
        System.out.println("Number of Edges: " + dataGraph.edgeSet().size());
        System.out.println("Number of Nodes: " + dataGraph.vertexSet().size());
        System.out.println("Done Loading DBPedia Graph!!!");

    }


    private void cleanNodeMap(String nodeTypesFilePath, String filePath) throws IOException {

        if (nodeTypesFilePath == null || nodeTypesFilePath.length() == 0) {
            System.out.println("No Input Node Types File Path!");
            return;
        }
        System.out.println("Start Loading DBPedia Node Map...");


        File file = new File(filePath);

        BufferedReader br = new BufferedReader(new FileReader(file));
        HashSet<String> set = new HashSet<>();

        String st;

        while ((st = br.readLine()) != null) {

            String[] triples = st.split("\t");

            String sub = triples[0];
            String subject = sub.substring(1, sub.length() - 1);
            set.add(subject);

            String obj = triples[2];
            if (obj.charAt(0) == '<') {
                String object = obj.substring(1, obj.length() - 1);
                set.add(object);

            }
        }
        System.out.println(set.size() + "total size!");

        br.close();


        Model model = ModelFactory.createDefaultModel();
        System.out.println("Loading Node Types...");
        model.read(nodeTypesFilePath);
        StmtIterator typeTriples = model.listStatements();

        File newType = new File("newType.ttl");
        BufferedWriter writer = new BufferedWriter(new FileWriter(newType));


        while (typeTriples.hasNext()) {

            Statement stmt = typeTriples.nextStatement();
            String sub = stmt.getSubject().getURI();

            if (sub.length() > 28) {
                sub = sub.substring(28);
            }
            if (!set.contains(sub)) {
                continue;
            }

            String url = stmt.getObject().asResource().getNameSpace();

            if (url.equals("http://dbpedia.org/ontology/")) {
                writer.write("<" + stmt.getSubject().toString() + ">" + " " + "<" + stmt.getPredicate().toString() + ">" + " " + "<" + stmt.getObject() + ">" + " .");
                writer.write("\n");
            }

        }
        writer.close();
        System.out.println("Done");

    }

    public void enhencedIMDB(String jsonFile) throws IOException {

        File file = new File(jsonFile);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        int i = 0;
        int j = 0;
        while ((st = br.readLine()) != null) {

            if (st.trim().length() == 0) {
                continue;
            }


            JSONObject obj = new JSONObject(st);
            if (!obj.has("Title")) {
                continue;
            }
            i++;
            String title = obj.getString("Title");
            String newTitle = title.trim().replaceAll(" ", "_");
//            System.out.println(newTitle);

            if (nodeMap.containsKey(newTitle.hashCode())) {
                j++;

                if (!obj.get("imdbRating").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    set.add(obj.getString("imdbRating"));
                    nodeMap.get(newTitle.hashCode()).attributes.put("Rating", set);
                }

                if (!obj.get("Rated").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    set.add(obj.getString("Rated"));
                    nodeMap.get(newTitle.hashCode()).attributes.put("Rated", set);
                }
                if (!obj.get("Genre").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    String[] genres = obj.getString("Genre").split(",");
                    for (String g : genres) {
                        set.add(g.trim());
                    }
                    nodeMap.get(newTitle.hashCode()).attributes.put("Category", set);
                }
                if (!obj.get("Language").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    String[] lang = obj.getString("Language").split(",");
                    for (String l : lang) {
                        set.add(l.trim());
                    }
                    nodeMap.get(newTitle.hashCode()).attributes.put("Language", set);
                }
                if (!obj.get("Country").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    set.add(obj.getString("Country"));
                    nodeMap.get(newTitle.hashCode()).attributes.put("Nation", set);
                }
                if (!obj.get("Year").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    set.add(obj.getString("Year"));
                    nodeMap.get(newTitle.hashCode()).attributes.put("Year", set);
                }
                if (!obj.get("Awards").equals("N/A")) {
                    HashSet<String> set = new HashSet<>();
                    set.add(obj.getString("Awards"));
                    nodeMap.get(newTitle.hashCode()).attributes.put("Awards", set);
                }

            }


        }
//        System.out.println(i + " " + j);

    }

    public HashSet<String> GMM (ArrayList<String> originalAnswer) {
        HashSet<String> original = new HashSet<String>(originalAnswer);
        System.out.println(original.size() + "???????");
        String[] array = original.toArray(new String[original.size()]);
        int j = 0;

        // generate a random number
        Random rndm = new Random();

        // this will generate a random number between 0 and
        // HashSet.size - 1
        int rndmNumber = rndm.nextInt(array.length);
        HashSet<String> S = new HashSet<>();
        S.add(array[rndmNumber]);

        while (S.size() < 10) {
            double max = -1;
            String nodeName = "";

            for (DataNode n : dataGraph.vertexSet()) {
                if (!n.types.contains("Film") || S.contains(n.getNodeName())) {
                    continue;
                }
                if (!n.attributes.containsKey("Category")) {
                    continue;
                }
                if (!n.attributes.get("Category").contains("Comedy")) {
                    continue;
                }
                double min = Double.MAX_VALUE;
                for (String node : S) {
                    double d = 1 - Dist(n, nodeMap.get(node.hashCode())) * 0.99;
                    double cov1 = 0;
                    if (original.contains(n.getNodeName())) {
                        cov1 += 0.5;
                        System.out.println("++++++");
                    }
                    if (original.contains(n.getNodeName())) {
                        cov1 += 0.5;
                        System.out.println("++++++");
                    }
                    d += cov1 * 0.01;

                    if (d < min) {
                        min = d;
                    }
                }
                if (min > max) {
                    max = min;
                    nodeName = n.getNodeName();
                }
            }
            System.out.println(max);

            S.add(nodeName);

        }
        System.out.println(S + "++++++");
        int count = 0;

        for (String s : S) {
            if (original.contains(s)) {
                count++;
            }
        }
        System.out.println(count);

        return S;
    }

    private double Dist(DataNode n, DataNode m) {

//        if(m == null || n == null) {
//            return 0;
//        }

        int i = 0;
        double result = 0;
        for (String key : n.attributes.keySet()) {
            if (m.attributes.containsKey(key)) {
                i++;
                result += JaccardDist((String) n.attributes.get(key).toArray()[0],
                        (String) m.attributes.get(key).toArray()[0]);
            }


        }

        return result / i;
    }

    private double JaccardDist(String str1, String str2) {
        Set<Character> s1 = new HashSet<>();//set elements cannot be repeated
        Set<Character> s2 = new HashSet<>();

        for (int i = 0; i < str1.length(); i++) {
            s1.add(str1.charAt(i));//Put the elements in string into the set collection by index one by one
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
//        System.out.println(jaccard);
        return jaccard;
    }









    public static void main(String args[]) throws IOException {

        MovieDataGraph dataGraph = new MovieDataGraph("newType.ttl",
                "C:\\Users\\Nick\\Downloads\\Film_dataset\\Film_dataset\\processed_dataset\\mix.dbpedia.graph",
                "C:\\Users\\Nick\\Downloads\\Film_dataset\\Film_dataset\\processed_dataset\\film.imdb.json");
        System.out.println(dataGraph.nodeLabelMap.size());
        System.out.println(dataGraph.edgeLabelMap.size());



    }

}


