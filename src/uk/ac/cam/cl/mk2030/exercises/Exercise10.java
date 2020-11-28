package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Exercise10 implements IExercise10 {
    /**
     * Load the graph file. Each line in the file corresponds to an edge; the
     * first column is the source node and the second column is the target. As
     * the graph is undirected, your program should add the source as a
     * neighbour of the target as well as the target a neighbour of the source.
     *
     * @param graphFile {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> For
     * each node, all the nodes neighbouring that node
     */
    @Override
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> sentiments = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(graphFile)) {
            reader.lines().forEach(line -> {
                String[] tokens = line.split(" |\\R");
                Integer from = Integer.parseInt(tokens[0]);
                Integer to = Integer.parseInt(tokens[1]);

                if (sentiments.containsKey(from)){
                    sentiments.get(from).add(to);
                } else {
                    Set<Integer> newS = new HashSet<>();
                    newS.add(to);
                    sentiments.put(from,  newS);
                }

                if (sentiments.containsKey(to)){
                    sentiments.get(to).add(from);
                } else {
                    Set<Integer> newS = new HashSet<>();
                    newS.add(from);
                    sentiments.put(to,  newS);
                }
            });
        } catch (IOException e) {
            throw new IOException("Can't access the file " + graphFile.toAbsolutePath(), e);
        }
        return sentiments;
    }

    /**
     * Find the number of neighbours for each point in the graph.
     *
     * @param graph {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *              loaded graph
     * @return {@link Map}<{@link Integer}, {@link Integer}> For each node, the
     * number of neighbours it has
     */
    @Override
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {

        Map<Integer,Integer> numNeighbours = new HashMap<>();
        for(Integer i: graph.keySet()) {
            numNeighbours.put(i,graph.get(i).size());
        }


        return numNeighbours;
    }

    /**
     * Find the maximal shortest distance between any two nodes in the network
     * using a breadth-first search.
     *
     * @param graph {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *              loaded graph
     * @return <code>int</code> The diameter of the network
     */
    @Override
    public int getDiameter(Map<Integer, Set<Integer>> graph) {

        Set<Integer> visitedNodesOverall = new HashSet<>();
        int maxDist = 0;

        for (Integer i: graph.keySet()){
                Queue<Integer> bfs_q = new LinkedList<>();
                Map<Integer,Integer> dist = new HashMap<>();
                Set<Integer> bfs_visited = new HashSet<>();

                bfs_q.add(i);
                dist.put(i,0);
                bfs_visited.add(i);
                while (!bfs_q.isEmpty()) {

                    int current = bfs_q.poll();
                    int currentDist = dist.get(current);


                    Set<Integer> neighbours = graph.get(current);
                    for (Integer n : neighbours) {




                        if (!bfs_visited.contains(n)) {

                            if (maxDist < currentDist + 1) {
                                maxDist = currentDist + 1;
                            }

                            bfs_visited.add(n);
                            bfs_q.add(n);
                            dist.put(n, currentDist + 1);
                        }

                    }


                }

        }

        return  maxDist;
    }
}
