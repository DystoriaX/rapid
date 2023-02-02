package engine.pattern;

import event.Event;

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

    public boolean Handle(PatternState state) {
		return state.updateAndCheck(this);
	}
    
    @Override
    public int hashCode() {
        return (this.getType().toString() + this.getVariable().toString()).hashCode();
    }
}
