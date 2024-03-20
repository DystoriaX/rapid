package engine.atomicity.conflictserializability.summarizedGraph;

import java.util.HashSet;

import engine.atomicity.AtomicityEngine;
import event.Thread;
import parse.ParserType;

public class SummarizedGraphEngine extends AtomicityEngine<SummarizedGraphState, SummarizedGraphEvent>{
    
    public SummarizedGraphEngine(ParserType pType, String trace_folder, int verbosity) {
		super(pType);
		this.threadSet = new HashSet<Thread> ();
		initializeReader(trace_folder);
		this.state = new SummarizedGraphState(this.threadSet, verbosity);
		handlerEvent = new SummarizedGraphEvent();
	}

	@Override
	protected boolean skipEvent(SummarizedGraphEvent handlerEvent) {
		return false;
	}

	@Override
	protected void postHandleEvent(SummarizedGraphEvent handlerEvent) {	
	}

	public int numTransactionsActive() {
		return this.state.numTransactionsActive();
	}
}
