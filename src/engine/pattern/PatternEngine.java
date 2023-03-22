package engine.pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import engine.Engine;
import event.Thread;
import parse.ParserType;
import parse.rr.ParseRoadRunner;

public class PatternEngine<S extends State, E extends PatternEvent<S>> extends Engine<E> {
    protected long eventCount;
    protected long totalSkippedEvents;
    
    protected HashSet<Thread> threadSet;
    protected HashMap<Integer, String> idToLocationMap;
    protected HashMap<String, Integer> locationToIdMap;
    protected S state;

    protected ArrayList<Integer> pattern = new ArrayList<>();

    public PatternEngine(ParserType pType, String trace_folder, String patternFileName) {
        super(pType);
        this.initializeReader(trace_folder);
        try {
            Scanner myReader = new Scanner(new File(patternFileName));
            while (myReader.hasNextLine()) {
              String loc = myReader.nextLine();
              pattern.add(locationToIdMap.get(loc));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        eventCount = 0;
        totalSkippedEvents = 0;
    }

    protected boolean analyzeEvent(E handlerEvent, Long eventCount){
		boolean patternMatched = false;
		try{
			patternMatched = handlerEvent.Handle(state);
		}
		catch(OutOfMemoryError oome){
			System.err.println("Number of events = " + Long.toString(eventCount));
			state.printMemory();
            oome.printStackTrace();
		}
		return patternMatched;
	}

    public void analyzeTrace() {
		if (this.parserType.isRR()) {
			analyzeTraceRR();
		}
		printCompletionStatus();
		// postAnalysis();
    }

    private void analyzeTraceRR() {
        boolean flag = false;
        while (rrParser.checkAndGetNext(handlerEvent)) {
			eventCount = eventCount + 1;
            // System.out.println(eventCount);
            // state.printMemory();
			boolean matched = analyzeEvent(handlerEvent, eventCount);
            if (matched) {
                flag = true;
                System.out.println("Pattern Matched on the first " + eventCount + " events");
                break;
            }
            postHandleEvent(handlerEvent);
		}
        if(!flag) {
            System.out.println("Not matched");
        }
        state.printMemory();
    }

    protected void initializeReaderRV(String trace_folder) {

    }

	protected void initializeReaderCSV(String trace_file) {

    }

	protected void initializeReaderSTD(String trace_file) {

    }

	protected void initializeReaderRR(String trace_file) {
        rrParser = new ParseRoadRunner(trace_file, true);
        threadSet = rrParser.getThreadSet();
        idToLocationMap = rrParser.idToLocationMap;
        locationToIdMap = rrParser.locationToIdMap;
    }

    protected boolean skipEvent(E handlerEvent) {
        // return !handlerEvent.getType().isAccessType();
        return false;
    }

	protected void postHandleEvent(E handlerEvent) {

    }

}
