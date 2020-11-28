package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.util.*;

public class Exercise9 implements IExercise9 {
    /**
     * Loads the sequences of visible and hidden states from the sequence files
     * (visible amino acids on first line and hidden features on second) and
     * uses them to estimate the parameters of the Hidden Markov Model that
     * generated them.
     *
     * @param sequencePairs@return {@link HiddenMarkovModel}<{@link AminoAcid}, {@link Feature}> The
     *                             estimated model
     * @throws IOException
     */
    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {

        Set<AminoAcid> Alphabet = new HashSet<>();
        List<HMMDataStore<AminoAcid, Feature>> sequenceList = sequencePairs;

        Map<AbstractMap.SimpleEntry,Integer> TransitionCounter = new HashMap<>();
        Map<AbstractMap.SimpleEntry<Feature, AminoAcid>, Integer> StateObservationCounter = new HashMap<>();
        Map<Feature, Integer> TotalStateOccurences = new HashMap<>();

        List<Feature> firstStates = sequenceList.get(0).hiddenSequence;
        Feature StartState = firstStates.get(0);
        Feature EndState = firstStates.get(firstStates.size() - 1);

        List<AminoAcid> firstObservations = sequenceList.get(0).observedSequence;
        AminoAcid StartSymbol = firstObservations.get(0);
        AminoAcid EndSymbol = firstObservations.get(firstObservations.size() - 1);

        int numSequences = sequenceList.size();

        Alphabet.add(StartSymbol);


        for (HMMDataStore<AminoAcid, Feature> sequence : sequenceList) {

            List<AminoAcid> observed = sequence.observedSequence;
            List<Feature> hidden = sequence.hiddenSequence;

            for (int i = 1; i < observed.size(); i++) {

                Feature previousState = hidden.get(i - 1);
                Feature currentState = hidden.get(i);
                AminoAcid currentObservation = observed.get(i);

                Alphabet.add(currentObservation);


                if (TotalStateOccurences.containsKey(currentState)) {
                    TotalStateOccurences.put(currentState, TotalStateOccurences.get(currentState) + 1);
                } else {
                    TotalStateOccurences.put(currentState, 1);
                }

                AbstractMap.SimpleEntry<Feature, Feature> transitionPair = new AbstractMap.SimpleEntry<>(previousState, currentState);
                AbstractMap.SimpleEntry<Feature, AminoAcid> observationPair = new AbstractMap.SimpleEntry<>(currentState, currentObservation);


                if (TransitionCounter.containsKey(transitionPair)) {
                    TransitionCounter.put(transitionPair, TransitionCounter.get(transitionPair) + 1);
                } else {
                    TransitionCounter.put(transitionPair, 1);
                }

                if (StateObservationCounter.containsKey(observationPair)) {
                    StateObservationCounter.put(observationPair, StateObservationCounter.get(observationPair) + 1);
                } else {
                    StateObservationCounter.put(observationPair, 1);
                }
            }
        }

        Map<Feature, Map<Feature, Double>> TransitionProbabilities = new HashMap<>();

        Map<Feature, Double> startingRow = new HashMap<>();

        for(Feature s: TotalStateOccurences.keySet()){
            AbstractMap.SimpleEntry<Feature,Feature> currentTransition  = new AbstractMap.SimpleEntry<>(StartState,s);
            if (TransitionCounter.containsKey(currentTransition)) {
                startingRow.put(s, ((double) TransitionCounter.get(currentTransition)) / numSequences);
            } else {
                startingRow.put(s, 0.0);
            }
        }

        startingRow.put(EndState,0.0);
        startingRow.put(StartState,0.0);
        TransitionProbabilities.put(StartState,startingRow);

        for (Feature s1 : TotalStateOccurences.keySet()){
            Map<Feature, Double> currentRow= new HashMap<>();

            for (Feature s2 : TotalStateOccurences.keySet()){
                AbstractMap.SimpleEntry<Feature,Feature> currentTransition  = new AbstractMap.SimpleEntry<>(s1,s2);
                if (TransitionCounter.containsKey(currentTransition)) {
                    currentRow.put(s2, ((double) TransitionCounter.get(currentTransition)) / TotalStateOccurences.get(s1));
                } else {
                    currentRow.put(s2,0.0);
                }
            }
            currentRow.put(StartState,0.0);
            TransitionProbabilities.put(s1,currentRow);
        }

        Map<Feature, Map<AminoAcid, Double>> ObservationProbabilities = new HashMap<>();


        for (AbstractMap.SimpleEntry<Feature,AminoAcid> pair: StateObservationCounter.keySet()){

            Feature currentState = pair.getKey();
            AminoAcid currentObservation = pair.getValue();
            Double probability = ((double) StateObservationCounter.get(pair))/ TotalStateOccurences.get(currentState);

            if (ObservationProbabilities.containsKey(currentState)){
                Map<AminoAcid, Double> currentRow =  ObservationProbabilities.get(currentState);
                currentRow.put(currentObservation,probability);
            } else {
                HashMap<AminoAcid, Double> newRow = new HashMap<>();
                newRow.put(currentObservation,probability);
                newRow.put(StartSymbol,0.0);
                ObservationProbabilities.put(currentState,newRow);

            }
        }

        Map<AminoAcid, Double> startStateRow = new HashMap<>();
        startStateRow.put(StartSymbol,1.0);
        ObservationProbabilities.put(StartState, startStateRow);

        for(Feature State: TotalStateOccurences.keySet()){
            for(AminoAcid Word: Alphabet){
                if (!(ObservationProbabilities.get(State).containsKey(Word))){
                    ObservationProbabilities.get(State).put(Word,0.0);
                }
            }
        }
        for(AminoAcid Word: Alphabet){
            if (!(ObservationProbabilities.get(StartState).containsKey(Word))){
                ObservationProbabilities.get(StartState).put(Word,0.0);
            }
        }


        return new HiddenMarkovModel<AminoAcid,Feature>(TransitionProbabilities,ObservationProbabilities);



    }

    /**
     * Uses the Viterbi algorithm to calculate the most likely single sequence
     * of hidden states given the observed sequence.
     *
     * @param model            A pre-trained HMM.
     * @param observedSequence {@link List}<{@link AminoAcid}> A sequence of observed amino
     *                         acids
     * @return {@link List}<{@link Feature}> The most likely single sequence of
     * hidden states
     */
    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
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
     * sequences in testSequencePairs.
     *
     * @param model             The HMM model.
     * @param testSequencePairs
     * @return {@link Map}<{@link List}<{@link Feature}>,
     * {@link Feature}<{@link Feature}>> A map from a real hidden
     * sequence to the equivalent estimated hidden sequence.
     * @throws IOException
     */
    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> results = new HashMap<>();

        for (HMMDataStore<AminoAcid,Feature> sequence : testSequencePairs) {
            results.put(sequence.hiddenSequence,viterbi(model,sequence.observedSequence));
        }

        return results;
    }

    /**
     * Calculates the precision of the estimated sequence with respect to the
     * membrane state, i.e. the proportion of predicted membrane states that
     * were actually in the membrane.
     *
     * @param true2PredictedMap {@link Map}<{@link List}<{@link Feature}>,
     *                          {@link List}<{@link Feature}>> A map from a real hidden
     *                          sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The precision of the estimated sequence with
     * respect to the membrane state averaged over all the test
     * sequences.
     */
    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        int numPredictedWeighted = 0;
        int actualWeighted = 0;

        for(List<Feature> trueSequence: true2PredictedMap.keySet()){
            List<Feature> predictedSequence = true2PredictedMap.get(trueSequence);
            for (int i = 0; i < predictedSequence.size();i++){
                if (predictedSequence.get(i) == Feature.MEMBRANE){
                    numPredictedWeighted += 1;
                    if (trueSequence.get(i) == Feature.MEMBRANE){
                        actualWeighted += 1;
                    }
                }
            }
        }

        return ((double) actualWeighted/numPredictedWeighted);
    }

    /**
     * Calculate the recall for the membrane state.
     *
     * @param true2PredictedMap {@link Map}<{@link List}<{@link Feature}>,
     *                          {@link List}<{@link Feature}>> A map from a real hidden
     *                          sequence to the equivalent estimated hidden sequence.
     * @return The recall for the membrane state.
     */
    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        int numWeighted = 0;
        int numPredictedWeighted = 0;

        for(List<Feature> trueSequence: true2PredictedMap.keySet()){
            List<Feature> predictedSequence = true2PredictedMap.get(trueSequence);
            for (int i = 0; i < predictedSequence.size();i++){
                if (trueSequence.get(i) == Feature.MEMBRANE){
                    numWeighted += 1;
                    if (predictedSequence.get(i) == Feature.MEMBRANE){
                        numPredictedWeighted += 1;
                    }
                }
            }
        }

        return ((double) numPredictedWeighted/numWeighted);
    }

    /**
     * Calculate the F1 score for the membrane state.
     *
     * @param true2PredictedMap
     */
    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        return 2 * ((precision * recall) /(precision + recall));
    }
}
