package engine.pattern.Vectorclock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import engine.pattern.State;
import event.Thread;
import event.Lock;
import event.Variable;
import util.vectorclock.VectorClock;

public class VectorClockState extends State {
    private HashMap<Thread, Integer> threadToIndex = new HashMap<>();
    public HashMap<ArrayList<String> , ArrayList<VectorClock>> currentState = new HashMap<>();
    public HashMap<String, Integer> pattern = new HashMap<>();

    private int numThreads;

    private HashMap<Thread, VectorClock> threadClock = new HashMap<>();
    private HashMap<Variable, VectorClock> readClock = new HashMap<>();
    private HashMap<Variable, VectorClock> writeClock = new HashMap<>();
    private HashMap<Lock, VectorClock> lockClock = new HashMap<>();

    public VectorClockState(HashSet<Thread> tSet, ArrayList<String> pattern) {
        numThreads = tSet.size();
        Iterator<Thread> itThread = tSet.iterator();
        int index = 0;
        while(itThread.hasNext()) {
            Thread thr = itThread.next();
            threadToIndex.put(thr, index);
            index++;
            threadClock.put(thr, emptyClock());
        }
        currentState.put(new ArrayList<>(), new ArrayList<>());
        ListIterator<String> it = pattern.listIterator();
        while (it.hasNext()) {
            this.pattern.put(it.next(), it.nextIndex());
        }
        System.out.println(this.pattern);
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

    public boolean extendWitness(String event, VectorClock vc) {
        if(pattern.containsKey(event)) {
            HashMap<ArrayList<String> , ArrayList<VectorClock>> newStates = new HashMap<>();
            for(ArrayList<String> witness: currentState.keySet()) {
                if(witnesses(event, vc, witness, currentState.get(witness))) {
                    if(witness.size() == pattern.keySet().size() - 1) {
                        return true;
                    }

                    ArrayList<String> extendedWitness = new ArrayList<>();
                    extendedWitness.addAll(witness);
                    extendedWitness.add(event);
                    ArrayList<VectorClock> extendedTimeStamps = new ArrayList<>();
                    extendedTimeStamps.addAll(currentState.get(witness));
                    extendedTimeStamps.add(vc);
                    newStates.put(extendedWitness, extendedTimeStamps);
                }
            }
            currentState.putAll(newStates);
        }
        return false;
    }

    private boolean witnesses(String event, VectorClock vc, ArrayList<String> witness, ArrayList<VectorClock> timestamps) {
        if(witness.contains(event)) {
            return false;
        }
        Iterator<String> itWitness = witness.iterator();
        Iterator<VectorClock> itTimestamp = timestamps.iterator();
        while(itWitness.hasNext()) {
            String event2 = itWitness.next();
            VectorClock timeStamp = itTimestamp.next();
            if(pattern.get(event2) > pattern.get(event) &&
                timeStamp.isLessThanOrEqual(vc)) {
                    return false;
                }
        }
        return true;
    }

    public void printMemory() {
        System.out.println(currentState);
    }
}

