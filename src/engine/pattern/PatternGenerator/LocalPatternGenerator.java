package engine.pattern.PatternGenerator;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import event.Event;
import event.Thread;
import parse.ParserType;

public class LocalPatternGenerator extends PatternGenerator {

    private long interval;

    public LocalPatternGenerator(ParserType pType, String sourceFile, String patternFile, int number) {
        super(pType, sourceFile, patternFile, number);
        this.interval = (numOfEvents < 1000000000) ? numOfEvents : (numOfEvents / 100);
    }

    boolean checkAndGetNext(Event handlerEvent) {
        if(this.parserType.isRR()) {
            return rrParser.checkAndGetNext(handlerEvent);
        }
        if(this.parserType.isSTD()) {
            if(stdParser.hasNext()) {
                stdParser.getNextEvent(handlerEvent);
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean generatePattern(ArrayList<String> pattern) {
        long start = (numOfEvents == interval) ? 0 : (long)((Math.random() + 0.5) / 2 * (numOfEvents - interval));
        System.out.println(numOfEvents + " " + interval + " " + start);

        HashMap<Thread, HashSet<Integer>> candidates = new HashMap<>();
        HashMap<Integer, Thread> indexToThread = new HashMap<>();
        HashMap<Thread, Integer> threadToIndex = new HashMap<>();
        int index = 0;
        for(Thread thread: threadSet) {
            threadToIndex.put(thread, index);
            indexToThread.put(index, thread);
            index++;
        }
        long cnt = 0;
        while(checkAndGetNext(handlerEvent)) {
            cnt++;
            if(cnt >= start && cnt < start + interval) {
                String loc = idToLocation(handlerEvent.getLocId());
                if(loc.length() > 0 && !loc.equals("null")) {
                    if(!candidates.containsKey(handlerEvent.getThread())) {
                        candidates.put(handlerEvent.getThread(), new HashSet<>());
                    }
                    candidates.get(handlerEvent.getThread()).add(handlerEvent.getLocId());
                }
            }
            if(cnt >= start + interval) {
                break;
            }
        }

        if(candidates.size() == 1) {
            System.out.println("single threaded");
            return false;
        }

        int left = k;
        while(left > 0) {
            int thrNum = (int)Math.min(left, candidates.size());
            if(thrNum == 0) {
                System.out.println("No enough locations");
                return false;
            }
            Random generator = new Random();
            cnt = 0;
            HashSet<Integer> chosenThreads = new HashSet<>();
            HashSet<Integer> chosenEvents = new HashSet<>();
            int randomIndex = 0;
            while(cnt < thrNum) {
                // System.out.println(randomIndex);
                ArrayList<Thread> setIndex = new ArrayList<>(candidates.keySet());
                while(chosenThreads.contains(randomIndex = generator.nextInt(candidates.keySet().size()))) {}
                Thread chosenThread = setIndex.get(randomIndex);
                chosenThreads.add(threadToIndex.get(chosenThread));
                if(candidates.containsKey(chosenThread)) {
                    HashSet<Integer> locs = candidates.get(chosenThread);
                    randomIndex = generator.nextInt(locs.size());
                    ArrayList<Integer> locsArray = new ArrayList<>(locs);
                    int locId = locsArray.get(randomIndex); 
                    if(!chosenEvents.contains(locId)) {
                        pattern.add(idToLocation(locId));
                        chosenEvents.add(locId);
                        cnt++;
                        for(Thread key: candidates.keySet()) {
                            if(candidates.get(key).contains(locId)) {
                                candidates.get(key).remove(locId);
                            }
                        }
                        candidates.entrySet().removeIf(e -> e.getValue().size() == 0);
                    }
                }
            }
            left = left - thrNum;
        }
        return true;
    }
    
}
