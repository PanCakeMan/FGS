package Infra;

public class Predicate {

    public String attr;
    public String nodeName;
    public String op;
    public String value;
    public String attrType;

    public Predicate (String attr, String op, String value, String attrType,String nodeName) {
        this.attr = attr;
        this.op = op;
        this.value = value;
        this.attrType = attrType;
        this.nodeName = nodeName;
    }

}
