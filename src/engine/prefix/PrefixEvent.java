package engine.prefix;

import java.util.ArrayList;

import org.javatuples.Pair;

import engine.pattern.Vectorclock.VectorClockEvent;
import engine.pattern.Vectorclock.VectorClockState;
import util.PipedDeepCopy;

public class PrefixEvent extends VectorClockEvent {

    public boolean Handle(State state) {
        System.out.println(state.states.size());
        ArrayList<Pair<VectorClockState, DependentInfo>> newStates = new ArrayList<>();
        boolean matched = false;
        for(Pair<VectorClockState, DependentInfo> track_state: state.states){
            if(mustIgnore(track_state.getValue1())) {
                ignore(track_state.getValue1());
            }
            else {
                DependentInfo dep_new = (DependentInfo) PipedDeepCopy.copy(track_state.getValue1());
                ignore(dep_new);
                VectorClockState copied_state = (VectorClockState) PipedDeepCopy.copy(track_state.getValue0());
                newStates.add(new Pair<VectorClockState,DependentInfo>(copied_state, dep_new));

                matched = super.Handle(track_state.getValue0());
                if(matched) {
                    return true;
                }
                if(this.getType().isWrite()) {
                    track_state.getValue1().remove(this.variable);
                }
                if(this.getType().isAcquire()) {
                    track_state.getValue1().add(this.lock);
                }
                if(this.getType().isRelease()) {
                    track_state.getValue1().remove(this.lock);
                }
            }
        }
        state.states.addAll(newStates);
        // for(Pair<VectorClockState, DependentInfo> track_state: state.states){
        //     track_state.getValue0().printMemory();
        // }
		return matched;
	}

    boolean mustIgnore(DependentInfo dep){

        if(this.getType().isAcquire()) {
            return dep.check_dependency(this.thread) || dep.check_dependency(this.lock);
        }

        if (this.getType().isRead()) {
            return dep.check_dependency(this.thread) || dep.check_dependency(this.variable);
        }

		return dep.check_dependency(this.thread);
	}

    void ignore(DependentInfo dep) {
        dep.add(this.thread);

        if(this.getType().isWrite()) {
            dep.add(this.variable);
        }
    }
}
