package engine.pattern;

import java.util.HashMap;
import java.util.HashSet;

import engine.Engine;
import event.Thread;
import parse.ParserType;
import parse.rr.ParseRoadRunner;

public class PatternEngine<S extends State, E extends PatternEvent<S>> extends Engine<E> {
    protected long eventCount;
    protected long totalSkippedEvents;
    
    protected HashSet<Thread> threadSet;
    protected HashMap<Integer, String> idToLocationMap;
    protected S state;

    public PatternEngine(ParserType pType, String trace_folder) {
        super(pType);
    }

    protected boolean analyzeEvent(E handlerEvent, Long eventCount){
		boolean patternMatched = false;
		try{
			patternMatched = handlerEvent.Handle(state);
		}
		catch(OutOfMemoryError oome){
			oome.printStackTrace();
			System.err.println("Number of events = " + Long.toString(eventCount));
			state.printMemory();
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
        while (rrParser.checkAndGetNext(handlerEvent)) {
			eventCount = eventCount + 1;
			if (skipEvent(handlerEvent)) {
				totalSkippedEvents = totalSkippedEvents + 1;
			} else {
				boolean matched = analyzeEvent(handlerEvent, eventCount);
                if(eventCount % 10 == 0) {
                    System.out.println("After analyzing " + eventCount + " events");
                    state.printMemory();
                }
				if (matched) {
                    System.out.println("Pattern Matched on the first " + eventCount + " events");
                    break;
				}
				postHandleEvent(handlerEvent);
			}
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
    }

    protected boolean skipEvent(E handlerEvent) {
        // return !handlerEvent.getType().isAccessType();
        return false;
    }

	protected void postHandleEvent(E handlerEvent) {

    }

}
