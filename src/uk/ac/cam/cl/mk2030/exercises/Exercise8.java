package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise8 implements IExercise8 {

    /**
     * Uses the Viterbi algorithm to calculate the most likely single sequence
     * of hidden states given the observed sequence and a model.
     *
     * @param model            {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}>
     *                         A sequence model.
     * @param observedSequence {@link List}<{@link DiceRoll}> A sequence of observed die
     *                         rolls
     * @return {@link List}<{@link DiceType}> The most likely single sequence of
     * hidden states
     */
    @Override
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
        return viterbiGeneric(model,observedSequence);
    }

     static public <S,O>  List<S> viterbiGeneric(HiddenMarkovModel<O,S> model, List<O> observedSequence){

        List<Map<S,Double>> delta = new ArrayList<>();
        List<Map<S,S>> sigh = new ArrayList<>();

        O startSymbol = observedSequence.get(0);
        int T = observedSequence.size();

        Set<S> states = model.getHiddenStates();
        Map<S, Map<S,Double>> transitions = model.getTransitionMatrix();
        Map<S,Map<O,Double>> emissions = model.getEmissionMatrix();


        for (int i = 0; i < T;i++){
            Map<S,Double> currentProbs = new HashMap<>();
            Map<S,S> currentMostLikely = new HashMap<>();

            for (S state : model.getHiddenStates()) {
                currentProbs.put(state,0.0);
                currentMostLikely.put(state,null);
            }
            delta.add(currentProbs);
            sigh.add(currentMostLikely);
        }

         Map<S,Double> firstColumn = delta.get(0);
         for(S state: states){
            firstColumn.put(state,Math.log(emissions.get(state).get(observedSequence.get(0))));
         }


        for (int t = 1; t <= T - 1; t++) {

            O currentObservation = observedSequence.get(t);
            Map<S,Double> currentObservationRow = delta.get(t);
            Map<S,S> currentMostLikely = sigh.get(t);

            for (S j : states){

                double currentObservationProb = emissions.get(j).get(currentObservation);
                S currentMaxState = null;
                Double currentMaxProb = null;

                for (S i : states) {
                    double observedProb = delta.get(t-1).get(i) + Math.log(transitions.get(i).get(j)) + Math.log(currentObservationProb);

                    if (currentMaxProb == null || observedProb >= currentMaxProb ){
                        currentMaxProb = observedProb;
                        currentMaxState = i;
                    }
                }

                currentObservationRow.put(j,currentMaxProb);
                currentMostLikely.put(j,currentMaxState);

            }

        }

        S endState = null;
        O endSymbol = observedSequence.get(observedSequence.size()- 1);

        Double probMax = null;
            for(S state: states){
                Double stateProb = emissions.get(state).get(endSymbol) ;
                if (probMax == null || stateProb > probMax){
                    endState = state;
                    probMax = stateProb;
                }
        }

        int i = observedSequence.size()-1;
        S currentState = endState;
        List<S> StateListRev = new ArrayList<>();

        while (i > 0){
            StateListRev.add(currentState);
            currentState = sigh.get(i).get(currentState);
            i = i - 1;
        }

        StateListRev.add(currentState);

        List<S> results = new ArrayList<>();

        for (int a = StateListRev.size()-1; a >= 0;a--){
            results.add(StateListRev.get(a));
        }


        return results;



    }

    /**
     * Uses the Viterbi algorithm to predict hidden sequences of all observed
     * sequences in testFiles.
     *
     * @param model     The HMM model.
     * @param testFiles A list of files with observed and true hidden sequences.
     * @return {@link Map}<{@link List}<{@link DiceType}>,
     * {@link List}<{@link DiceType}>> A map from a real hidden sequence
     * to the equivalent estimated hidden sequence.
     * @throws IOException
     */
    @Override
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
        List<HMMDataStore<DiceRoll,DiceType>> sequences = HMMDataStore.loadDiceFiles(testFiles);
        Map<List<DiceType>, List<DiceType>> results = new HashMap<>();

        for (HMMDataStore<DiceRoll,DiceType> sequence : sequences) {
            results.put(sequence.hiddenSequence,viterbi(model,sequence.observedSequence));
        }

        return results;
    }

    /**
     * Calculates the precision of the estimated sequence with respect to the
     * weighted state, i.e. the proportion of predicted weighted states that
     * were actually weighted.
     *
     * @param true2PredictedMap {@link Map}<{@link List}<{@link DiceType}>,
     *                          {@link List}<{@link DiceType}>> A map from a real hidden
     *                          sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The precision of the estimated sequence with
     * respect to the weighted state averaged over all the test
     * sequences.
     */
    @Override
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        int numPredictedWeighted = 0;
        int actualWeighted = 0;

        for(List<DiceType> trueSequence: true2PredictedMap.keySet()){
            List<DiceType> predictedSequence = true2PredictedMap.get(trueSequence);
            for (int i = 0; i < predictedSequence.size();i++){
                if (predictedSequence.get(i) == DiceType.WEIGHTED){
                    numPredictedWeighted += 1;
                    if (trueSequence.get(i) == DiceType.WEIGHTED){
                        actualWeighted += 1;
                    }
                }
            }
        }

        return ((double) actualWeighted/numPredictedWeighted);
    }

    /**
     * Calculates the recall of the estimated sequence with respect to the
     * weighted state, i.e. the proportion of actual weighted states that were
     * predicted weighted.
     *
     * @param true2PredictedMap {@link Map}<{@link List}<{@link DiceType}>,
     *                          {@link List}<{@link DiceType}>> A map from a real hidden
     *                          sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The recall of the estimated sequence with
     * respect to the weighted state averaged over all the test
     * sequences.
     */
    @Override
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        int numWeighted = 0;
        int numPredictedWeighted = 0;

        for(List<DiceType> trueSequence: true2PredictedMap.keySet()){
            List<DiceType> predictedSequence = true2PredictedMap.get(trueSequence);
            for (int i = 0; i < predictedSequence.size();i++){
                if (trueSequence.get(i) == DiceType.WEIGHTED){
                    numWeighted += 1;
                    if (predictedSequence.get(i) == DiceType.WEIGHTED){
                        numPredictedWeighted += 1;
                    }
                }
            }
        }

        return ((double) numPredictedWeighted/numWeighted);
    }

    /**
     * Calculates the F1 measure of the estimated sequence with respect to the
     * weighted state, i.e. the harmonic mean of precision and recall.
     *
     * @param true2PredictedMap {@link Map}<{@link List}<{@link DiceType}>,
     *                          {@link List}<{@link DiceType}>> A map from a real hidden
     *                          sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The F1 measure of the estimated sequence with
     * respect to the weighted state averaged over all the test
     * sequences.
     */
    @Override
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        return 2 * ((precision * recall) /(precision + recall));
    }
}
