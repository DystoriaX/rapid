package engine.pattern.Bertoni;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import engine.pattern.State;
import event.Thread;
import event.Lock;
import event.Variable;
import util.vectorclock.VectorClock;

public class BertoniState extends State {
    private HashMap<Thread, Integer> threadToIndex = new HashMap<>();
    public HashMap<Integer, Integer> pattern = new HashMap<>();

    private int numThreads;

    private HashMap<Thread, VectorClock> threadClock = new HashMap<>();
    private HashMap<Variable, VectorClock> readClock = new HashMap<>();
    private HashMap<Variable, VectorClock> writeClock = new HashMap<>();
    private HashMap<Lock, VectorClock> lockClock = new HashMap<>();

    public HashMap<Thread, ArrayList<VectorClock>> history = new HashMap<>();
    public HashMap<Vector<Integer>, Integer> idealToNonTerm = new HashMap<>();
    public HashMap<Integer, HashMap<Integer, Integer>> specialSym = new HashMap<>();

    private HashSet<ArrayList<Thread>> combinations = new HashSet<>();

    private int idealsNum = 0;

    public BertoniState(HashSet<Thread> tSet, ArrayList<Integer> pattern) {
        numThreads = tSet.size();
        Iterator<Thread> itThread = tSet.iterator();
        int index = 0;
        while(itThread.hasNext()) {
            Thread thr = itThread.next();
            threadToIndex.put(thr, index);
            threadClock.put(thr, emptyClock());
            history.put(thr, new ArrayList<VectorClock>());
            specialSym.put(index, new HashMap<>());
            index++;
        }
        
        idealToNonTerm.put(emptyClock().getClock(), -1);

        Iterator<Integer> it = pattern.iterator();
        int cnt = 0;
        while (it.hasNext()) {
            this.pattern.put(it.next(), cnt++);
        }
        generateCombinations(new ArrayList<Thread>(), new ArrayList<Thread>(tSet), 0);
    }

    private void generateCombinations(ArrayList<Thread> base, ArrayList<Thread> candidates, int index) {
        for(int i = index; i < candidates.size(); i++) {
            ArrayList<Thread> combination = new ArrayList<>(base);
            combination.add(candidates.get(i));
            combinations.add(combination);
            generateCombinations(combination, candidates, i + 1);
        }
    }

    public VectorClock emptyClock() {
        return new VectorClock(numThreads);
    }

    private HashSet<Integer> zeroSet() {
        HashSet<Integer> initialSym = new HashSet<>();
        initialSym.add(0);
        return initialSym;
    }

    public VectorClock getThreadClock(Thread t) {
        return threadClock.get(t);
    }

    public VectorClock getReadClock(Variable v) {
        if(!readClock.containsKey(v)) {
            readClock.put(v, emptyClock());
        }
        return readClock.get(v);
    }

    public VectorClock getWriteClock(Variable v) {
        if(!writeClock.containsKey(v)) {
            writeClock.put(v, emptyClock());
        }
        return writeClock.get(v);
    }

    public VectorClock getLockClock(Lock l) {
        if(!lockClock.containsKey(l)) {
            lockClock.put(l, emptyClock());
        }
        return lockClock.get(l);
    }

    public int getThreadIndex(Thread thread) {
        return threadToIndex.get(thread);
    }

    public boolean computeNonTerm(int locId, Thread thread) {
        if(pattern.containsKey(locId)) {
            specialSym.get(threadToIndex.get(thread)).
                    put(threadClock.get(thread).
                            getClockIndex(threadToIndex.get(thread)), 
                        pattern.get(locId));
        }
        ArrayList<Ideal> ideals = generateIdeals(threadClock.get(thread), thread);
        Collections.sort(ideals);
        idealsNum = ideals.size();

        // System.out.println(ideals);
        for(Ideal ideal: ideals) {
            int nonterm = -1;
            for(int thr: ideal.maximalThreads) {
                // System.out.println(ideal + " " + thr);
                Vector<Integer> clock = new Vector<>(ideal.clock);
                clock.set(thr, clock.get(thr) - 1);
                if(idealToNonTerm.containsKey(clock) && nonterm < idealToNonTerm.get(clock)) {
                    nonterm = idealToNonTerm.get(clock);
                }
                if(specialSym.get(thr).containsKey(ideal.clock.get(thr))) {
                    int sym = specialSym.get(thr).get(ideal.clock.get(thr));
                    // System.out.println(sym);
                    if(sym == 0 || (idealToNonTerm.containsKey(clock) && idealToNonTerm.get(clock) == sym - 1)) {
                        if(sym == pattern.size() - 1) {
                            return true;
                        }
                        if(nonterm < sym) {
                            nonterm = sym;
                        }
                    }
                }
            }
            if(nonterm >= 0) {
                idealToNonTerm.put(ideal.clock, nonterm);
            }
        }
        // System.out.println(idealToNonTerm);
        return false;
    }

    private ArrayList<Ideal> generateIdeals(VectorClock vc, Thread thread) {
        HashSet<Ideal> ideals = new HashSet<>();
        ideals.add(new Ideal(new Vector<>(vc.getClock()), new HashSet<Integer>(Arrays.asList(threadToIndex.get(thread)))));
        for(ArrayList<Thread> combination: combinations) {
            if(combination.contains(thread)) {
                continue;
            }
            HashMap<Integer, VectorClock> vcs = new HashMap<>();
            vcs.put(threadToIndex.get(thread), vc);
            generateIdeal(combination, 0, ideals, vcs, thread);
        }
        return new ArrayList<Ideal>(ideals);
    }

    private void generateIdeal(ArrayList<Thread> combination, int index, HashSet<Ideal> ideals, HashMap<Integer, VectorClock> vcs, Thread thread) {
        if(index == combination.size()) {
            VectorClock vc = new VectorClock(numThreads);
            vc.updateWithMax(vcs.values().toArray(new VectorClock[0]));
            ideals.add(new Ideal(vc.getClock(), getMaximalThreads(vcs)));
        }
        else {
            Thread thr = combination.get(index);
            for(int i = vcs.get(threadToIndex.get(thread)).getClockIndex(threadToIndex.get(thr)); i < history.get(thr).size(); i++) {
                vcs.put(threadToIndex.get(thr), history.get(thr).get(i));
                generateIdeal(combination, index + 1, ideals, vcs, thread);
                vcs.remove(threadToIndex.get(thr));
            }
        }
    }

    private HashSet<Integer> getMaximalThreads(HashMap<Integer, VectorClock> vcs) {
        HashSet<Integer> maximalThreads = new HashSet<>();
        for(int thr: vcs.keySet()) {
            boolean flag = true;
            for(int thr2: vcs.keySet()) {
                if(thr == thr2) {
                    continue;
                }
                if(vcs.get(thr).isLessThanOrEqual(vcs.get(thr2))) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                maximalThreads.add(thr);
            }
        }
        return maximalThreads;
    }

    public void printMemory() {
        // System.out.println(idealsNum);
    }
}

class Ideal implements Comparable<Ideal> {

    public Vector<Integer> clock;
    public HashSet<Integer> maximalThreads;

    public Ideal(Vector<Integer> vc, HashSet<Integer> mt) {
        clock = vc;
        maximalThreads = mt;
    }

    public int sum() {
        return clock.stream().reduce(0, Integer::sum);
    }

    @Override
    public int hashCode() {
        return clock.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    @Override
	public int compareTo(Ideal ideal) {
		if (this.sum() == ideal.sum()) {
			return 0;
		} else if (this.sum() < ideal.sum()) {
			return -1;
		} else
			return 1;
	}

    @Override
    public String toString() {
        return clock.toString() + ", " + maximalThreads.toString();
    }
}
