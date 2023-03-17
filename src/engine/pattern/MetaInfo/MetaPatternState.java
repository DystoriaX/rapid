package engine.pattern.MetaInfo;

import java.util.ArrayList;
import java.util.HashSet;

import event.Thread;
import engine.pattern.State;

public class MetaPatternState extends State {
    private int numOfThreads;
    private long numOfLocations;
    private long numOfEvents;

    ArrayList<ArrayList<Integer>> patterns;
    ArrayList<Long> numOfLocationsInPattern = new ArrayList<>();

    public MetaPatternState(HashSet<Thread> tSet, ArrayList<ArrayList<Integer>> patterns, int numOfLocations) {
        numOfThreads = tSet.size();
        this.numOfLocations = numOfLocations;
        numOfEvents = 0;
        for(int i = 0; i < 10; i++) {
            numOfLocationsInPattern.add((long)0);
        }
        this.patterns = patterns;
    }

    public void update(int locId) {
        for(int i = 0; i < 10; i++) {
            if(patterns.get(i).contains(locId)) {
                numOfLocationsInPattern.set(i, numOfLocationsInPattern.get(i) + 1);
            }
        }
        numOfEvents++;
    }

    public void printMemory() {
        System.out.println(numOfEvents);
        System.out.println(numOfLocations);
        System.out.println(numOfThreads);
        for(int i = 0; i < 10; i++) {
            System.out.println(numOfLocationsInPattern.get(i));
        }
    }
}
