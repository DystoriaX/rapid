package engine.atomicity.conflictserializability.summarizedGraph;

import engine.atomicity.AtomicityEvent;
import event.Lock;
import event.Thread;
import event.Variable;

public class SummarizedGraphEvent extends AtomicityEvent<SummarizedGraphState> {
    
    @Override
	public void printRaceInfoLockType(SummarizedGraphState state) {
		if(this.getType().isLockType()){
			if(state.verbosity == 2){
				String str = "#";
				str += Integer.toString(getLocId());
				str += "|";
				str += this.getType().toString();
				str += "|";
				str += this.getLock().toString();
				str += "|";
				str += this.getThread().getName();
				System.out.println(str);
			}
		}		
	}

	@Override
	public void printRaceInfoTransactionType(SummarizedGraphState state) {
		if(this.getType().isLockType()){
			if(state.verbosity == 2){
				String str = "#";
				str += Integer.toString(getLocId());
				str += "|";
				str += this.getType().toString();
				str += "|";
				str += this.getLock().toString();
				str += "|";
				str += this.getThread().getName();
				System.out.println(str);
			}
		}
	}

	@Override
	public void printRaceInfoAccessType(SummarizedGraphState state) {
		if(this.getType().isAccessType()){
			if(state.verbosity == 1 || state.verbosity == 2){
				String str = "#";
				str += Integer.toString(getLocId());
				str += "|";
				str += this.getType().toString();
				str += "|";
				str += this.getVariable().getName();
				str += "|";
				str += this.getThread().getName();
				str += "|";
				str += this.getAuxId();
				System.out.println(str);
			}	
		}		
	}

	@Override
	public void printRaceInfoExtremeType(SummarizedGraphState state) {
		if(this.getType().isExtremeType()){
			if(state.verbosity == 2){
				String str = "#";
				str += Integer.toString(getLocId());
				str += "|";
				str += this.getType().toString();
				str += "|";
				str += this.getTarget().toString();
				str += "|";
				str += this.getThread().getName();
				System.out.println(str);
			}
		}		
	}

	@Override
	public boolean HandleSubAcquire(SummarizedGraphState state) {
		Thread t = this.getThread();
		Lock l = this.getLock();
		state.checkAndAddLock(l);

		if(state.isThreadNoCurrentTransaction(t)) {
			for(Thread tprime: state.threadToIndex.keySet()) {
                if(state.threadToLockOfThisTr.get(tprime).contains(l) || state.threadToLockAftThisTr.get(tprime).contains(l) || state.threadToThreadAftThisTr.get(tprime).contains(t)) {
                    state.threadToLockAftThisTr.get(tprime).add(l);
                    state.threadToThreadAftThisTr.get(tprime).add(t);
                }
            }
		}
		else {
            state.threadToLockOfThisTr.get(t).add(l);
            Integer id = state.threadToIndex.get(t);
            for(Thread tprime: state.threadToIndex.keySet()) {
                Integer idprime = state.threadToIndex.get(tprime);
                if(id != idprime && state.threadToLockOfThisTr.get(tprime).contains(l)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                else if(state.threadToLockAftThisTr.get(tprime).contains(l)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                // if(state.cycleDetector_thb_graph.detectCycles()) {
				// 	return true;
				// }
            }
		}
		return false;
	}

	@Override
	public boolean HandleSubRelease(SummarizedGraphState state) {
		return false;
	}

	@Override
	public boolean HandleSubRead(SummarizedGraphState state) {
		Thread t = this.getThread();
		Variable v = this.getVariable();
		state.checkAndAddVariable(v);

        if(state.isThreadNoCurrentTransaction(t)){
            for(Thread tprime: state.threadToIndex.keySet()) {
                if(state.threadToWriteOfThisTr.get(tprime).contains(v) || state.threadToWriteAftThisTr.get(tprime).contains(v) || state.threadToThreadAftThisTr.get(tprime).contains(t)) {
                    state.threadToReadAftThisTr.get(tprime).add(v);
                    state.threadToThreadAftThisTr.get(tprime).add(t);
                }
            }
		}
		else {
            state.threadToReadOfThisTr.get(t).add(v);
            Integer id = state.threadToIndex.get(t);
            for(Thread tprime: state.threadToIndex.keySet()) {
                Integer idprime = state.threadToIndex.get(tprime);
                if(id != idprime && state.threadToWriteOfThisTr.get(tprime).contains(v)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                else if(state.threadToWriteAftThisTr.get(tprime).contains(v)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                // should not do this if every txn ends
                // if(state.cycleDetector_thb_graph.detectCycles()) {
				// 	return true;
				// }
            }
		}
		return false;
	}

	@Override
	public boolean HandleSubWrite(SummarizedGraphState state) {

		boolean violationDetected = false;

		Thread t = this.getThread();
		Variable v = this.getVariable();
		state.checkAndAddVariable(v);

		if(state.isThreadNoCurrentTransaction(t)){
            for(Thread tprime: state.threadToIndex.keySet()) {
                if(state.threadToReadOfThisTr.get(tprime).contains(v) || state.threadToReadAftThisTr.get(tprime).contains(v) || state.threadToWriteOfThisTr.get(tprime).contains(v) || state.threadToWriteAftThisTr.get(tprime).contains(v) || state.threadToThreadAftThisTr.get(tprime).contains(t)) {
                    state.threadToWriteAftThisTr.get(tprime).add(v);
                    state.threadToThreadAftThisTr.get(tprime).add(t);
                }
            }
		}
		else {
            state.threadToWriteOfThisTr.get(t).add(v);
            Integer id = state.threadToIndex.get(t);
            for(Thread tprime: state.threadToIndex.keySet()) {
                Integer idprime = state.threadToIndex.get(tprime);
                if(id != idprime && (state.threadToReadOfThisTr.get(tprime).contains(v) || state.threadToWriteOfThisTr.get(tprime).contains(v))) {
                    state.thb_graph.addEdge(idprime, id);
                }
                else if(state.threadToReadAftThisTr.get(tprime).contains(v) || state.threadToWriteAftThisTr.get(tprime).contains(v)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                // if(state.cycleDetector_thb_graph.detectCycles()) {
				// 	return true;
				// }
            }
		}
		return violationDetected;
	}

	@Override
	public boolean HandleSubFork(SummarizedGraphState state) {
		Thread t = this.getThread();
		Thread child = this.getTarget();
		
		if(!state.isThreadNoCurrentTransaction(t)) {
            state.threadToThreadAftThisTr.get(t).add(child);
        }
		return false;
	}

	@Override
	public boolean HandleSubJoin(SummarizedGraphState state) {
		Thread t = this.getThread();
		Thread child_t = this.getTarget();
		if(!state.isThreadNoCurrentTransaction(t)){
            Integer id = state.threadToIndex.get(t);
			for(Thread tprime: state.threadToIndex.keySet()) {
                Integer idprime = state.threadToIndex.get(tprime);
                if(state.threadToThreadAftThisTr.get(tprime).contains(child_t)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                // if(state.cycleDetector_thb_graph.detectCycles()) {
				// 	return true;
				// }
            }
        }
		return false;
	}

	@Override
	public boolean HandleSubBegin(SummarizedGraphState state) {
		Thread t = this.getThread();

        int cur_depth = state.threadToNestingDepth.get(t);
		state.threadToNestingDepth.put(t, cur_depth + 1);

		if(cur_depth == 0){
			// This is the case when cur_depth = 0;
            Integer id = state.threadToIndex.get(t);
            state.thb_graph.addVertex(id);
            for(Thread tprime: state.threadToIndex.keySet()) {
                Integer idprime = state.threadToIndex.get(tprime);
                if(state.threadToThreadAftThisTr.get(tprime).contains(t)) {
                    state.thb_graph.addEdge(idprime, id);
                }
                // if(state.cycleDetector_thb_graph.detectCycles()) {
				// 	return true;
				// }
            }
		}
		else{
			// Treat this as a no-op
		}
		return false;
	}

	@Override
	public boolean HandleSubEnd(SummarizedGraphState state) {
		Thread t = this.getThread();

		int cur_depth = state.threadToNestingDepth.get(t);
		state.threadToNestingDepth.put(t, cur_depth - 1);
		if(cur_depth == 1) {
            if(state.updateGraphAndCheckLoop(t)){
                return true;
            }
		}
		else {
			// Treat this as no-op
		}
		return false;
	}
}
