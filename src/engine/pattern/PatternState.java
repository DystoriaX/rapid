package engine.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import event.Lock;
import event.Thread;
import event.Variable;
import org.javatuples.Quartet;

public class PatternState {

    private Set<List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>>> currentState = new HashSet<>();
    private Set<List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>>> backupState = new HashSet<>();
    ArrayList<String> pattern = new ArrayList<>();
    PatternEvent handlerEvent;

    public PatternState() {
        currentState.add(new ArrayList<>(Collections.nCopies(3, null)));
    }

    public boolean updateAndCheck(PatternEvent event) {
        handlerEvent = event;
        for(List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> afterSetGroup: currentState) {
            backupState.add(updateAfterSetGroup(afterSetGroup));
            if(isNewAfterSet(afterSetGroup)) {
                int cnt = 0;
                for(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet: afterSetGroup) {
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
        Set<List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>>> tmp = currentState;
        currentState = backupState;
        backupState = tmp;
        // printCurrentState();
        return false;
    }

    protected List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> updateAfterSetGroup(List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> afterSetGroup) {
        for(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet: afterSetGroup) {
            if(afterSet != null && handlerEvent.isDependent(afterSet)) {
                handlerEvent.updateAfterSet(afterSet);
            }
        }
        return afterSetGroup;
    }

    private boolean patternContains(String sym) {
        return pattern.contains(sym);
    }

    private int patternIndexOf(String sym) {
        return pattern.indexOf(sym);
    }

    protected boolean isNewAfterSet(List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> afterSetGroup) {
        if(!patternContains(handlerEvent.toHashString()) || afterSetGroup.get(patternIndexOf(handlerEvent.toHashString())) != null) {
            return false;
        }
        for(int i = patternIndexOf(handlerEvent.toHashString()) + 1; i < afterSetGroup.size(); i++) {
            Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet = afterSetGroup.get(i);
            if(afterSet != null && handlerEvent.isDependent(afterSet)) {
                return false;
            }
        }
        return true;
    }

    protected List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> newAfterSetGroup(List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> afterSetGroup) {
        List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> newAfterSetGroup = new ArrayList<>();
        Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> newAfterSet = null;
        for(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet: afterSetGroup) {
            newAfterSet = null;
            if(afterSet != null) {
                newAfterSet = new Quartet<>(new HashSet<Variable>(), new HashSet<Variable>(), new HashSet<Thread>(), new HashSet<Lock>());
                newAfterSet.getValue0().addAll(afterSet.getValue0());
                newAfterSet.getValue1().addAll(afterSet.getValue1());
                newAfterSet.getValue2().addAll(afterSet.getValue2());
                newAfterSet.getValue3().addAll(afterSet.getValue3());
            }
            newAfterSetGroup.add(newAfterSet);
        }
        newAfterSet = new Quartet<>(new HashSet<Variable>(), new HashSet<Variable>(), new HashSet<Thread>(), new HashSet<Lock>());
        handlerEvent.updateAfterSet(newAfterSet);
        newAfterSetGroup.set(patternIndexOf(handlerEvent.toHashString()), newAfterSet);
        return newAfterSetGroup;
    }

    public void printCurrentState() {
        System.out.println("Current State of size " + currentState.size());
        for(List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> afterSetGroup: currentState) {
            System.out.println("AfterSet Group of size " + afterSetGroup.size());
            for(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet: afterSetGroup) {
                System.out.println(afterSet);
            }
        }
    }

    public void printMemory() {
        System.out.println("Current State of size " + currentState.size());
        int cnt = 0;
        for(List<Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>>> afterSetGroup: currentState) {
            System.out.println("Number " + cnt++ + ": ");
            for(Quartet<Set<Variable>, Set<Variable>, Set<Thread>, Set<Lock>> afterSet: afterSetGroup) {
                if(afterSet != null) {
                    System.out.print(afterSet.getValue0().size() + " ");
                    System.out.print(afterSet.getValue1().size() + " ");
                    System.out.print(afterSet.getValue2().size() + " ");
                    System.out.print(afterSet.getValue3().size());
                    System.out.println();
                }
                else {
                    System.out.println("null");
                }
            }
        }
    }
}
