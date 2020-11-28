package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Maximiser {

    static final Path dataDirectory = Paths.get("D:/Cambridge/CompSci/1A/MLRWD/Task 1/data/sentiment_dataset");

    private static double STRONGPOSWEIGHT = 0.8;
    private static double WEAKPOSWEIGHT = 0.6;
    private static double STRONGNEGWEIGHT = 1;
    private static double WEAKNEGWEIGHT = 0.9;

    public static Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        HashMap<String, Word> lexicon = new HashMap<>();

        try (Stream<String> lines = Files.lines(lexiconFile)) {
            lines.map(Exercise1::LinetoWord).forEach(s -> lexicon.put(s.getWord(), s));
        } catch (IOException i) {
            throw i;
        }

        Map<Path, Sentiment> sentiments = new HashMap<Path, Sentiment>();

        for (Path reviewPath : testSet) {
            List<String> reviewWords = Tokenizer.tokenize(reviewPath);

            double pos = 0;
            double neg = 0;
            for (String word : reviewWords) {
                if (lexicon.containsKey(word)) {
                    Word lexWord = lexicon.get(word);
                    if (lexWord.getPolarity() == Word.Polarity.NEGATIVE) {
                        if (lexWord.getIntensity() == Word.Intensity.STRONG) {
                            neg += STRONGNEGWEIGHT;
                        }

                        neg += WEAKNEGWEIGHT;
                    } else {
                        if (lexWord.getIntensity() == Word.Intensity.STRONG) {
                            pos += STRONGPOSWEIGHT;
                        }

                        pos += WEAKPOSWEIGHT;

                    }
                }
            }

            if (pos >= neg) {
                sentiments.put(reviewPath, Sentiment.POSITIVE);
            } else {
                sentiments.put(reviewPath, Sentiment.NEGATIVE);

            }
        }
        return sentiments;

    }

    public static void main(String[] args) {

        Exercise1 e = new Exercise1();

        try {
            Path lexiconFile = Paths.get("D:/Cambridge/CompSci/1A/MLRWD/Task 1/data/sentiment_lexicon.txt");

            // Loading the dataset.
            Path sentimentFile = dataDirectory.resolve("review_sentiment");
            Path reviewsDir = dataDirectory.resolve("reviews");


            Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(reviewsDir, sentimentFile);

            double[] results = new double[] {0,0,0,0};

            for (double sneg = 0.9610; sneg <= 0.962; sneg += 0.0005){
                for (double wpos = 0.25; wpos <= 0.27; wpos += 0.01){
                    for (double wneg = 0.37; wneg <= 0.39; wneg += 0.01){
                        STRONGNEGWEIGHT = sneg;
                        WEAKNEGWEIGHT = wneg;
                        WEAKPOSWEIGHT = wpos;
                        Map<Path, Sentiment> improvedPredictions = improvedClassifier(dataSet.keySet(), lexiconFile);
                        double calculatedAccuracy = e.calculateAccuracy(dataSet, improvedPredictions);
                        System.out.println(calculatedAccuracy);
                        if (calculatedAccuracy > results[3]){
                            results = new double[] {sneg,wpos,wneg,calculatedAccuracy};
                        }

                    }
                }
            }

            System.out.println(Arrays.toString(results));
        } catch (Exception a) {
            System.out.println("job fucked");
        }
    }
}
