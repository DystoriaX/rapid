package engine.pattern.OptimizedPatternTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import engine.pattern.State;
import event.Lock;
import event.Thread;
import event.Variable;
import util.Trie;
import util.vectorclock.VectorClock;

public class OptimizedVectorClockState extends State {
    private HashMap<Thread, Integer> threadToIndex = new HashMap<>();
    public HashMap<ArrayList<Integer>, ArrayList<VectorClock>> currentState = new HashMap<>();

    // Maps from linecode to set of indices???
    public HashMap<Integer, HashSet<Integer>> pattern = new HashMap<>();

    private int numThreads;
    private int k;

    private HashMap<Thread, VectorClock> threadClock = new HashMap<>();
    private HashMap<Variable, VectorClock> readClock = new HashMap<>();
    private HashMap<Variable, VectorClock> writeClock = new HashMap<>();
    private HashMap<Lock, VectorClock> lockClock = new HashMap<>();

    private Trie<Integer> patternTrie = new Trie<>();
    private HashSet<ArrayList<Integer>> partialCandidates = new HashSet<>();
    private HashMap<Integer, HashSet<ArrayList<Integer>>> prefixesEndsWith = new HashMap<>();

    public OptimizedVectorClockState(HashSet<Thread> tSet, ArrayList<ArrayList<Integer>> patterns) {
        // Populate threads
        numThreads = tSet.size();
        Iterator<Thread> itThread = tSet.iterator();
        int index = 0;
        while (itThread.hasNext()) {
            Thread thr = itThread.next();
            threadToIndex.put(thr, index);
            index++;
            threadClock.put(thr, emptyClock());
        }

        // Populate table of permutation to after sets (VC)
        currentState.put(new ArrayList<>(), new ArrayList<>());

        // Maps each line of code in a pattern into an index
        for (ArrayList<Integer> pattern : patterns) {
            patternTrie.add(pattern);

            // Assumption: all pattern has the same size
            this.k = pattern.size();
        }

        for (ArrayList<Integer> pattern : patterns) {
            ArrayList<Integer> ids = patternTrie.getIds(pattern);

            for (int i = 0; i < pattern.size(); i++) {
                int p = pattern.get(i);
                if (!this.pattern.containsKey(p)) {
                    this.pattern.put(p, new HashSet<>());
                }

                this.pattern.get(p).add(ids.get(i));
            }

            generatePattern(ids);
        }
    }

    private final void generatePattern(ArrayList<Integer> pattern) {
        // Heap's algorithm

        int i = 0;
        int length = pattern.size();
        Integer[] c = new Integer[length];
        Arrays.fill(c, 0);

        populatePartialCandidates(pattern);
        while (i < length) {
            if (c[i] < i) {
                if (i % 2 == 0) {
                    // Swap pattern[0] and pattern[i]
                    int tmp = pattern.get(0);
                    pattern.set(0, pattern.get(i));
                    pattern.set(i, tmp);
                } else {
                    // Swap pattern[c[i]] and pattern[i]
                    int tmp = pattern.get(c[i]);
                    pattern.set(c[i], pattern.get(i));
                    pattern.set(i, tmp);
                }

                // Add permutation
                populatePartialCandidates(pattern);
                c[i] = c[i] + 1;
                i = 0;
            } else {
                c[i] = 0;
                i = i + 1;
            }
        }
        for (ArrayList<Integer> candidate : partialCandidates) {
            System.out.println(candidate);
        }
    }

    private final void populatePartialCandidates(ArrayList<Integer> pattern) {
        // Get all the prefixes
        ArrayList<Integer> prefix = new ArrayList<>();

        System.out.println("Pattern: " + pattern);
        for (int i = 0; i < pattern.size(); i++) {
            int currentLabel = pattern.get(i);

            if (!prefixesEndsWith.containsKey(currentLabel)) {
                prefixesEndsWith.put(currentLabel, new HashSet<>());
            }

            // Needs a copy
            prefixesEndsWith.get(currentLabel).add(new ArrayList<>(prefix));

            prefix.add(currentLabel);

            // Needs a copy
            partialCandidates.add(new ArrayList<>(prefix));
        }
    }

    public VectorClock emptyClock() {
        return new VectorClock(numThreads);
    }

    public VectorClock getThreadClock(Thread t) {
        return threadClock.get(t);
    }

    public VectorClock getReadClock(Variable v) {
        if (!readClock.containsKey(v)) {
            readClock.put(v, emptyClock());
        }
        return readClock.get(v);
    }

    public VectorClock getWriteClock(Variable v) {
        if (!writeClock.containsKey(v)) {
            writeClock.put(v, emptyClock());
        }
        return writeClock.get(v);
    }

    public VectorClock getLockClock(Lock l) {
        if (!lockClock.containsKey(l)) {
            lockClock.put(l, emptyClock());
        }
        return lockClock.get(l);
    }

    public int getThreadIndex(Thread thread) {
        return threadToIndex.get(thread);
    }

    public boolean extendWitness(int locId, Thread thread, VectorClock vc) {
        if (pattern.containsKey(locId)) {
            HashMap<ArrayList<Integer>, ArrayList<VectorClock>> newStates = new HashMap<>();

            // pattern maps each location to a particular index. Reason: multiple same
            // location will have different indices
            for (int index : pattern.get(locId)) {

                // Equals to the permutation of the patterns
                for (ArrayList<Integer> witness : prefixesEndsWith.get(index)) {
                    if (currentState.containsKey(witness) && witnesses(index, vc, witness, currentState.get(witness))) {
                        if (witness.size() == k - 1) {
                            return true;
                        }

                        // Add the new event as part of the permutation
                        ArrayList<Integer> extendedWitness = new ArrayList<>();
                        extendedWitness.addAll(witness);
                        extendedWitness.add(index);

                        // Add it to after set
                        ArrayList<VectorClock> extendedTimeStamps = new ArrayList<>();
                        extendedTimeStamps.addAll(currentState.get(witness));
                        extendedTimeStamps.add(vc);
                        newStates.put(extendedWitness, extendedTimeStamps);
                    }
                }
            }
            currentState.putAll(newStates);
        }
        return false;
    }

    private boolean witnesses(int index, VectorClock vc, ArrayList<Integer> witness,
            ArrayList<VectorClock> timestamps) {
        Iterator<Integer> itWitness = witness.iterator();
        Iterator<VectorClock> itTimestamp = timestamps.iterator();
        while (itWitness.hasNext()) {
            Integer witnessIndex = itWitness.next();
            if (witnessIndex == index) {
                return false;
            }
            VectorClock timeStamp = itTimestamp.next();
            if (witnessIndex > index &&
                    timeStamp.isLessThanOrEqual(vc)) {
                return false;
            }
        }
        return true;
    }

    public void printMemory() {
        // for(Thread t: threadClock.keySet())
        // System.out.println(threadClock.get(t));
    }

}
