package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise11;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Exercise11  implements IExercise11 {
    /**
     * Load the graph file. Use Brandes' algorithm to calculate the betweenness
     * centrality for each node in the graph.
     *
     * @param graphFile {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Double}> For
     * each node, its betweenness centrality
     */
    @Override
    public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {

        Map<Integer, Set<Integer>> graph = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(graphFile)) {
            reader.lines().forEach(line -> {
                String[] tokens = line.split(" |\\R");
                Integer from = Integer.parseInt(tokens[0]);
                Integer to = Integer.parseInt(tokens[1]);

                if (graph.containsKey(from)){
                    graph.get(from).add(to);
                } else {
                    Set<Integer> newS = new HashSet<>();
                    newS.add(to);
                    graph.put(from,  newS);
                }

                if (graph.containsKey(to)){
                    graph.get(to).add(from);
                } else {
                    Set<Integer> newS = new HashSet<>();
                    newS.add(from);
                    graph.put(to,  newS);
                }
            });
        } catch (IOException e) {
            throw new IOException("Can't access the file " + graphFile.toAbsolutePath(), e);
        }

        Set<Integer> V = graph.keySet();
        Map<Integer,Double> C_b = new HashMap<>();

        for (Integer w : V){
            C_b.put(w,0.0);
        }

        for( Integer s: V){


            Queue<Integer> Q = new LinkedList<>();
            List<Integer> S = new ArrayList<>();

            Map<Integer,List<Integer>> Predecessors = new HashMap<>(); //Predecessors on shortest paths through node (from s)
            Map<Integer,Integer> Distances = new HashMap<>();//Distance from s
            Map<Integer,Integer> numShortestPaths = new HashMap<>();//Number of shortest paths through node
            Map<Integer,Double> Dependencies = new HashMap<>();//Directed dependencies  (sum of proportions of shortest paths through node from s to node t)

            for (Integer w : V){
                Predecessors.put(w,new ArrayList<>());
                Distances.put(w,-1);
                numShortestPaths.put(w,0);
                Dependencies.put(w,0.0);
            }

            Q.add(s);
            Distances.put(s,0);
            numShortestPaths.put(s,1);

            while (!Q.isEmpty()){ //Counting of number of shortest paths
                Integer current = Q.poll();
                S.add(current);
                Set<Integer> neighbours = graph.get(current);

                for (Integer n: neighbours){

                    Integer dist = Distances.get(n);

                    if (dist == -1){
                        Distances.put(n,Distances.get(current) + 1);
                        Q.add(n);
                    }

                    dist = Distances.get(n);

                    if (dist == Distances.get(current) + 1){
                        numShortestPaths.put(n,numShortestPaths.get(n) + numShortestPaths.get(current));
                        Predecessors.get(n).add(current);
                    }


                }
            }

            for (int i = S.size() - 1; i >= 0; i-- ){
                Integer w = S.get(i);
                for (Integer v: Predecessors.get(w)){
                    Dependencies.put(v,Dependencies.get(v) + (((double)numShortestPaths.get(v))/(numShortestPaths.get(w)))*(1 + Dependencies.get(w)));
                }

                if ( w != s){
                    C_b.put(w,C_b.get(w) + Dependencies.get(w));
                }
            }
        }

        for(Integer i : C_b.keySet()){
            C_b.put(i,C_b.get(i)/2);
        }

        return C_b;
    }
}
