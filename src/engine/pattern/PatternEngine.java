package engine.pattern;

import java.util.Collections;

import engine.Engine;
import parse.ParserType;
import parse.rr.ParseRoadRunner;

public class PatternEngine extends Engine<PatternEvent> {

    protected long eventCount;
    protected long totalSkippedEvents;

    protected PatternState state;

    public PatternEngine(ParserType pType, String trace_folder) {
        super(pType);
        handlerEvent = new PatternEvent();
        state = new PatternState();
        this.initializeReader("benchmark/exp4j/pattern.rr");
        generatePattern();
        this.initializeReader(trace_folder);
    }

    public void generatePattern() {
        // int cnt = 0;
        while (rrParser.checkAndGetNext(handlerEvent)) {
			// if(handlerEvent.getType().isBegin() && Math.random() < 0.0002) {
                state.pattern.add(handlerEvent.toHashString());
            //     if((++cnt) >= 6) {
            //         break;
            //     }
            // }
		}
        // Collections.shuffle(state.pattern);
        System.out.println("patterns:");
        for(String event: state.pattern) {
            System.out.println(event);
        }
        System.out.println("patterns end");
        
    }

    protected boolean analyzeEvent(PatternEvent handlerEvent, Long eventCount){
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
                if(eventCount % 1000 == 0) {
                    System.out.println("After analyzing " + eventCount + " events");
                }
				if (matched) {
                    System.out.println("Pattern Matched on the first " + eventCount + " events");
                    break;
				}
				postHandleEvent(handlerEvent);
			}
		}
    }

    protected void initializeReaderRV(String trace_folder) {

    }

	protected void initializeReaderCSV(String trace_file) {

    }

	protected void initializeReaderSTD(String trace_file) {

    }

	protected void initializeReaderRR(String trace_file) {
        rrParser = new ParseRoadRunner(trace_file, true);
    }

    protected boolean skipEvent(PatternEvent handlerEvent) {
        // return !handlerEvent.getType().isAccessType();
        return false;
    }

	protected void postHandleEvent(PatternEvent handlerEvent) {

    }
}
