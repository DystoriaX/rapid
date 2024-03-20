package engine.pattern.MetaInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import engine.pattern.PatternEngine;
import parse.ParserType;

public class MetaPatternEngine extends PatternEngine<MetaPatternState, MetaPatternEvent> {
    
    ArrayList<ArrayList<Integer>> patterns = new ArrayList<>();
    
    public MetaPatternEngine(ParserType pType, String trace_folder, String patternFile) {
        super(pType, trace_folder, patternFile + "/pattern0");
        handlerEvent = new MetaPatternEvent();
        for(int i = 0; i < 10; i++) {
            try {
                ArrayList<Integer> thispattern = new ArrayList<>();
                Scanner myReader = new Scanner(new File(patternFile + "/pattern" + i));
                while (myReader.hasNextLine()) {
                    String loc = myReader.nextLine();
                    if(this.parserType.isRR()) {
                        thispattern.add(locationToIdMap.get(loc));
                    }
                    if(this.parserType.isSTD()) {
                        thispattern.add(Integer.parseInt(loc));
                    }
                  
                }
                myReader.close();
                patterns.add(thispattern);
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        
        if(this.parserType.isRR()) {
            state = new MetaPatternState(threadSet, patterns, locationToIdMap.size());
        }
        if(this.parserType.isSTD()) {
            state = new MetaPatternState(threadSet, patterns, stdParser.numOfLocations);
        }
        
    }
}
