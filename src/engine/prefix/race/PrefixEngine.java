package engine.prefix.race;

import java.util.HashMap;
import java.util.HashSet;

import engine.Engine;
import event.Thread;
import parse.ParserType;
import parse.rr.ParseRoadRunner;
import parse.std.ParseStandard;

public class PrefixEngine extends Engine<PrefixEvent> {
    protected long eventCount;
    protected long totalSkippedEvents;
    
    protected HashSet<Thread> threadSet;
    protected HashMap<Integer, String> idToLocationMap = null;
    protected HashMap<String, Integer> locationToIdMap = null;
    protected State state;

    public boolean partition = false;
    long startTimeAnalysis = 0;
    long start;
    long end;

    public PrefixEngine(ParserType pType, String trace_folder, double prob) {
        super(pType);
        eventCount = 0;
        totalSkippedEvents = 0;
        this.initializeReader(trace_folder);
        handlerEvent = new PrefixEvent();
        state = new State(threadSet, prob);
    }

    protected void analyzeEvent(PrefixEvent handlerEvent, Long eventCount){
		try{
			handlerEvent.Handle(state);
		}
		catch(OutOfMemoryError oome){
			System.err.println("Number of events = " + Long.toString(eventCount));
			state.printMemory();
            oome.printStackTrace();
		}
	}

    public void analyzeTrace() {
		if (this.parserType.isRR()) {
			analyzeTraceRR();
		}
        if (this.parserType.isSTD()) {
			analyzeTraceSTD();
		}
		printCompletionStatus();
		// postAnalysis();
    }

    private void analyzeTraceRR() {
        boolean flag = false;
        startTimeAnalysis = System.currentTimeMillis();
        long stopTimeAnalysis = 0;
        while(rrParser.checkAndGetNext(handlerEvent)) {
            eventCount = eventCount + 1;
            analyzeEvent(handlerEvent, eventCount);
            postHandleEvent(handlerEvent);
        }
        if(!flag) {
            stopTimeAnalysis = System.currentTimeMillis();
            System.out.println("Not matched");
        }
        long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
        System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
    }

    private void analyzeTraceSTD() {
        startTimeAnalysis = System.currentTimeMillis();
        long stopTimeAnalysis = 0;
        while(stdParser.hasNext()){
			eventCount = eventCount + 1;
            System.out.println(eventCount);
			stdParser.getNextEvent(handlerEvent);
            analyzeEvent(handlerEvent, eventCount);
            postHandleEvent(handlerEvent);
        }
        stopTimeAnalysis = System.currentTimeMillis();
        long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
        System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
        state.printMemory();
    }

    protected void initializeReaderRV(String trace_folder) {

    }

	protected void initializeReaderCSV(String trace_file) {

    }

	protected void initializeReaderSTD(String trace_file) {
        stdParser = new ParseStandard(trace_file, true);
		threadSet = stdParser.getThreadSet();
    }

	protected void initializeReaderRR(String trace_file) {
        rrParser = new ParseRoadRunner(trace_file, true, start, end);
        threadSet = rrParser.getThreadSet();
        idToLocationMap = rrParser.idToLocationMap;
        locationToIdMap = rrParser.locationToIdMap;
    }

    protected boolean skipEvent(PrefixEvent handlerEvent) {
        // return !handlerEvent.getType().isAccessType();
        return false;
    }

	protected void postHandleEvent(PrefixEvent handlerEvent) {

    } 
}
