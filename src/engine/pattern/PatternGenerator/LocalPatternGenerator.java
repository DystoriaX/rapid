package engine.pattern.PatternGenerator;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import event.Thread;

public class LocalPatternGenerator extends PatternGenerator {

    private long interval;

    public LocalPatternGenerator(int k, String sourceFile, String patternFile, int number, double proportion) {
        super(k, sourceFile, patternFile, number);
        this.interval = (long)(proportion * numOfEvents);
    }

    @Override
    public boolean generatePattern(ArrayList<String> pattern) {
        long start = 0;
        while((start = (long)(Math.random() * numOfEvents)) + interval < numOfEvents) {}

        HashMap<Thread, HashSet<Integer>> candidates = new HashMap<>();
        HashMap<Integer, Thread> indexToThread = new HashMap<>();
        int index = 0;
        for(Thread thread: threadSet) {
            indexToThread.put(index, thread);
            index++;
        }
        long cnt = 0;
        while(rrParser.checkAndGetNext(handlerEvent)) {
            cnt++;
            if(cnt >= start && cnt < start + interval) {
                if(idToLocationMap.get(handlerEvent.getLocId()).length() > 0) {
                    if(!candidates.containsKey(handlerEvent.getThread())) {
                        candidates.put(handlerEvent.getThread(), new HashSet<>());
                    }
                    candidates.get(handlerEvent.getThread()).add(handlerEvent.getLocId());
                }
            }
        }

        if(candidates.size() < k) {
            return false;
        }
        else {
            Random generator = new Random();
            cnt = 0;
            HashSet<Integer> chosenThreads = new HashSet<>();
            HashSet<Integer> chosenEvents = new HashSet<>();
            int randomIndex = 0;
            while(cnt < k) {
                // System.out.println(randomIndex);
                while(chosenThreads.contains(randomIndex = generator.nextInt(threadSet.size()))) {}
                if(candidates.containsKey(indexToThread.get(randomIndex))) {
                    HashSet<Integer> locs = candidates.get(indexToThread.get(randomIndex));
                    randomIndex = generator.nextInt(locs.size());
                    ArrayList<Integer> locsArray = new ArrayList<>(locs);
                    int locId = locsArray.get(randomIndex); 
                    if(!chosenEvents.contains(locId)) {
                        pattern.add(idToLocationMap.get(locId));
                        cnt++;
                    }
                    
                }
            }
            return true;
        }
    }
    
}
