package engine.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import event.Thread;
import event.Variable;
import org.javatuples.Pair;

public class PatternState {

    private Set<List<Pair<Set<Variable>, Set<Thread>>>> currentState = new HashSet<>();
    private Set<List<Pair<Set<Variable>, Set<Thread>>>> backupState = new HashSet<>();
    ArrayList<String> pattern = new ArrayList<>();
    PatternEvent handlerEvent;

    public PatternState() {
        currentState.add(new ArrayList<>(Collections.nCopies(3, null)));
    }

    public boolean updateAndCheck(PatternEvent event) {
        handlerEvent = event;
        for(List<Pair<Set<Variable>, Set<Thread>>> afterSetGroup: currentState) {
            backupState.add(updateAfterSetGroup(afterSetGroup));
            if(isNewAfterSet(afterSetGroup)) {
                int cnt = 0;
                for(Pair<Set<Variable>, Set<Thread>> afterSet: afterSetGroup) {
                    if(afterSet != null) {
                        cnt++;
                    }
                }
                if(cnt == pattern.size() - 1) {
                    // printCurrentState();
                    return true;
                }
                backupState.add(newAfterSetGroup(afterSetGroup));
            }
        }
        currentState.clear();
        Set<List<Pair<Set<Variable>, Set<Thread>>>> tmp = currentState;
        currentState = backupState;
        backupState = tmp;
        // printCurrentState();
        return false;
    }

    protected List<Pair<Set<Variable>, Set<Thread>>> updateAfterSetGroup(List<Pair<Set<Variable>, Set<Thread>>> afterSetGroup) {
        for(Pair<Set<Variable>, Set<Thread>> afterSet: afterSetGroup) {
            if(afterSet != null && isDependentOnAfterSet(afterSet)) {
                afterSet.getValue0().add(handlerEvent.getVariable());
                afterSet.getValue1().add(handlerEvent.getThread());
            }
        }
        return afterSetGroup;
    }

    private boolean isDependentOnAfterSet(Pair<Set<Variable>, Set<Thread>> afterSet) {
        return handlerEvent.isDependent(afterSet);
    }

    private boolean patternContains(String sym) {
        return pattern.contains(sym);
    }

    private int patternIndexOf(String sym) {
        return pattern.indexOf(sym);
    }

    protected boolean isNewAfterSet(List<Pair<Set<Variable>, Set<Thread>>> afterSetGroup) {
        if(!patternContains(handlerEvent.toHashString()) || afterSetGroup.get(patternIndexOf(handlerEvent.toHashString())) != null) {
            return false;
        }
        for(int i = patternIndexOf(handlerEvent.toHashString()) + 1; i < afterSetGroup.size(); i++) {
            Pair<Set<Variable>, Set<Thread>> afterSet = afterSetGroup.get(i);
            if(afterSet != null && isDependentOnAfterSet(afterSet)) {
                return false;
            }
        }
        
        return true;
    }

    protected List<Pair<Set<Variable>, Set<Thread>>> newAfterSetGroup(List<Pair<Set<Variable>, Set<Thread>>> afterSetGroup) {
        List<Pair<Set<Variable>, Set<Thread>>> newAfterSetGroup = new ArrayList<>();
        Pair<Set<Variable>, Set<Thread>> newAfterSet = null;
        for(Pair<Set<Variable>, Set<Thread>> afterset: afterSetGroup) {
            newAfterSet = null;
            if(afterset != null) {
                newAfterSet = new Pair<>(new HashSet<Variable>(), new HashSet<Thread>());
                newAfterSet.getValue0().addAll(afterset.getValue0());
                newAfterSet.getValue1().addAll(afterset.getValue1());
            }
            newAfterSetGroup.add(newAfterSet);

        }
        newAfterSet = new Pair<>(new HashSet<Variable>(), new HashSet<Thread>());
        newAfterSet.getValue0().add(handlerEvent.getVariable());
        newAfterSet.getValue1().add(handlerEvent.getThread());
        newAfterSetGroup.set(patternIndexOf(handlerEvent.toHashString()), newAfterSet);
        return newAfterSetGroup;
    }

    public void printCurrentState() {
        System.out.println("Current State of size " + currentState.size());
        for(List<Pair<Set<Variable>, Set<Thread>>> afterSetGroup: currentState) {
            System.out.println("AfterSet Group of size " + afterSetGroup.size());
            for(Pair<Set<Variable>, Set<Thread>> afterset: afterSetGroup) {
                System.out.println(afterset);
            }
        }
    }

    public void printMemory() {
        System.out.println(currentState.size());
    }
}
