package engine.pattern.OptimizedPatternTrack;

import engine.pattern.PatternEngine;
import parse.ParserType;

public class OptimizedVectorClockEngine extends PatternEngine<OptimizedVectorClockState, OptimizedVectorClockEvent> {
    public OptimizedVectorClockEngine(ParserType pType, String trace_folder, String patternFile) {
        super(pType, trace_folder, patternFile);
        handlerEvent = new OptimizedVectorClockEvent();
        state = new OptimizedVectorClockState(threadSet, patterns);
    }
}
