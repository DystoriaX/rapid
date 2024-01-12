package engine.prefix.race;

import java.util.ArrayList;
import java.util.Iterator;

import engine.pattern.Vectorclock.VectorClockEvent;
import util.PipedDeepCopy;

public class PrefixEvent extends VectorClockEvent {

    public boolean Handle(State state) {
        ArrayList<DependentInfo> newStates = new ArrayList<>();
        boolean matched = false;
        
        state.timestamp++;

        boolean threadLocal = this.getType().isTransactionType() || (this.getType().isAccessType() && this.variable.touchedThreads.size() == 1);
        if(threadLocal) {
            return false;
        }
        // System.out.println(state.states.size());
        for(Iterator<DependentInfo> iterator = state.states.iterator(); iterator.hasNext();){
            DependentInfo dep = iterator.next(); 
            
            if(dep.birth > 0 && state.timestamp - dep.birth >= state.prob) {
                iterator.remove();
                continue;
            }
            
            if(mustIgnore(dep)) {
                if(this.getType().isRead() && !dep.check_dependency(this.thread)) {
                    if(dep.addReadCandidates(this.getThread(), this.getVariable())) {
                        state.racy = true;
                    }
                }
                ignore(dep, state);
                if(dep.allThreads(state.tSet.size())) {
                    iterator.remove();
                }
            }
            else {
                if(this.getType().isAccessType() || !dep.wr_candidates.isEmpty() || !dep.rd_candidates.isEmpty() ) {
                    DependentInfo dep_new = (DependentInfo) PipedDeepCopy.copy(dep);
                    if(this.getType().isAccessType()) {
                        if(this.getType().isRead()) {
                            if(dep_new.addReadCandidates(this.getThread(), this.getVariable())) {
                                state.racy = true;
                            }
                        }
                        else {
                            if(dep_new.addWriteCandidates(this.getThread(), this.getVariable())) {
                                state.racy = true;
                            }
                        }
                    }
                    ignore(dep_new, state);
                    if(!dep_new.allThreads(state.tSet.size())) {
                        dep_new.birth = state.timestamp;
                        newStates.add(dep_new);
                    }
                }

                if(this.getType().isWrite()) {
                    dep.remove(this.variable);
                }
                if(this.getType().isAcquire()) {
                    dep.add(this.lock);
                }
                if(this.getType().isRelease()) {
                    dep.remove(this.lock);
                }
            }
        }

        state.states.addAll(newStates);
        if(state.racy) {
            state.raceCnt++;
        }
        state.racy = false;
        state.printMemory();
		return matched;
	}

    boolean mustIgnore(DependentInfo dep){

        if(this.getType().isAcquire()) {
            return dep.check_dependency(this.thread) || dep.check_dependency(this.lock);
        }

        if (this.getType().isRead()) {
            return dep.check_dependency(this.thread) || dep.check_dependency(this.variable);
        }

        if(this.getType().isJoin()) {
            return dep.check_dependency(this.thread) || dep.check_dependency(this.target);
        }

		return dep.check_dependency(this.thread);
	}

    void ignore(DependentInfo dep, State state) {
        dep.add(this.thread);

        if(this.getType().isWrite()) {
            dep.add(this.variable);
        }

        if(this.getType().isFork()) {
            dep.add(this.target);
        }
    }
}
