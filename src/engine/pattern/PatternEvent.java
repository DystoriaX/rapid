package engine.pattern;

import java.util.Set;

import event.Event;
import event.Lock;
import event.Thread;
import event.Variable;

import org.javatuples.Quartet;

public class PatternEvent extends Event {

    public String toHashString() {
        String basicInfo = type + "-" + this.getThread();
        if(type.isAccessType()) {
            return basicInfo + "-" + this.getVariable();
        }
        else if(type.isTransactionType()) {
            return basicInfo + "-";
        }
        else if(type.isLockType()) {
            return basicInfo + "-" + this.getLock();
        }
        else if(type.isExtremeType()) {
            return basicInfo + "-" + this.getTarget();
        }
        else {
            throw new IllegalArgumentException("Illegal type");
        }
    }

    public boolean isDependent(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet) {
        if(afterSet.getValue2().contains(thread)) {
            return true;
        }
        if(type.isRead()) {
            return afterSet.getValue1().contains(variable);
        }
        else if(type.isWrite()) {
            return afterSet.getValue0().contains(variable) 
                || afterSet.getValue1().contains(variable);
        }
        else if(type.isLockType()) {
            afterSet.getValue3().contains(lock);
        }
        return false;
    }

    public void updateAfterSet(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet) {
        if(type.isRead()) {
            afterSet.getValue2().add(thread);
            afterSet.getValue0().add(variable);
        }
        else if(type.isWrite()) {
            afterSet.getValue2().add(thread);
            afterSet.getValue1().add(variable);
        }
        else if(type.isLockType()) {
            afterSet.getValue2().add(thread);
            afterSet.getValue3().add(lock);
        }
    }

    public boolean Handle(PatternState state) {
        if(type.isAccessType()) {
		    return state.updateAndCheck(this);
        }
        return false;
	}
}
