package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mk2030.exercises.Exercise2;
import uk.ac.cam.cl.mk2030.exercises.Exercise1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise5 implements IExercise5 {

    /**
     * Split the given data randomly into 10 folds.
     *
     * @param dataSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
     *
     * @param seed
     *            A seed for the random shuffling.
     * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
     *         of folds with even numbers of each sentiment
     */

    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {

        int n = dataSet.size();
        Random r = new Random(seed);

        List<Integer> indexes = new ArrayList<>();
        List<Map<Path,Sentiment>> groups = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            groups.add(new HashMap<Path,Sentiment>());
        }


        for (int i = 0; i < n;i++){
            indexes.add(i % 10);
        }

        for (Path document: dataSet.keySet()){
            int randomNum = r.nextInt(indexes.size());
            int randomGroup = indexes.get(randomNum);
            indexes.remove(randomNum);
            groups.get(randomGroup).put(document,dataSet.get(document));
        }

        return groups;
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {

        int n = dataSet.size();
        Random r = new Random(seed);

         List<Map<Path,Sentiment>> groups = new ArrayList<>();

        for (int i = 0; i < 10 ; i++) {
            groups.add(new HashMap<>());
        }

        List<Integer> Posindexes = new ArrayList<>();
        List<Integer> Negindexes = new ArrayList<>();

        Map<Path,Sentiment> Posdata = new HashMap<>();
        Map<Path,Sentiment> Negdata = new HashMap<>();

        dataSet.keySet().stream().forEach(i -> {
            if (dataSet.get(i) == Sentiment.NEGATIVE){
                Posdata.put(i,dataSet.get(i));
            }else{
                Negdata.put(i,dataSet.get(i));
        }
        });

        int pos = Posdata.size();
        int neg = Negdata.size();

        System.out.println(pos);
        System.out.println(neg);

        double posProportion = ((double)pos )/(pos + neg);
        double negProportion = 1 - posProportion;

        int posPerGroup = (int) (posProportion*(n/10));
        int negPerGroup = (int) (negProportion*(n/10));

        for (int i =0; i < 9;i++){

            for (int j = 0; j < posPerGroup;j++){
                Posindexes.add(i);
            }

            for(int j = 0; j < negPerGroup;j++){
                Negindexes.add(i);
            }
        }

        for (Path Pdocument: Posdata.keySet()) {
            if (Posindexes.isEmpty()) {
                groups.get(9).put(Pdocument, Posdata.get(Pdocument));
            } else {
                int randomNum = r.nextInt(Posindexes.size());
                int randomGroup = Posindexes.get(randomNum);
                Posindexes.remove(randomNum);
                groups.get(randomGroup).put(Pdocument, Posdata.get(Pdocument));

            }
        }

        for (Path Ndocument : Negdata.keySet()) {
            if (Negindexes.isEmpty()) {
                groups.get(9).put(Ndocument, Negdata.get(Ndocument));
            } else {
                int randomNum = r.nextInt(Negindexes.size());
                int randomGroup =Negindexes.get(randomNum);
                Negindexes.remove(randomNum);
                groups.get(randomGroup).put(Ndocument, Negdata.get(Ndocument));
            }
        }

        return groups;

        }

    /**
     * Run cross-validation on the dataset according to the folds.
     *
     * @param folds
     *            {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A
     *            set of folds.
     * @return Scores for individual cross-validation runs.
     * @throws IOException
     */

    @Override
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {

        double[] crossVal = new double[10];

        Exercise2 e2 = new Exercise2();
        Exercise1 e1 = new Exercise1();

        for (int i = 0; i < 10;i++){

            Map<Path,Sentiment> trainingSet = new HashMap<>();
            Map<Path, Sentiment> testSet = folds.get(i);

            for(int j = 0; j< 10;j++){
                if (j != i){
                    Map<Path,Sentiment> currentFold = folds.get(j);
                    currentFold.keySet().stream().forEach(a -> trainingSet.put(a,currentFold.get(a)));
                }
            }

            Map<Sentiment,Double> classProbs = e2.calculateClassProbabilities(trainingSet);
            Map<String, Map<Sentiment, Double>> smoothedLogProbs = e2.calculateSmoothedLogProbs(trainingSet);

            Map<Path,Sentiment> results = e2.naiveBayes(testSet.keySet(),smoothedLogProbs,classProbs);
            crossVal[i] = e1.calculateAccuracy(testSet,results);

        }
        return crossVal;
    }

    @Override
    public double cvAccuracy(double[] scores) {
        int n = scores.length;
        double count = 0;

        for (int i = 0; i < n; i++){
            count += scores[i];
        }

        return count/n;
    }

    @Override
    public double cvVariance(double[] scores) {
        double mean = cvAccuracy(scores);
        int n = scores.length;

        double sum = 0;

        for (int i = 0; i < n;i++){
            sum += (mean - scores[i])*(mean - scores[i]);
        }

        return (sum)/n;

    }
}
