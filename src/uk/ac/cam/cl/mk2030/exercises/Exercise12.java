package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;

import java.util.*;

public class Exercise12 implements IExercise12 {
    /**
     * Compute graph clustering using the Girvan-Newman method. Stop algorithm when the
     * minimum number of components has been reached (your answer may be higher than
     * the minimum).
     *
     * @param graph             {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *                          loaded graph
     * @param minimumComponents {@link int} The minimum number of components to reach.
     * @return {@link List}<{@link Set}<{@link Integer}>>
     * List of components for the graph.
     */

    static final double ERROR =  1e-06d;

    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {

        List<Set<Integer>> cComponents = getComponents(graph);
        Map<Integer, Map<Integer, Double>> eBetweenness;
        while (getNumberOfEdges(graph) >0 && cComponents.size() < minimumComponents ) {

            eBetweenness = getEdgeBetweenness(graph);

            double max = 0;
            double betweeness;
            Map<Integer,List<Integer>> maxEdges = new HashMap<>();

            for (int v : eBetweenness.keySet()) {
                for (int t : eBetweenness.get(v).keySet()) {
                    if ((betweeness = eBetweenness.get(v).get(t)) - max > ERROR) {
                        max = betweeness;
                        maxEdges = new HashMap<>();
                        List<Integer> ts = new ArrayList<>();
                        ts.add(t);
                        maxEdges.put(v,ts);
                    }
                    else if (betweeness - max > -ERROR) {
                        if (maxEdges.containsKey(v)) {
                            maxEdges.get(v).add(t);
                        } else {
                            List<Integer> ts = new ArrayList<>();
                            ts.add(t);
                            maxEdges.put(v,ts);
                        }

                    }
                }
            }

            for (Integer u: maxEdges.keySet()) {
                List<Integer> vs = maxEdges.get(u);
                for(Integer v: vs){
                    graph.get(u).remove(v);
                }
            }
            cComponents = getComponents(graph);
        }

        return cComponents;
    }




    private void dfs(Integer current,Map<Integer,Set<Integer>> graph, Map<Integer,Boolean> visited, Set<Integer> components){
        visited.put(current,Boolean.TRUE);
        components.add(current);
        for (Integer n: graph.get(current)){
            if (!visited.get(n)) {
                visited.put(n,Boolean.TRUE);
                components.add(n);
                dfs(n,graph,visited,components);
            }
        }
    }





    /**
     * Find the number of edges in the graph.
     *
     * @param graph {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *              loaded graph
     * @return {@link Integer}> Number of edges.
     */
    @Override
    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
        int numEdges = 0;

        for (Integer i : graph.keySet()) {
            Set<Integer> edges = graph.get(i);
            for (Integer j : edges) {
                numEdges++;
            }

        }

        return numEdges/2;
    }



    /**
     * Find the number of components in the graph using a DFS.
     *
     * @param graph {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *              loaded graph
     * @return {@link List}<{@link Set}<{@link Integer}>>
     * List of components for the graph.
     */
    @Override
    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
        List<Set<Integer>> components = new ArrayList<>();

        Map<Integer,Boolean> visited = new HashMap<>();

        for (Integer v : graph.keySet()){
            visited.put(v,false);
        }

        for (Integer i: graph.keySet()){
            if (!visited.get(i)){
                Set<Integer> currentComponent = new HashSet<>();
                visited.put(i,Boolean.TRUE);
                currentComponent.add(i);
                dfs(i,graph,visited,currentComponent);
                components.add(currentComponent);
            }
        }

        return components;
    }

    /**
     * Calculate the edge betweenness.
     *
     * @param graph {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *              loaded graph
     * @return {@link Map}<{@link Integer},
     * {@link Map}<{@link Integer},{@link Double}>> Edge betweenness for
     * each pair of vertices in the graph
     */
    @Override
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        Set<Integer> V = graph.keySet();
        Map<Integer,Map<Integer,Double>> C_b = new HashMap<>();

        for (Integer i: V) {
            Map<Integer,Double> V_map = new HashMap<>();
            C_b.put(i,V_map);
            for (Integer j : graph.get(i)) {
                V_map.put(j,0.0);
            }
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
                    Map<Integer,Double> V_map = C_b.get(v);
                    Double c =  (((double)numShortestPaths.get(v))/(numShortestPaths.get(w)))*(1 + Dependencies.get(w));
                    V_map.put(w,V_map.get(w) + c);
                    Dependencies.put(v,Dependencies.get(v) + c);

                }
            }
        }

        return C_b;

    }



    }

