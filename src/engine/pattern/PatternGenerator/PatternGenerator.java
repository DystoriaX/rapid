package engine.pattern.PatternGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import event.Event;
import event.Thread;
import parse.rr.ParseRoadRunner;

public abstract class PatternGenerator {
    protected int k;
    protected String sourceFile;
    protected String patternFile;
    protected int number;

    protected ParseRoadRunner rrParser; // RR
	protected Event handlerEvent = new Event();

    protected ArrayList<ArrayList<String>> patterns;
    protected HashSet<Thread> threadSet;
    protected HashMap<Integer, String> idToLocationMap;
    protected long numOfEvents;

    public PatternGenerator(int k, String sourceFile, String patternFile, int number) {
        this.k = k;
        this.sourceFile = sourceFile;
        this.patternFile = patternFile;
        this.number = number;
        patterns = new ArrayList<>();
        initRRParser();
        numOfEvents = traceLength();
    }

    protected void initRRParser() {
        rrParser = new ParseRoadRunner(sourceFile, true);
        threadSet = rrParser.getThreadSet();
        idToLocationMap = rrParser.idToLocationMap;
    }

    private long traceLength() {
        long cnt = 0;
        while(rrParser.checkAndGetNext(handlerEvent)) {
            cnt++;
        }
        initRRParser();
        return cnt;
    }

    public void generatePatterns() {
        for(int i = 0; i < number; i++) {
            initRRParser();
            ArrayList<String> pattern = new ArrayList<>();
            while(!this.generatePattern(pattern)) {
                initRRParser();
            }
            patterns.add(pattern);
            writeToFile(i);
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
