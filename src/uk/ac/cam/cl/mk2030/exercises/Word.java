package uk.ac.cam.cl.mk2030.exercises;

public class Word {

    public Word(String intensity,String polarity,String word) {

        this.word = word;
        if (intensity.equals("strong") && polarity.equals("negative")) {
            this.intensity = Intensity.STRONG;
            this.polarity = Polarity.NEGATIVE;

        } else if (intensity.equals("weak") && polarity.equals("negative")){
            this.intensity = Intensity.WEAK;
            this.polarity = Polarity.NEGATIVE;

        } else if (intensity.equals("strong")  && polarity.equals("positive")) {
            this.intensity = Intensity.STRONG;
            this.polarity = Polarity.POSITIVE;

        } else {
            this.intensity = Intensity.WEAK;
            this.polarity = Polarity.POSITIVE;
        }
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Intensity getIntensity() {
        return intensity;
    }

    public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
    }

    public Polarity getPolarity() {
        return polarity;
    }

    public void setPolarity(Polarity polarity) {
        this.polarity = polarity;
    }

    enum Intensity {
        STRONG,WEAK;
    }

    enum Polarity {
        POSITIVE,NEGATIVE;
    }


    private String word;
    private Intensity intensity;
    private Polarity polarity;




}
