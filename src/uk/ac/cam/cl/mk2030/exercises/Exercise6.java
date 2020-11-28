package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Exercise6 implements IExercise6 {
    /**
     * Calculate the probability of a document belonging to a given class based
     * on the training data.
     *
     * @param trainingSet {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
     *                    paths
     * @return {@link Map}<{@link NuancedSentiment}, {@link Double}> Class
     * probabilities.
     * @throws IOException
     */
    @Override
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {

        long numPos = trainingSet.keySet().stream().filter(i -> trainingSet.get(i) == NuancedSentiment.POSITIVE).count();
        long numNeg = trainingSet.keySet().stream().filter(i -> trainingSet.get(i) == NuancedSentiment.NEGATIVE).count();
        long numNuetral = trainingSet.keySet().stream().filter(i -> trainingSet.get(i) == NuancedSentiment.NEUTRAL).count();
        long totalNum = trainingSet.size();

        double probPos = numPos / (double) totalNum;
        double probNeg = numNeg/ (double) totalNum;
        double probNeutral = numNuetral / (double) totalNum;

        Map<NuancedSentiment, Double> ClassProbs = new HashMap<>();
        ClassProbs.put(NuancedSentiment.POSITIVE, probPos);
        ClassProbs.put(NuancedSentiment.NEGATIVE, probNeg);
        ClassProbs.put(NuancedSentiment.NEUTRAL, probNeutral);

        return ClassProbs;
    }

    /**
     * Modify your smoothed Naive Bayes to calculate log probabilities for three classes.
     *
     * @param trainingSet {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
     *                    paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment},
     * {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {

        Map<String, int[]> amountMap = new HashMap<>(); //Will keep track of how many times each word appears in +ve and -ve reviews

        int totalPos = 0;
        int totalNeg = 0;
        int totalNeutral = 0;

        for (Path reviewPath : trainingSet.keySet()) { //For each word in the path

            List<String> reviewWords = Tokenizer.tokenize(reviewPath);
            NuancedSentiment reviewSentiment = trainingSet.get(reviewPath);

            for (String word : reviewWords) {
                if (amountMap.containsKey(word)) {
                    if (reviewSentiment == NuancedSentiment.POSITIVE) {
                        amountMap.get(word)[0]++;
                        totalPos++;
                    } else if (reviewSentiment == NuancedSentiment.NEGATIVE) {
                        amountMap.get(word)[1]++;
                        totalNeg++;
                    } else {
                        amountMap.get(word)[2]++;
                        totalNeutral++;
                    }

                } else {
                    if (reviewSentiment == NuancedSentiment.POSITIVE) {
                        amountMap.put(word, new int[]{1, 0,0});
                        totalPos++;
                    } else  if (reviewSentiment == NuancedSentiment.NEGATIVE){
                        amountMap.put(word, new int[]{0, 1,0});
                        totalNeg++;
                    } else {
                        amountMap.put(word, new int[] {0,0,1});
                        totalNeutral++;
                    }
                }
            }

        }

        Map<String, Map<NuancedSentiment, Double>> wordProbs = new HashMap<>();

        int V = amountMap.size();

        for (String word : amountMap.keySet()) {
            Map<NuancedSentiment, Double> sentimentProbs = new HashMap<>();

            int posAmount = amountMap.get(word)[0];
            double posProb = (posAmount + 1) / ((double) totalPos + V);

            int negAmount = amountMap.get(word)[1];
            double negProb = (negAmount + 1)/ ((double) totalNeg + V);

            int neutralAmount = amountMap.get(word)[2];
            double neutralProb = (neutralAmount + 1)/ ((double) totalNeutral + V);

            double logPosProb = Math.log(posProb);
            double logNegProb = Math.log(negProb);
            double logNeutralProb = Math.log(neutralProb);

            sentimentProbs.put(NuancedSentiment.POSITIVE, logPosProb);
            sentimentProbs.put(NuancedSentiment.NEGATIVE, logNegProb);
            sentimentProbs.put(NuancedSentiment.NEUTRAL, logNeutralProb);

            wordProbs.put(word, sentimentProbs);

        }

        return wordProbs;
    }

    /**
     * Modify your Naive Bayes classifier so that it can classify reviews which
     * may also have neutral sentiment.
     *
     * @param testSet            {@link Set}<{@link Path}> Test review paths
     * @param tokenLogProbs      {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment}, {@link Double}> tokenLogProbs
     * @param classProbabilities {@link Map}<{@link NuancedSentiment}, {@link Double}> classProbabilities
     * @return {@link Map}<{@link Path}, {@link NuancedSentiment}> Predicted sentiments
     * @throws IOException
     */
    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {

        Map<Path, NuancedSentiment> Classifications = new HashMap<>();

        for (Path review : testSet) {

            List<String> words = Tokenizer.tokenize(review);
            double[] C_bs = new double[]{Math.log(classProbabilities.get(NuancedSentiment.POSITIVE)), Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE)),Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL))};
            for (String word : words) {
                if (tokenLogProbs.containsKey(word)) {//Only care about words which we have seen before, unknown words do not affect classification
                    Map<NuancedSentiment, Double> W_cs = tokenLogProbs.get(word);
                    C_bs[0] += W_cs.get(NuancedSentiment.POSITIVE);
                    C_bs[1] += W_cs.get(NuancedSentiment.NEGATIVE);
                    C_bs[2] += W_cs.get(NuancedSentiment.NEUTRAL);
                }
            }


            if (C_bs[0] >= C_bs[1]) {
                if (C_bs[2] >= C_bs[0]) {
                    Classifications.put(review, NuancedSentiment.NEUTRAL);
                } else {
                    Classifications.put(review, NuancedSentiment.POSITIVE);
                }
            }else {
                if (C_bs[2] >= C_bs[1]){
                    Classifications.put(review,NuancedSentiment.NEUTRAL);
                } else {
                    Classifications.put(review, NuancedSentiment.NEGATIVE);
                }
            }
        }

        return Classifications;
    }



    /**
     * Calculate the proportion of predicted sentiments that were correct.
     *
     * @param trueSentiments      {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
     *                            correct sentiment for each review
     * @param predictedSentiments {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
     *                            calculated sentiment for each review
     * @return <code>double</code> The overall accuracy of the predictions
     */
    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {

        int numTotal = predictedSentiments.size();
        int numCorrect = 0;

        for(Path review : predictedSentiments.keySet()){
            if (predictedSentiments.get(review) == trueSentiments.get(review)){
                numCorrect++;
            }
        }

        return ((double) numCorrect)/numTotal;

    }



    /**
     * Given some predictions about the sentiment in reviews, generate an
     * agreement table which for each review contains the number of predictions
     * that predicted each sentiment.
     *
     * @param predictedSentiments {@link Collection}<{@link Map}<{@link Integer},
     *                            {@link Sentiment}>> Different predictions for the
     *                            sentiment in each of a set of reviews 1, 2, 3, 4.
     * @return {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
     * {@link Integer}>> For each review, the number of predictio   ns that
     * predicted each sentiment
     */
    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {

        Map<Integer, Map<Sentiment, Integer>> reviewAnswers = new HashMap<Integer, Map<Sentiment, Integer>>();

        for (int i = 1; i < 5; i++) {

            HashMap<Sentiment, Integer> sentiments = new HashMap<>();

            sentiments.put(Sentiment.NEGATIVE, 0);
            sentiments.put(Sentiment.POSITIVE, 0);

            reviewAnswers.put(i, sentiments);
        }

        for (Map<Integer, Sentiment> reviewer : predictedSentiments) {

            for (Integer Opinion : reviewer.keySet()) {
                Sentiment reviewSentiment = reviewer.get(Opinion);
                Map<Sentiment, Integer> review = reviewAnswers.get(Opinion);
                review.put(reviewSentiment, review.get(reviewSentiment) + 1);
            }

        }

        return reviewAnswers;


        }



    /**
     * Using your agreement table, calculate the kappa value for how much
     * agreement there was; 1 should mean total agreement and -1 should mean total disagreement.
     *
     * @param agreementTable {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
     *                       {@link Integer}>> For each review (1, 2, 3, 4) the number of predictions
     *                       that predicted each sentiment
     * @return <code>double</code> The kappa value, between -1 and 1
     */
    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {

        double ni = 0;
        double N = agreementTable.size();

        for(Integer review : agreementTable.keySet()){
            ni += agreementTable.get(review).get(Sentiment.POSITIVE);
            ni += agreementTable.get(review).get(Sentiment.NEGATIVE);
            break;
        }

        double P_e = 0;

        for (Sentiment s : List.of(Sentiment.POSITIVE,Sentiment.NEGATIVE)) {
            double P_i = 0;

            for (Integer i : agreementTable.keySet()) {
                if (agreementTable.get(i).containsKey(s)) {
                    P_i += agreementTable.get(i).get(s) / ni;
                }


            }
            P_i = (1 / N) * (1 / N) * (P_i * P_i);
            P_e += P_i;
        }


        double P_a = 0;

        for(Integer i: agreementTable.keySet()){

            if (agreementTable.get(i).containsKey(Sentiment.NEGATIVE)) {
                double nij = agreementTable.get(i).get(Sentiment.NEGATIVE);
                P_a += nij * (nij - 1);
            }

            if (agreementTable.get(i).containsKey(Sentiment.POSITIVE)) {
                double nij = agreementTable.get(i).get(Sentiment.POSITIVE);
                P_a += nij * (nij - 1);
            }

        }

        P_a = P_a * (1/(N*ni*(ni-1)));

        return ((P_a - P_e)/(1 - P_e));
    }
}
