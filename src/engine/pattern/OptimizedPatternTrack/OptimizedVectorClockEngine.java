package engine.pattern.OptimizedPatternTrack;

import parse.ParserType;

public class OptimizedVectorClockEngine
        extends OptimizedPatternEngine<OptimizedVectorClockState, OptimizedVectorClockEvent> {
    public OptimizedVectorClockEngine(ParserType pType, String trace_folder, String patternFile) {
        super(pType, trace_folder, patternFile);
        handlerEvent = new OptimizedVectorClockEvent();
        state = new OptimizedVectorClockState(threadSet, patternsGraph);
    }
}
