package engine.pattern;

import event.EventType;
import event.Lock;
import event.Variable;
import event.Thread;

public class PatternSymbol {
    protected EventType type;
	protected Thread thread;
	
	//Data for Acquire/Release
	protected Lock lock;
	// protected HashSet<Variable> readVarSet;
	// protected HashSet<Variable> writeVarSet;
	
	//Data for Read/Write
	protected Variable variable;
	// private HashSet<Lock> lockSet;
	
	//Data for Fork/Join
	protected Thread target;

    public PatternSymbol(EventType tp, Thread th) {
        type = tp;
        thread = th;
    }

    public PatternSymbol(EventType tp, Thread th, Variable var) {
        type = tp;
        thread = th;
        variable = var;
    }

    public PatternSymbol(EventType tp, Thread th, Lock l) {
        type = tp;
        thread = th;
        lock = l;
    }

    public PatternSymbol(EventType tp, Thread th, Thread tar) {
        type = tp;
        thread = th;
        target = tar;
    }

    public EventType getType() {
        return type;
    }

    public Thread getThread() {
        return thread;
    }

    public Variable getVariable() {
        return variable;
    }

    public Lock getLock() {
        return lock;
    }

    public Thread getTarget() {
        return target;
    }

    public boolean isDependent(PatternSymbol event_b) {
        if(this.getThread().equals(event_b.getThread())) {
            return true;
        }

        if(this.getType().isAccessType() && event_b.getType().isAccessType()) {
            if(this.getVariable().equals(event_b.getVariable()) && (this.getType().isWrite() || event_b.getType().isWrite())) {
                return true;
            }
        }

        return false;
    }

    public String toFullStringForChildren() {
		return "(Symbol" + "-"
				+ this.type.toString() + "-" + this.thread.toString();
	}
	
    public String toString() {
        return toFullString();
    }

	public String toFullString(){
		String str = "";
		
		if(this.getType().isLockType())		str = this.toFullStringLockType();
		if(this.getType().isAccessType())	str = this.toFullStringAccessType();
		if(this.getType().isExtremeType())	str = this.toFullStringExtremeType();
		
		return str;
	}

    public String toFullStringAccessType() {
		return toFullStringForChildren() 
				+ "-" + this.getVariable().toString() + ")";
	}

    public String toFullStringExtremeType() {
		return toFullStringForChildren() + "-" + this.getTarget().toString() + ")";
	}

    public String toFullStringLockType() {
		return toFullStringForChildren() + "-" + this.getLock().toString() + ")";
	}
}
