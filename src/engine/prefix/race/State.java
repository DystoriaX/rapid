package engine.prefix.race;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import event.Lock;
import event.Thread;
import event.Variable;

public class State {
    public ArrayList<DependentInfo> states = new ArrayList<>(); 
    public HashSet<Thread> tSet;
    public int raceCnt = 0;
    public boolean racy = false;
    double prob;
    public long timestamp;
    public HashSet<Integer> racyLocs = new HashSet<>();

    public State(HashSet<Thread> tSet, double prob) {
        states.add(new DependentInfo());
        this.tSet = tSet;
        this.prob = prob;
    };

    public void reset() {
        states.clear();
        states.add(new DependentInfo());
    }

    public void forget(Thread thread) {
        for(Iterator<DependentInfo> iterator = states.iterator(); iterator.hasNext();){
            DependentInfo dep = iterator.next(); 
            dep.add(thread);
            
            if(dep.allThreads(tSet.size())) {
                iterator.remove();
            }
        }
    }

    public void printMemory() {
    }
}

class DependentInfo implements Serializable {
    HashSet<Integer> tSet = new HashSet<>();
    HashSet<Integer> wr_vars = new HashSet<>();
    HashSet<Integer> rel_locks = new HashSet<>();

    Variable candidate_var;
    boolean is_read_candidate;

    public long birth = 0;

    public boolean allThreads(int n) {
        return tSet.size() == n;
    }

    public boolean check_dependency(Thread t) {
        return tSet.contains(t.getId());
    }

    public boolean check_dependency(Variable v) {
        return wr_vars.contains(v.getId());
    }

    public boolean check_dependency(Lock l) {
        return rel_locks.contains(l.getId());
    }

    public boolean tSetEmpty() {
        return tSet.isEmpty();
    }

    public void add(Thread t) {
        tSet.add(t.getId());
    }

    public void add(Variable v) {
        wr_vars.add(v.getId());
    }

    public void add(Lock l) {
        rel_locks.add(l.getId());
    }

    public void remove(Variable v) {
        wr_vars.remove(v.getId());
    }

    public void remove(Lock l) {
        rel_locks.remove(l.getId());
    }
    
    public void addWriteCandidate(Variable var) {
        candidate_var = var;
        is_read_candidate = false;
    }

    public boolean checkWriteCandidate(Variable var) {
        return candidate_var != null && var.getId() == candidate_var.getId();
    }

    public void addReadCandidate( Variable var) {
        candidate_var = var;
        is_read_candidate = true;
    }

    public boolean checkReadCandidate(Variable var) {
        return candidate_var != null && var.getId() == candidate_var.getId() && !is_read_candidate;
    }

}
