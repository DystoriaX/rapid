package engine.pattern.PatternGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import event.Event;
import event.Thread;
import parse.ParserType;
import parse.rr.ParseRoadRunner;
import parse.std.ParseStandard;

public abstract class PatternGenerator {
    protected int k;
    protected String sourceFile;
    protected String patternFile;
    protected int number;

    protected ParserType parserType;
	protected ParseStandard stdParser; // STD
    protected ParseRoadRunner rrParser; // RR
	protected Event handlerEvent = new Event();

    protected ArrayList<ArrayList<String>> patterns;
    protected HashSet<Thread> threadSet;
    protected HashMap<Integer, String> idToLocationMap = null;
    protected long numOfEvents;

    public PatternGenerator(ParserType pType,  String sourceFile, String patternFile, int number) {
        this.sourceFile = sourceFile;
        this.patternFile = patternFile;
        this.number = number;
        patterns = new ArrayList<>();
        this.parserType = pType;
        initParser();
        
    }

    protected void initParser() {
        if(this.parserType.isRR()) {
            initRRParser();
        }
        if(this.parserType.isSTD()) {
            initSTDParser();
        }
    }

    protected void initRRParser() {
        rrParser = new ParseRoadRunner(sourceFile, true);
        numOfEvents = rrParser.tot;
        threadSet = rrParser.getThreadSet();
        idToLocationMap = rrParser.idToLocationMap;
    }

    protected void initSTDParser() {
        stdParser = new ParseStandard(sourceFile, true);
		threadSet = stdParser.getThreadSet();
        numOfEvents = stdParser.tot;
    }

    protected void resetParser() {
        if(this.parserType.isRR()) {
            resetRRParser();
        }
        if(this.parserType.isSTD()) {
            resetSTDParser();
        }
    }

    protected void resetSTDParser() {
        stdParser.totEvents = 0;
        try{
            stdParser.bufferedReader = new BufferedReader(new FileReader(sourceFile));
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + sourceFile + "'");
        }
    }

    protected void resetRRParser() {
        rrParser.totEvents = 0;
        try{
            rrParser.bufferedReader = new BufferedReader(new FileReader(sourceFile));
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + sourceFile + "'");
        }
    }

    public void generatePatterns() {
        for(int i = 0; i < number; i++) {
            System.out.println("start " + i);
            this.k = i >= (int)(number / 2) ? 3 : 5;
            resetParser();
            ArrayList<String> pattern = new ArrayList<>();
            while(!this.generatePattern(pattern)) {
                resetRRParser();
            }
            patterns.add(pattern);
            writeToFile(i);
        }
    }

    protected String idToLocation(int locId) {
        if(idToLocationMap != null) {
            return idToLocationMap.get(locId);
        }
        else {
            return String.valueOf(locId);
        }
    }

    protected abstract boolean generatePattern(ArrayList<String> pattern);

    private void writeToFile(int i) {
        try {
            File myObj = new File(patternFile + i);
            myObj.createNewFile();
            FileWriter myWriter = new FileWriter(patternFile + i);
            for(String loc: patterns.get(i)) {
                myWriter.write(loc + "\n");
            }
            myWriter.close();
            System.out.println("Finish Writing");
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
    }

}
