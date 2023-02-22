package engine.pattern.vectorclock;

import java.util.ArrayList;

import engine.pattern.PatternEngine;
import parse.ParserType;

public class VectorClockEngine extends PatternEngine<VectorClockState, VectorClockEvent> {

    ArrayList<String> pattern = new ArrayList<>();

    public VectorClockEngine(ParserType pType, String trace_folder) {
        super(pType, trace_folder);
        handlerEvent = new VectorClockEvent();
        this.initializeReader("benchmark/exp4j/patternshort.rr");
        generatePattern();
        this.initializeReader(trace_folder);
        state = new VectorClockState(threadSet, pattern);
    }

    public void generatePattern() {
        // int cnt = 0;
        while (rrParser.checkAndGetNext(handlerEvent)) {
			// if(handlerEvent.getType().isBegin() && Math.random() < 0.0002) {
                pattern.add(handlerEvent.toHashString());
                // if((++cnt) >= 6) {
                //     break;
                // }
            // }
		}
        // Collections.shuffle(pattern);
        System.out.println(pattern);
    }

   
    
}
