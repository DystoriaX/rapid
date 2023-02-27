package engine.pattern.PatternGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import event.Event;
import event.Thread;
import parse.rr.ParseRoadRunner;

public abstract class PatternGenerator {
    private int k;
    private String sourceFile;

    protected ParseRoadRunner rrParser; // RR
	protected Event handlerEvent;

    private ArrayList<Integer> pattern;
    private HashSet<Thread> threadSet;
    private HashMap<Integer, String> idToLocationMap;

    public PatternGenerator(int k, String sourceFile) {
        this.k = k;
        this.sourceFile = sourceFile;
        pattern = new ArrayList<>();
        initRRParser();
    }

    protected void initRRParser() {
        rrParser = new ParseRoadRunner(sourceFile, true);
        threadSet = rrParser.getThreadSet();
        idToLocationMap = rrParser.idToLocationMap;
    }

    public abstract void generatePattern();

    public ArrayList<Integer> getPattern() {
        return pattern;
    }
}
