package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Exercise2 implements IExercise2 {

    @Override
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {

        long numPos = trainingSet.keySet().stream().filter(i -> trainingSet.get(i) == Sentiment.POSITIVE).count();
        long totalNum = trainingSet.size();
        double probPos = numPos / (double) totalNum;
        double probNeg = 1 - probPos;

        Map<Sentiment, Double> ClassProbs = new HashMap<>();
        ClassProbs.put(Sentiment.POSITIVE, probPos);
        ClassProbs.put(Sentiment.NEGATIVE, probNeg);

        return ClassProbs;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {

        Map<String, int[]> amountMap = new HashMap<>(); //Will keep track of how many times each word appears in +ve and -ve reviews

        int totalPos = 0;
        int totalNeg = 0;

        for (Path reviewPath : trainingSet.keySet()) { //For each word in the path

            List<String> reviewWords = Tokenizer.tokenize(reviewPath);
            Sentiment reviewSentiment = trainingSet.get(reviewPath);

            for (String word : reviewWords) {
                if (amountMap.containsKey(word)) {
                    if (reviewSentiment == Sentiment.POSITIVE) {
                        amountMap.get(word)[0]++;
                        totalPos++;
                    } else {
                        amountMap.get(word)[1]++;
                        totalNeg++;
                    }

                } else {
                    if (reviewSentiment == Sentiment.POSITIVE) {
                        amountMap.put(word, new int[]{1, 0});
                        totalPos++;
                    } else {
                        amountMap.put(word, new int[]{0, 1});
                        totalNeg++;
                    }
                }
            }

        }

        Map<String, Map<Sentiment, Double>> wordProbs = new HashMap<>();

        for (String word : amountMap.keySet()) {
            Map<Sentiment, Double> sentimentProbs = new HashMap<>();

            int posAmount = amountMap.get(word)[0];
            double posProb = posAmount / (double) totalPos;

            int negAmount = amountMap.get(word)[1];
            double negProb = negAmount / (double) totalNeg;

            double logPosProb = Math.log(posProb);
            double logNegProb = Math.log(negProb);

            sentimentProbs.put(Sentiment.POSITIVE, logPosProb);
            sentimentProbs.put(Sentiment.NEGATIVE, logNegProb);

            wordProbs.put(word, sentimentProbs);

        }

        return wordProbs;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {

        Map<String, int[]> amountMap = new HashMap<>();

        int totalPos = 0;
        int totalNeg = 0;

        for (Path reviewPath : trainingSet.keySet()) { //counting number of occurences of each word in each class

            List<String> reviewWords = Tokenizer.tokenize(reviewPath);
            Sentiment reviewSentiment = trainingSet.get(reviewPath);

            for (String word : reviewWords) {
                if (amountMap.containsKey(word)) {
                    if (reviewSentiment == Sentiment.POSITIVE) {
                        amountMap.get(word)[0]++;
                        totalPos++;
                    } else {
                        amountMap.get(word)[1]++;
                        totalNeg++;
                    }

                } else {

                    if (reviewSentiment == Sentiment.POSITIVE) {
                        amountMap.put(word, new int[]{1, 0});
                        totalPos++;
                    } else {
                        amountMap.put(word, new int[]{0, 1});
                        totalNeg++;
                    }
                }
            }

        }

        totalPos += amountMap.size() ; //Add normalising to denominator
        totalNeg += amountMap.size() ;

        Map<String, Map<Sentiment, Double>> wordProbs = new HashMap<>();

        for (String word : amountMap.keySet()) {

            Map<Sentiment, Double> sentimentProbs = new HashMap<>();

            int posAmount = amountMap.get(word)[0];
            double posProb = (posAmount + 1) / ((double) totalPos);

            int negAmount = amountMap.get(word)[1];
            double negProb = (negAmount + 1) / ((double) totalNeg);

            double logPosProb = Math.log(posProb);
            double logNegProb = Math.log(negProb);

            sentimentProbs.put(Sentiment.POSITIVE, logPosProb);
            sentimentProbs.put(Sentiment.NEGATIVE, logNegProb);

            wordProbs.put(word, sentimentProbs);

        }

        return wordProbs;
    }

    @Override
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {

        Map<Path, Sentiment> Classifications = new HashMap<>();
        for (Path review : testSet) {

            List<String> words = Tokenizer.tokenize(review);

            double[] C_bs = new double[]{classProbabilities.get(Sentiment.POSITIVE), classProbabilities.get(Sentiment.NEGATIVE)};
            for (String word : words) {
                if (tokenLogProbs.containsKey(word)) {//Only care about words which we have seen before, unknown words do not affect classification
                    Map<Sentiment, Double> W_cs = tokenLogProbs.get(word);
                    C_bs[0] += W_cs.get(Sentiment.POSITIVE);
                    C_bs[1] += W_cs.get(Sentiment.NEGATIVE);
                }
            }


            if (C_bs[0] >= C_bs[1]) {
                Classifications.put(review,Sentiment.POSITIVE);
            } else {
                Classifications.put(review,Sentiment.NEGATIVE);
            }
        }

        return Classifications;
    }
}
