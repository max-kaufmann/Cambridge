package uk.ac.cam.cl.mk2030.exercises;

import com.sun.jdi.InvalidCodeIndexException;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;




public class Exercise1 implements IExercise1 {

    private static final double STRONGPOSWEIGHT = 2;
    private static final double WEAKPOSWEIGHT = 0.65;
    private static final double STRONGNEGWEIGHT = 2.4;
    private static final double WEAKNEGWEIGHT = 0.9;

    static Word  LinetoWord(String line) {
        String[] lineArr = line.split(" ");
        String word = lineArr[0].substring(5);
        String strIntensity = lineArr[1].substring(10);
        String strPolarity = lineArr[2].substring(9);

        return new Word(strIntensity,strPolarity,word);
    }

    @Override
    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        HashMap<String, Word> lexicon = new HashMap<>();

        try (Stream<String> lines = Files.lines(lexiconFile)) {
            lines.map(Exercise1::LinetoWord).forEach(s -> lexicon.put(s.getWord(), s));
        } catch (IOException i) {
            throw i;
        }

        int h = 5;

        Map<Path, Sentiment> sentiments = new HashMap<Path, Sentiment>();

        for (Path reviewPath : testSet) {
            List<String> reviewWords = Tokenizer.tokenize(reviewPath);

            int pos = 0;
            int neg = 0;
            for (String word : reviewWords) {
                if (lexicon.containsKey(word)) {
                    Word lexWord = lexicon.get(word);
                    if (lexWord.getPolarity() == Word.Polarity.NEGATIVE) {
                        neg++;
                    } else {
                        pos++;
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

    @Override
    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {

        int correct = 0;
        for (Path path: predictedSentiments.keySet()){
            if (trueSentiments.get(path) == predictedSentiments.get(path)){
                correct += 1;
            }
        }

        return (correct / ((double)trueSentiments.size()));
    }

    @Override
    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
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


}
