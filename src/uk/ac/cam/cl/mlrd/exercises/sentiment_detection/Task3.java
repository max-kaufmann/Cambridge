package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import com.sun.javafx.css.parser.Token;
import javafx.util.Pair;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.ChartPlotter;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Task3  {

    public static void main(String[] args) throws IOException{
        Task3 t = new Task3();
        t.heapLaw();
    }

    BestFit.Line BestFitLine;


    {

        try {

            HashMap<String, Integer> wordFreq = new HashMap<>(); //counts words and frequency of words

            for (Path file : Files.newDirectoryStream(Path.of("D:\\Cambridge\\CompSci\\1A\\MLRWD\\Task 1\\data\\large_dataset\\large_dataset")))  { // for every text file
                List<String> words = Tokenizer.tokenize(file);
                for (String word : words) { //for every word in text file
                    if (wordFreq.containsKey(word)) {
                        wordFreq.put(word, wordFreq.get(word) + 1);
                    } else {
                        wordFreq.put(word, 0);
                    }
                }
            }

            List<Pair<String, Integer>> freqs = new ArrayList<>();//List of words, sorted by frequency

            for (String k : wordFreq.keySet()) {
                freqs.add(new Pair<>(k, wordFreq.get(k)));
            }
            freqs.sort((j, i) -> i.getValue().compareTo(j.getValue()));//Sorting the words by frequency
            List<Pair<String, Integer>> plotData = freqs.stream().limit(10000L).collect(Collectors.toList());//First 10000 words
            List<BestFit.Point> ZipfLines = new ArrayList<>(); //This is what will be plotted, i.e rank vs frequency

            for (int i = 1; i < plotData.size() + 1; i++) { //start at one so logs don't go to minus infinity
                ZipfLines.add(new BestFit.Point(i, plotData.get(i - 1).getValue()));//adds rank to words
            }

            ChartPlotter plot = new ChartPlotter();

            List<String> task1words = List.of(new String[]{"great", "recommend", "fun", "boring", "funny", "interesting", "scary", "bad", "good"}); //my words
            Map<String,Integer> task1ranks = new HashMap<>();
            List<BestFit.Point> task1freqs = new ArrayList<>();//data to be plotted

            int i = 0;

            for (Pair<String, Integer> word : plotData) {
                if (task1words.contains(word.getKey())) {
                    task1ranks.put(word.getKey(),i);
                    task1freqs.add(new BestFit.Point(i, wordFreq.get(word.getKey())));//add words with frquency and rank
                }
                i++;
            }

            List<BestFit.Point> logpoints = ZipfLines.stream().map(j -> new BestFit.Point(Math.log(j.x), Math.log(j.y))).collect(Collectors.toList()); //Log of points


            Map<BestFit.Point, Double> leastSquaresPoints = new HashMap<>();

            logpoints.stream().forEach(x -> leastSquaresPoints.put(x, Math.exp(x.y)));//add frequency as a weight

            BestFit.Line l = BestFit.leastSquares(leastSquaresPoints);
            BestFit.Point lp1 = new BestFit.Point(0, l.yIntercept);
            BestFit.Point lp2 = new BestFit.Point(10, l.yIntercept + 10 * l.gradient);
            this.BestFitLine = l;
            System.out.println("!");

            plot.plotLines(ZipfLines);
            plot.plotLines(task1freqs);
            plot.plotLines(logpoints, new ArrayList<BestFit.Point>(List.of(lp1, lp2)));

            for  (String word: task1words){
                System.out.println(word + ": estimated freq = " + estFrequency(task1ranks.get(word)) + " actual freq = " + wordFreq.get(word));
            }

            double alpha = - this.BestFitLine.gradient;
            double k = Math.exp(this.BestFitLine.yIntercept);

            System.out.println("AlPHA = " + alpha);
            System.out.println("K = " + k) ;

        } catch (Exception e){
            System.out.println(e);

    }

    }


    int estFrequency(int rank){
        return (int) Math.exp(Math.log(rank)*this.BestFitLine.gradient + this.BestFitLine.yIntercept);


    }

    void heapLaw() throws IOException{

        Set<String> types = new HashSet<>();
        List<BestFit.Point> points = new ArrayList<>();

        int p = 0;
        int numTokens = 0;
        for (Path file : Files.newDirectoryStream(Path.of("D:\\Cambridge\\CompSci\\1A\\MLRWD\\Task 1\\data\\large_dataset\\large_dataset"))) {
            List<String> words = Tokenizer.tokenize(file);
            for (String word : words) {
                types.add(word);
                numTokens += 1;

                if (numTokens == Math.pow(2,p)) {
                    p += 1;
                    points.add(new BestFit.Point(Math.log(numTokens),Math.log(types.size())));
                }
            }
        }

        points.add(new BestFit.Point(Math.log(numTokens),Math.log(types.size())));

      ChartPlotter plot =  new ChartPlotter();
      plot.plotLines(points);



    }
}
