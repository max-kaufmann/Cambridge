package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Exercise4  implements IExercise4 {

    private static final double STRONGPOSWEIGHT = 2;
    private static final double WEAKPOSWEIGHT =1;
    private static final double STRONGNEGWEIGHT = 2;
    private static final double WEAKNEGWEIGHT = 1;

    static Word  LinetoWord(String line) {
        String[] lineArr = line.split(" ");
        String word = lineArr[0].substring(5);
        String strIntensity = lineArr[1].substring(10);
        String strPolarity = lineArr[2].substring(9);

        return new Word(strIntensity,strPolarity,word);
    }

    @Override
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        HashMap<String, Word> lexicon = new HashMap<>();

        try (Stream<String> lines = Files.lines(lexiconFile)) {
            lines.map(Exercise4::LinetoWord).forEach(s -> lexicon.put(s.getWord(), s));
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
                        } else {
                            neg += WEAKNEGWEIGHT;
                        }
                    } else {
                        if (lexWord.getIntensity() == Word.Intensity.STRONG) {
                            pos += STRONGPOSWEIGHT;
                        } else {
                            pos += WEAKPOSWEIGHT;
                        }

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
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
        long Pos = 0; //num times a better than b
        long Neg = 0; //num times b better than a
        long Null = 0; //num times same

        for (Path file : actualSentiments.keySet()) {
            if (classificationA.get(file) == classificationB.get(file)) {
                Null++;
            } else if (classificationA.get(file) == actualSentiments.get(file)) {
                Pos++;
            } else {
                Neg++;
            }
        }

        long n = Pos + Neg +2*(( Null + 1)/2);
        long k = (Null + 1) / 2 + Math.min(Pos, Neg);
        BigDecimal prob = new BigDecimal(0);

        System.out.println(n);

        for (long i = 0; i <= k; i++) {
            prob = prob.add(((new BigDecimal(choose(n, i))).multiply(new BigDecimal(0.5).pow((int) n))));
        }

        return 2*prob.doubleValue();
    }

    BigInteger choose(long n, long k){
        BigInteger numerator = new BigInteger("1");
        BigInteger denominator = new BigInteger("1");

        for (int i = 0; i<k;i++){
            numerator = numerator.multiply(BigInteger.valueOf(n-i));
        }

        for (int i = 1 ;i <= k;i++){
            denominator = denominator.multiply(BigInteger.valueOf(i));
        }

        return numerator.divide(denominator);
    }
}
