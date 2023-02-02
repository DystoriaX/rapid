package engine.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PatternState {

    private Set<Map<PatternSymbol, Set<PatternSymbol>>> currentState = new HashSet<>();
    ArrayList<String> pattern = new ArrayList<>();
    private Map<String, PatternSymbol> symbolPool = new HashMap<>();
    PatternSymbol handlerSymbol;

    public PatternState() {
        currentState.add(new HashMap<>());
    }

    public boolean updateAndCheck(PatternEvent event) {
        if(!symbolPool.keySet().contains(event.toHashString())) {
            symbolPool.put(event.toHashString(),event.toPatternSymbol());
        }
        handlerSymbol = symbolPool.get(event.toHashString());

        Set<Map<PatternSymbol, Set<PatternSymbol>>> newState = new HashSet<>();
        for(Map<PatternSymbol, Set<PatternSymbol>> afterSetGroup: currentState) {
            updateAfterSetGroup(afterSetGroup);
            // newState.add(afterSetGroup);
            if(isNewAfterSet(afterSetGroup)) {
                if(afterSetGroup.keySet().size() == pattern.size() - 1) {
                    printCurrentState();
                    return true;
                }
                newState.add(newAfterSetGroup(afterSetGroup));
            }
        }
        currentState.addAll(newState);
        printCurrentState();
        return false;
    }

    protected void updateAfterSetGroup(Map<PatternSymbol, Set<PatternSymbol>> afterSetGroup) {
        for(PatternSymbol event_k: afterSetGroup.keySet()) {
            if(afterSetGroup.get(event_k) == null) {
                System.out.println("null");
            }
            if(isDependentOnAfterSet(afterSetGroup.get(event_k))) {
                afterSetGroup.get(event_k).add(handlerSymbol);
            }
        }
    }

    private boolean isDependentOnAfterSet(Set<PatternSymbol> afterSet) {
        for(PatternSymbol s: afterSet) {
            if(s.isDependent(handlerSymbol)) {
                return true;
            } 
        }
        return false;
    }

    private boolean patternContains(PatternSymbol sym) {
        for(String event: pattern) {
            if(symbolPool.containsKey(event)
                && symbolPool.get(event).equals(sym)) {
                    return true;
            }
        }
        return false;
    }

    private int patternIndexOf(PatternSymbol sym) {
        int cnt = 0;
        for(String event: pattern) {
            if(symbolPool.containsKey(event)
                && symbolPool.get(event).equals(sym)) {
                    return cnt;
            }
            cnt++;
        }
        throw new IllegalArgumentException("illegal pattern");
    }

    protected boolean isNewAfterSet(Map<PatternSymbol, Set<PatternSymbol>> afterSetGroup) {
        if(!patternContains(handlerSymbol) || afterSetGroup.keySet().contains(handlerSymbol)) {
            return false;
        }
        for(PatternSymbol event_k: afterSetGroup.keySet()) {
            if(patternIndexOf(event_k) > patternIndexOf(handlerSymbol) && afterSetGroup.get(event_k).contains(handlerSymbol)) {
                return false;
            }
        }
        return true;
    }

    protected Map<PatternSymbol, Set<PatternSymbol>> newAfterSetGroup(Map<PatternSymbol, Set<PatternSymbol>> afterSetGroup) {
        Map<PatternSymbol, Set<PatternSymbol>> newAfterSetGroup = new HashMap<>();
        for(PatternSymbol event_k: afterSetGroup.keySet()) {
            Set<PatternSymbol> newAfterSet = new HashSet<>();
            newAfterSet.addAll(afterSetGroup.get(event_k));
            newAfterSetGroup.put(event_k, newAfterSet);
        }
        newAfterSetGroup.put(handlerSymbol, new HashSet<PatternSymbol>(Arrays.asList(handlerSymbol)));
        return newAfterSetGroup;
    }

    public void printCurrentState() {
        System.out.println("Current State:");
        for(Map<PatternSymbol, Set<PatternSymbol>> afterSetGroup: currentState) {
            System.out.println("AfterSet Group:");
            for(PatternSymbol event_k: afterSetGroup.keySet()) {
                System.out.println(event_k + ":");
                System.out.println(afterSetGroup.get(event_k));
            }
        }
    }

    public void printMemory() {

    }
}
