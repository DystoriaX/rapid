package engine.prefix.race;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    protected String sourceFile;

    HashSet<Long> racyevents = new HashSet<>();

    public boolean partition = false;
    long startTimeAnalysis = 0;

    HashMap<Thread, Long> thread_last_events = new HashMap<>();
    HashMap<Long, Thread> last_events_thread = new HashMap<>();

    public PrefixEngine(ParserType pType, String trace_folder, double prob) {
        super(pType);
        sourceFile = trace_folder;
        eventCount = 0;
        totalSkippedEvents = 0;
        this.initializeReader(trace_folder);
        handlerEvent = new PrefixEvent();
        for(Thread t: threadSet) {
            thread_last_events.put(t, (long)0);
        }
        filterTraceSTD();
        for(Thread t: threadSet) {
            last_events_thread.put(thread_last_events.get(t), t);
        }
        // System.out.println(last_events_thread);
        eventCount = 0;
        resetSTDParser();
        state = new State(threadSet, prob);
    }

    protected void analyzeEvent(PrefixEvent handlerEvent, Long eventCount){
		
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
            stdParser.getNextEvent(handlerEvent);
            try{
                if(handlerEvent.Handle(state)) {
                    racyevents.add(eventCount);
                }
                if(last_events_thread.containsKey(eventCount)) {
                    state.forget(last_events_thread.get(eventCount));
                }
            }
            catch(OutOfMemoryError oome){
                System.err.println("Number of events = " + Long.toString(eventCount));
                state.printMemory();
                oome.printStackTrace();
            }
            // System.out.println(eventCount + " " + state.raceCnt + " " + state.states.size() + " " + state.tSet.size());
            postHandleEvent(handlerEvent);
        }
        stopTimeAnalysis = System.currentTimeMillis();
        long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
        System.out.println("Number of racy events = " + racyevents.size());
        System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
        // state.printMemory();
    }

    private void filterTraceSTD() {
        while(stdParser.hasNext()){
			eventCount = eventCount + 1;
			stdParser.getNextEvent(handlerEvent);
            thread_last_events.put(handlerEvent.getThread(), eventCount);
            if(handlerEvent.getType().isJoin()) {
                thread_last_events.put(handlerEvent.getTarget(), eventCount);
            }
            postHandleEvent(handlerEvent);
        }
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
        rrParser = new ParseRoadRunner(trace_file, true);
        threadSet = rrParser.getThreadSet();
        idToLocationMap = rrParser.idToLocationMap;
        locationToIdMap = rrParser.locationToIdMap;
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

    protected boolean skipEvent(PrefixEvent handlerEvent) {
        // return !handlerEvent.getType().isAccessType();
        return false;
    }

	protected void postHandleEvent(PrefixEvent handlerEvent) {

    } 
}
