package tkg;

import java.io.IOException;

public class GMTest {

public static void main (String args[]) throws IOException, ClassNotFoundException {


    AlgoGSPAN algo = new AlgoGSPAN();
    algo.runAlgorithm("C:\\Users\\Nick\\Desktop\\imdb_binary_graph.txt","output.txt",0.1,false,false,5,false);


}

}
