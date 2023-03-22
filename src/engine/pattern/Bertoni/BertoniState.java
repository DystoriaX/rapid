package engine.pattern.Bertoni;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import engine.pattern.State;
import event.Thread;
import event.Lock;
import event.Variable;
import util.vectorclock.VectorClock;

public class BertoniState extends State {
    private HashMap<Thread, Integer> threadToIndex = new HashMap<>();
    public HashMap<Integer, HashSet<Integer>> pattern = new HashMap<>();

    private int numThreads;
    private int k;

    private HashMap<Thread, VectorClock> threadClock = new HashMap<>();
    private HashMap<Variable, VectorClock> readClock = new HashMap<>();
    private HashMap<Variable, VectorClock> writeClock = new HashMap<>();
    private HashMap<Lock, VectorClock> lockClock = new HashMap<>();

    public HashMap<Thread, ArrayList<VectorClock>> history = new HashMap<>();
    public TreeMap<TotalOrderVectorClock, Integer> idealToNonTerm = new TreeMap<>();
    public HashMap<Integer, HashMap<Integer, Integer>> specialSym = new HashMap<>();
    public TreeSet<Ideal> ideals = new TreeSet<>();

    private HashSet<ArrayList<Thread>> combinations = new HashSet<>();

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
        
        idealToNonTerm.put(new TotalOrderVectorClock(emptyClock().getClock()), -1);

        int cnt = 0;
        for(int p : pattern) {
            if(!this.pattern.containsKey(p)) {
                this.pattern.put(p, new HashSet<Integer>());
            }
            this.pattern.get(p).add(cnt++);
        }
        this.k = pattern.size();
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
                        locId);
        }
        ideals = generateIdeals(threadClock.get(thread), thread);

        
        for(Ideal ideal: ideals) {
            int nonterm = -1;
            for(int thr: ideal.maximalThreads) {
                Vector<Integer> clock = new Vector<>(ideal.getClock());
                clock.set(thr, clock.get(thr) - 1);
                TotalOrderVectorClock vc = new TotalOrderVectorClock(clock);
                if(!idealToNonTerm.containsKey(vc)) {
                    System.out.println(ideals.size());
                    System.out.println(vc);
                    System.out.println(idealToNonTerm.size());
                    throw new IllegalArgumentException("Wrong Implementation! Earlier ideals should have been computed!");
                }
                if(nonterm < idealToNonTerm.get(vc)) {
                    nonterm = idealToNonTerm.get(vc);
                }
                if(specialSym.get(thr).containsKey(ideal.getClock().get(thr))) {
                    int loc = specialSym.get(thr).get(ideal.getClock().get(thr));
                    for(int sym : pattern.get(loc)) {
                        if(sym == 0 || (idealToNonTerm.get(vc) == sym - 1)) {
                            if(sym == k - 1) {
                                return true;
                            }
                            if(nonterm < sym) {
                                nonterm = sym;
                            }
                        }
                    }
                }
            }
            if(nonterm >= -1) {
                idealToNonTerm.put(ideal.totclock, nonterm);
            }
        }
        return false;
    }

    private TreeSet<Ideal> generateIdeals(VectorClock vc, Thread thread) {
        ideals = new TreeSet<>();
        ideals.add(new Ideal(new TotalOrderVectorClock(vc.getClock()), new HashSet<Integer>(Arrays.asList(threadToIndex.get(thread)))));
        for(ArrayList<Thread> combination: combinations) {
            if(combination.contains(thread)) {
                continue;
            }
            HashMap<Integer, VectorClock> vcs = new HashMap<>();
            vcs.put(threadToIndex.get(thread), vc);
            generateIdeal(combination, 0, vcs, thread);
        }
        return ideals;
    }

    private void generateIdeal(ArrayList<Thread> combination, int index, HashMap<Integer, VectorClock> vcs, Thread thread) {
        if(index == combination.size()) {
            VectorClock vc = new VectorClock(numThreads);
            vc.updateWithMax(vcs.values().toArray(new VectorClock[0]));
            HashSet<Integer> maxThreads = getMaximalThreads(vcs);
            if(maxThreads.size() == combination.size() + 1) {
                ideals.add(new Ideal(new TotalOrderVectorClock(vc.getClock()), maxThreads));
            }
        }
        else {
            Thread thr = combination.get(index);
            for(int i = vcs.get(threadToIndex.get(thread)).getClockIndex(threadToIndex.get(thr)); i < history.get(thr).size(); i++) {
                vcs.put(threadToIndex.get(thr), history.get(thr).get(i));
                generateIdeal(combination, index + 1, vcs, thread);
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
        System.out.println(idealToNonTerm.keySet().size());
        System.out.println(ideals.size());
    }
}

class TotalOrderVectorClock implements Comparable<TotalOrderVectorClock> {

    public Vector<Integer> clock;
    public int dim;
    public int sum;

    public TotalOrderVectorClock(Vector<Integer> vc) {
        clock = new Vector<>(vc);
        dim = clock.size();
        sum = 0;
        for(int i: clock) {
            sum += i;
        }
    }

    @Override
	public int compareTo(TotalOrderVectorClock other) {
        if (!(this.dim == other.dim)) {
			throw new IllegalArgumentException("Mismatch in this.dim and argument.dim");
		}
        int sumDiff = this.sum - other.sum;
		if (sumDiff == 0) {
			for(int i = 0; i < this.clock.size(); i++) {
                int diff = this.clock.get(i) - other.clock.get(i);
                if(diff != 0) {
                    return diff;
                }
            }
            return 0;
		} else {
            return sumDiff;
        }
	}

    @Override
    public String toString() {
        return clock.toString() + " " + sum;
    }
}

class Ideal implements Comparable<Ideal> {

    public TotalOrderVectorClock totclock;
    public HashSet<Integer> maximalThreads;

    public Ideal(TotalOrderVectorClock vc, HashSet<Integer> mt) {
        totclock = vc;
        maximalThreads = mt;
    }

    public Vector<Integer> getClock() {
        return totclock.clock;
    }

    @Override
	public int compareTo(Ideal other) {
        return this.totclock.compareTo(other.totclock);
	}

    @Override
    public String toString() {
        return totclock.toString() + ", " + maximalThreads.toString();
    }
}
