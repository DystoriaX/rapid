package engine.pattern.OptimizedPatternTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.function.Consumer;

import engine.pattern.State;
import event.Lock;
import event.Thread;
import event.Variable;
import util.DAG;
import util.vectorclock.VectorClock;

public class OptimizedVectorClockState extends State {
    private HashMap<Thread, Integer> threadToIndex = new HashMap<>();
    public HashMap<ArrayList<Integer>, ArrayList<VectorClock>> currentState = new HashMap<>();

    // Maps from locIds to set of indices???
    public HashMap<Integer, HashSet<Integer>> pattern = new HashMap<>();

    private int numThreads;
    private int k;

    private HashMap<Thread, VectorClock> threadClock = new HashMap<>();
    private HashMap<Variable, VectorClock> readClock = new HashMap<>();
    private HashMap<Variable, VectorClock> writeClock = new HashMap<>();
    private HashMap<Lock, VectorClock> lockClock = new HashMap<>();

    private HashSet<ArrayList<Integer>> partialCandidates = new HashSet<>();
    private HashMap<Integer, HashSet<ArrayList<Integer>>> prefixesEndsWith = new HashMap<>();

    private HashSet<ArrayList<Integer>> bugs = new HashSet<>();

    public OptimizedVectorClockState(HashSet<Thread> tSet, DAG<Integer> patternG) {
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

        currentState.put(new ArrayList<>(), new ArrayList<>());

        Stack<ArrayList<ArrayList<Integer>>> candidateStack = new Stack<>();
        candidateStack.push(new ArrayList<>(Collections.singleton(new ArrayList<>())));

        Consumer<DAG<Integer>.Node> preSearch = (DAG<Integer>.Node u) -> {
            ArrayList<ArrayList<Integer>> newCandidates = new ArrayList<>();

            for (ArrayList<ArrayList<Integer>> candidateList : candidateStack) {
                for (ArrayList<Integer> candidate : candidateList) {
                    // Try insert the new id on the candidate
                    for (int i = 0; i <= candidate.size(); i++) {
                        ArrayList<Integer> newCandidate = new ArrayList<>(candidate);
                        newCandidate.add(i, u.id);
                        newCandidates.add(newCandidate);
                    }
                }
            }

            candidateStack.push(newCandidates);

            // Update partial candidates
            partialCandidates.addAll(newCandidates);

            // Update prefixesEndsWith
            for (ArrayList<Integer> candidate : newCandidates) {
                int lastElement = candidate.get(candidate.size() - 1);
                ArrayList<Integer> prefix = new ArrayList<>(candidate.subList(0, candidate.size() - 1));

                prefixesEndsWith.putIfAbsent(lastElement, new HashSet<>());
                prefixesEndsWith.get(lastElement).add(prefix);
            }

            // Update mapping from locId to id for patterns
            pattern.putIfAbsent(u.data, new HashSet<>());
            pattern.get(u.data).add(u.id);
        };

        Consumer<DAG<Integer>.Node> postSearch = (DAG<Integer>.Node u) -> {
            candidateStack.pop();
        };

        patternG.dfs(preSearch, postSearch);

        this.k = 0;

        for (ArrayList<Integer> pc : partialCandidates) {
            this.k = Math.max(this.k, pc.size());
        }

        for (ArrayList<Integer> pc : partialCandidates) {
            if (pc.size() != this.k) {
                continue;
            }

            ArrayList<Integer> npc = new ArrayList<>(pc);
            bugs.add(npc);
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
                            ArrayList<Integer> bugFound = new ArrayList<>(witness);
                            bugFound.add(index);
                            bugs.remove(bugFound);

                            // Found all bugs
                            if (bugs.isEmpty()) {
                                return true;
                            }
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
