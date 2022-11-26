# Repository of  Fair Group Summarization with Graph Patterns

Dataset  

Covid-19: https://github.com/imdevskp/covid-19-india-data/  

Movie: https://www2.helsinki.fi/en/researchgroups/unified-database-management-systems-udbm  
DBpedia:https://www.dbpedia.org/resources/knowledge-graphs/
* To identify node types, we integrate DBpedia Ontology for building movie data graph.
Please see: http://wikidata.dbpedia.org/services-resources/ontology

Linkedin: Cosnet: Connecting heterogeneous social networks with local and global consist  


Test Case example (DBpedia Movie):

Call Procedure: VF2Checker.runFGS or VF2Checker.runFGSStream.

Test cases are commented in the main function. 

You can commment out to investigate the details.

Example:

1. Build graph (para 1: ontology of DBpedia file , para 2: Movie graph file, 
 para 3: IMDB enrich data file ): 

        MovieDataGraph graph = new MovieDataGraph("newType.ttl",
                 "mix.dbpedia.graph", "film.imdb.json");
                 
                 

2. Define constraints (two movie groups with range constraint [20,30]): 

        ArrayList<String> group = new ArrayList<>();
        group.add("Comedy");
        group.add("Action");

        ArrayList<Rangepair> cc = new ArrayList<>();
        cc.add(new Rangepair(20, 30));
        cc.add(new Rangepair(20, 30));

3. Call proecedure (para 1: group (group), para 2: constraints(cc),
para 3: hop(2), para 4: number of node in Vp(m=50), para 5: size of result pattern set (n=10))

       checker.runFGS(group, cc, 2, 50,10);