package engine.pattern.PatternGenerator;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import event.Event;
import event.Thread;
import parse.ParserType;

public class LocalPatternGenerator extends PatternGenerator {


    public LocalPatternGenerator(ParserType pType, String sourceFile, String patternFile, int number) {
        super(pType, sourceFile, patternFile, number);
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
    public boolean generatePattern(ArrayList<String> pattern, HashMap<Thread, HashSet<Integer>> candidates) {
        int left = k;
        int cnt;
        Random generator = new Random();
        while(left > 0) {
            int thrNum = (int)Math.min(left, candidates.size());
            if(thrNum == 0) {
                System.out.println("No enough locations");
                return false;
            }
            cnt = 0;
            int randomIndex = 0;
            while(cnt < thrNum) {
                ArrayList<Thread> setIndex = new ArrayList<>(candidates.keySet());
                Collections.shuffle(setIndex);
                for(Thread chosenThread: setIndex) {
                    HashSet<Integer> locs = candidates.get(chosenThread);
                    randomIndex = generator.nextInt(locs.size());
                    ArrayList<Integer> locsArray = new ArrayList<>(locs);
                    int locId = locsArray.get(randomIndex); 
                    pattern.add(idToLocation(locId));
                    cnt++;
                    if(cnt == thrNum) {
                        break;
                    }
                }
            }
            left = left - thrNum;
        }
        return true;
    }
    
}
