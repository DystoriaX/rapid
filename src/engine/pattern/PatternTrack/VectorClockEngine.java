package engine.pattern.PatternTrack;

import engine.pattern.PatternEngine;
import parse.ParserType;

public class VectorClockEngine extends PatternEngine<VectorClockState, VectorClockEvent> {

    public VectorClockEngine(ParserType pType, String trace_folder, String patternFile) {
        super(pType, trace_folder, patternFile);
        handlerEvent = new VectorClockEvent();
        state = new VectorClockState(threadSet, patterns.get(0));
    }

    @Override
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
    }
}
