package uk.ac.cam.cl.mk2030.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;


public class Exercise7 implements IExercise7 {
    /**
     * Loads the sequences of visible and hidden states from the sequence files
     * (visible dice rolls on first line and hidden dice types on second) and uses
     * them to estimate the parameters of the Hidden Markov Model that generated
     * them.
     *
     * @param sequenceFiles {@link Collection}<{@link Path}> The files containing dice roll
     *   wilder tyson                 sequences
     * @return {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}> The
     * estimated model
     * @throws IOException
     */
    @Override
    public  HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
        List<HMMDataStore<DiceRoll,DiceType>> sequenceList = HMMDataStore.loadDiceFiles(sequenceFiles);

        Set<DiceRoll> Alphabet = new HashSet<>();

        Map<AbstractMap.SimpleEntry,Integer> TransitionCounter = new HashMap<>();
        Map<SimpleEntry<DiceType,DiceRoll>,Integer> StateObservationCounter = new HashMap<>();
        Map<DiceType, Integer> TotalStateOccurences = new HashMap<>();

        List<DiceType> firstStates = sequenceList.get(0).hiddenSequence;
        DiceType StartState = firstStates.get(0);
        DiceType EndState = firstStates.get(firstStates.size() - 1);

        List<DiceRoll> firstObservations = sequenceList.get(0).observedSequence;
        DiceRoll StartSymbol = firstObservations.get(0);
        DiceRoll EndSymbol = firstObservations.get(firstObservations.size() - 1);

        int numSequences = sequenceList.size();

        Alphabet.add(StartSymbol);


        for (HMMDataStore<DiceRoll,DiceType> sequence : sequenceList) {

            List<DiceRoll> observed = sequence.observedSequence;
            List<DiceType> hidden = sequence.hiddenSequence;

            for (int i = 1; i < observed.size(); i++) {

                DiceType previousState = hidden.get(i - 1);
                DiceType currentState = hidden.get(i);
                DiceRoll currentObservation = observed.get(i);

                Alphabet.add(currentObservation);


                if (TotalStateOccurences.containsKey(currentState)) {
                    TotalStateOccurences.put(currentState, TotalStateOccurences.get(currentState) + 1);
                } else {
                    TotalStateOccurences.put(currentState, 1);
                }

                SimpleEntry<DiceType, DiceType> transitionPair = new SimpleEntry<DiceType, DiceType>(previousState, currentState);
                SimpleEntry<DiceType, DiceRoll> observationPair = new SimpleEntry<DiceType, DiceRoll>(currentState, currentObservation);


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

            Map<DiceType,Map<DiceType,Double>> TransitionProbabilities = new HashMap<>();

            Map<DiceType,Double> startingRow = new HashMap<>();

            for(DiceType s: TotalStateOccurences.keySet()){
                SimpleEntry<DiceType,DiceType> currentTransition  = new SimpleEntry<>(StartState,s);
                if (TransitionCounter.containsKey(currentTransition)) {
                    startingRow.put(s, ((double) TransitionCounter.get(currentTransition)) / numSequences);
                } else {
                    startingRow.put(s, 0.0);
                }
            }

            startingRow.put(EndState,0.0);
            startingRow.put(StartState,0.0);
            TransitionProbabilities.put(StartState,startingRow);

            for (DiceType s1 : TotalStateOccurences.keySet()){
                Map<DiceType,Double> currentRow= new HashMap<>();

                for (DiceType s2 : TotalStateOccurences.keySet()){
                    SimpleEntry<DiceType,DiceType> currentTransition  = new SimpleEntry<>(s1,s2);
                    if (TransitionCounter.containsKey(currentTransition)) {
                        currentRow.put(s2, ((double) TransitionCounter.get(currentTransition)) / TotalStateOccurences.get(s1));
                    } else {
                        currentRow.put(s2,0.0);
                    }
                }
                currentRow.put(StartState,0.0);
                TransitionProbabilities.put(s1,currentRow);
            }

            Map<DiceType,Map<DiceRoll,Double>> ObservationProbabilities = new HashMap<>();


            for (SimpleEntry<DiceType,DiceRoll> pair: StateObservationCounter.keySet()){

                DiceType currentState = pair.getKey();
                DiceRoll currentObservation = pair.getValue();
                Double probability = ((double) StateObservationCounter.get(pair))/ TotalStateOccurences.get(currentState);

                if (ObservationProbabilities.containsKey(currentState)){
                    Map<DiceRoll,Double > currentRow =  ObservationProbabilities.get(currentState);
                    currentRow.put(currentObservation,probability);
                } else {
                    HashMap<DiceRoll,Double> newRow = new HashMap<>();
                    newRow.put(currentObservation,probability);
                    newRow.put(StartSymbol,0.0);
                    ObservationProbabilities.put(currentState,newRow);

                }
            }

            HashMap<DiceRoll,Double> startStateRow = new HashMap<>();
            startStateRow.put(StartSymbol,1.0);
            ObservationProbabilities.put(StartState, startStateRow);

            for(DiceType State: TotalStateOccurences.keySet()){
                for(DiceRoll Word: Alphabet){
                    if (!(ObservationProbabilities.get(State).containsKey(Word))){
                        ObservationProbabilities.get(State).put(Word,0.0);
                    }
                }
            }
        for(DiceRoll Word: Alphabet){
            if (!(ObservationProbabilities.get(StartState).containsKey(Word))){
                ObservationProbabilities.get(StartState).put(Word,0.0);
            }
        }


            return new HiddenMarkovModel<DiceRoll,DiceType>(TransitionProbabilities,ObservationProbabilities);



        }
    }


