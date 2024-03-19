package engine.pattern.OptimizedPatternTrack;

import java.util.HashMap;
import java.util.HashSet;
import engine.Engine;
import engine.pattern.State;
import engine.pattern.OptimizedPatternTrack.parser.DAGParser;
import event.Thread;
import parse.ParserType;
import parse.rr.ParseRoadRunner;
import parse.std.ParseStandard;
import util.DAG;

public class OptimizedPatternEngine<S extends State, E extends OptimizedPatternEvent<S>> extends Engine<E> {
    protected long eventCount;
    protected long totalSkippedEvents;

    protected HashSet<Thread> threadSet;
    protected HashMap<Integer, String> idToLocationMap = null;
    protected HashMap<String, Integer> locationToIdMap = null;
    protected S state;

    protected DAG<Integer> patternsGraph;

    public boolean partition = false;
    protected long startTimeAnalysis = 0;
    protected long start;
    protected long end;

    public OptimizedPatternEngine(ParserType pType, String trace_folder, String patternFileName) {
        super(pType);

        this.initializeReader(trace_folder);

        DAGParser parser = new DAGParser(patternFileName);
        patternsGraph = parser.parse().map((loc) -> {
            if (pType.isRR()) {
                return locationToIdMap.get(loc);
            } else {
                return Integer.valueOf(loc);
            }
        });

        eventCount = 0;
        totalSkippedEvents = 0;
    }

    protected boolean analyzeEvent(E handlerEvent, Long eventCount) {
        boolean patternMatched = false;

        try {
            patternMatched = handlerEvent.Handle(state);
        } catch (OutOfMemoryError oome) {
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
        if (this.parserType.isSTD()) {
            analyzeTraceSTD();
        }
        printCompletionStatus();
        // postAnalysis();
    }

    protected void analyzeTraceRR() {
        boolean flag = false;
        startTimeAnalysis = System.currentTimeMillis();
        long stopTimeAnalysis = 0;
        while (rrParser.checkAndGetNext(handlerEvent)) {
            eventCount = eventCount + 1;
            boolean matched = analyzeEvent(handlerEvent, eventCount);
            if (matched) {
                stopTimeAnalysis = System.currentTimeMillis();
                flag = true;
                System.out.println("Pattern Matched on the first " + eventCount + " events");
                break;
            }
            postHandleEvent(handlerEvent);
        }
        if (!flag) {
            stopTimeAnalysis = System.currentTimeMillis();
            System.out.println("Not matched");
        }
        long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
        System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
        state.printMemory();
    }

    private void analyzeTraceSTD() {
        boolean flag = false;
        while (stdParser.hasNext()) {
            eventCount = eventCount + 1;
            stdParser.getNextEvent(handlerEvent);
            boolean matched = analyzeEvent(handlerEvent, eventCount);
            if (matched) {
                flag = true;
                System.out.println("Pattern Matched on the first " + eventCount + " events");
                break;
            }
            postHandleEvent(handlerEvent);
        }
        if (!flag) {
            System.out.println("Not matched");
        }
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

    protected boolean skipEvent(E handlerEvent) {
        // return !handlerEvent.getType().isAccessType();
        return false;
    }

    protected void postHandleEvent(E handlerEvent) {

    }

}
