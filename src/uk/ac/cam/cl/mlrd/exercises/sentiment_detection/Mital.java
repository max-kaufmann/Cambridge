package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

public class Mital {

    public static void main(String[] args) throws IOException {
        List<String> o = Tokenizer.tokenize(Path.of("D:\\Cambridge\\CompSci\\1A\\MLRWD\\Supervision 1\\lol"));
        o.sort(String::compareTo);
        System.out.println(o);
    }
}
