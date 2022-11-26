package Infra;

import java.util.*;

public class DataNode {

    private String nodeName;
    public HashMap<String,HashSet<String>> attributes = new HashMap<>();
    public HashSet<String> types;
    public boolean isRoot = false;
    public ArrayList<Predicate> preds;


    public DataNode(String nodeName) {

        this.nodeName = nodeName;
        preds = new ArrayList<>();
        types = new HashSet<>();

    }

    public String getNodeName() {
        return nodeName;
    }


}
