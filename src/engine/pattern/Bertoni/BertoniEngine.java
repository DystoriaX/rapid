package engine.pattern.Bertoni;

import java.util.ArrayList;

import engine.pattern.PatternEngine;
import parse.ParserType;

public class BertoniEngine extends PatternEngine<BertoniState, BertoniEvent> {
    ArrayList<String> pattern = new ArrayList<>();

    public BertoniEngine(ParserType pType, String trace_folder) {
        super(pType, trace_folder);
        handlerEvent = new BertoniEvent();
        this.initializeReader("benchmark/exp4j/pattern.rr");
        generatePattern();
        this.initializeReader(trace_folder);
        state = new BertoniState(threadSet, pattern);
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
