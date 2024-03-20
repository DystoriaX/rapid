package engine.pattern.MetaInfo;

import engine.pattern.PatternEvent;

public class MetaPatternEvent extends PatternEvent<MetaPatternState> {
    @Override
    public boolean Handle(MetaPatternState state) {
        super.Handle(state);
        state.update(this.locId);
        return false;
    }


    public boolean HandleSubAcquire(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubRelease(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubRead(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubWrite(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubFork(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubJoin(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubBegin(MetaPatternState state) {
        return false;
    }

	public boolean HandleSubEnd(MetaPatternState state) {
        return false;
    }

    public boolean HandleSubDummy(MetaPatternState state) {
        return false;
    }
}
