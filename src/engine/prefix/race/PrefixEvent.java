package engine.prefix.race;

import java.util.ArrayList;
import java.util.Iterator;

import engine.pattern.PatternTrack.VectorClockEvent;
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
        for(Iterator<DependentInfo> iterator = state.states.iterator(); iterator.hasNext();){
            DependentInfo dep = iterator.next(); 
            if(dep.birth > 0 && state.timestamp - dep.birth >= state.prob) {
                iterator.remove();
                continue;
            }
            
            if(!state.racy && dep.birth != 0 && this.getType().isAccessType() && !dep.check_dependency(this.thread)) {
                if((this.getType().isRead() && dep.checkReadCandidate(this.getVariable())) || (this.getType().isWrite() && dep.checkWriteCandidate(this.getVariable()))) {
                    state.racy = true;
                }
            }
            
            if(mustIgnore(dep)) {
                ignore(dep, state);
                if(dep.allThreads(state.tSet.size())) {
                    iterator.remove();
                }
            }
            else {
                if(((this.getType().isAccessType() && dep.birth == 0) || (this.getType().isAcquire() && dep.birth > 0))) {
                    DependentInfo dep_new = (DependentInfo) PipedDeepCopy.copy(dep);
                    if(this.getType().isAccessType()) {
                        if(this.getType().isRead()) {
                            dep_new.addReadCandidate(this.getVariable());
                        }
                        else {
                            dep_new.addWriteCandidate(this.getVariable());
                        }
                        dep_new.birth = state.timestamp;
                    }
                    ignore(dep_new, state);
                    if(!dep_new.allThreads(state.tSet.size())) {
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
            matched = true;
        }
        state.racy = false;
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
