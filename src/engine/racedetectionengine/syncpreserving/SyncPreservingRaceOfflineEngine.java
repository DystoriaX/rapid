package engine.racedetectionengine.syncpreserving;

import java.util.HashMap;
import java.util.HashSet;

import engine.accesstimes.orderedvars.OrderedVarsEngine;
import engine.racedetectionengine.RaceDetectionEngine;
import event.Thread;
import parse.ParserType;


public class SyncPreservingRaceOfflineEngine extends RaceDetectionEngine<SyncPreservingRaceState, SyncPreservingRaceEvent> {

	private HashMap<String, HashSet<String>> lockToThreadSet;
	private HashSet<String> orderedVariables;
	
	public SyncPreservingRaceOfflineEngine(ParserType pType, String trace_folder) {
		super(pType);
		this.threadSet = new HashSet<Thread> ();
		initializeReader(trace_folder);
		this.state = new SyncPreservingRaceState(this.threadSet);
		handlerEvent = new SyncPreservingRaceEvent();

		boolean time_reporting = true;
		long startTimeAnalysis = 0;
		if(time_reporting){
			startTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
		}	
		OrderedVarsEngine orderedVarsEngine = new OrderedVarsEngine(pType, trace_folder, 0);
		orderedVarsEngine.analyzeTrace(false, 0);
		
		if(time_reporting){
			long stopTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
			long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
			System.out.println("Time for Phase-1 = " + timeAnalysis + " milliseconds");	
		}
		
		this.lockToThreadSet = orderedVarsEngine.getLockToThreadSet();
		this.state.stringVariableToThreadSet = orderedVarsEngine.getVariableToThreadSet();
		this.orderedVariables = orderedVarsEngine.getOrdredVars();
	}

	@Override
	protected boolean skipEvent(SyncPreservingRaceEvent handlerEvent){
		if(handlerEvent.getType().isAccessType()){
			String var_name = handlerEvent.getVariable().getName();
			if(this.orderedVariables.contains(var_name)) {
				return true;
			}
		}
		else if(handlerEvent.getType().isLockType()){
			if(lockToThreadSet.get(handlerEvent.getLock().getName()).size() <= 1 ){
				return true;
			}
		}
		return false;
	}

	@Override
	protected void postHandleEvent(SyncPreservingRaceEvent handlerEvent) {	
	}

}
