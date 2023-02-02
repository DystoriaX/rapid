package engine.pattern;

import java.util.Set;

import event.Event;
import event.Thread;
import event.Variable;

import org.javatuples.Pair;

public class PatternEvent extends Event {

    public PatternSymbol toPatternSymbol() {

        if(this.getType().isAccessType()) {
            return new PatternSymbol(this.getType(), this.getThread(), this.getVariable());
        }
        else if(this.getType().isTransactionType()) {
            return new PatternSymbol(this.getType(), this.getThread());
        }
        else if(this.getType().isLockType()) {
            return new PatternSymbol(this.getType(), this.getThread(), this.getLock());
        }
        else if(this.getType().isExtremeType()) {
            return new PatternSymbol(this.getType(), this.getThread(), this.getTarget());
        }
        else {
            throw new IllegalArgumentException("Illegal type");
        }
        
    }

    public String toHashString() {
        String basicInfo = this.getType() + "-" + this.getThread();
        if(this.getType().isAccessType()) {
            return basicInfo + "-" + this.getVariable();
        }
        else if(this.getType().isTransactionType()) {
            return basicInfo + "-";
        }
        else if(this.getType().isLockType()) {
            return basicInfo + "-" + this.getLock();
        }
        else if(this.getType().isExtremeType()) {
            return basicInfo + "-" + this.getTarget();
        }
        else {
            throw new IllegalArgumentException("Illegal type");
        }
    }

    public boolean isDependent(Pair<Set<Variable>, Set<Thread>> afterSet) {
        if(afterSet.getValue0().contains(getVariable())) {
            return true;
        }
        if(afterSet.getValue1().contains(getThread())) {
            return true;
        }
        return false;
    }

    public boolean Handle(PatternState state) {
        if(this.getType().isAccessType()) {
		    return state.updateAndCheck(this);
        }
        return false;
	}
    
    @Override
    public int hashCode() {
        return (this.getType().toString() + this.getVariable().toString()).hashCode();
    }
}
