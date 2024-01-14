package engine.prefix.race;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import event.Lock;
import event.Thread;
import event.Variable;

public class State {
    public ArrayList<DependentInfo> states = new ArrayList<>(); 
    HashSet<Thread> tSet;
    public int raceCnt = 0;
    public boolean racy = false;
    double prob;
    public long timestamp;

    public State(HashSet<Thread> tSet, double prob) {
        states.add(new DependentInfo());
        this.tSet = tSet;
        this.prob = prob;
    };

    public void printMemory() {
        System.out.println(raceCnt);
        // System.out.println(states.size());
    }
}

class DependentInfo implements Serializable {
    HashSet<Thread> tSet = new HashSet<>();
    HashSet<Variable> wr_vars = new HashSet<>();
    HashSet<Lock> rel_locks = new HashSet<>();

    Variable candidate_var;
    boolean is_read_candidate;

    HashSet<Variable> wr_candidates = new HashSet<>();
    HashSet<Variable> rd_candidates = new HashSet<>();

    HashMap<Variable, HashSet<Thread>> wr_var_to_thread = new HashMap<>();
    HashMap<Variable, HashSet<Thread>> rd_var_to_thread = new HashMap<>(); 

    public long birth = 0;

    public boolean allThreads(int n) {
        return tSet.size() == n;
    }

    public boolean check_dependency(Thread t) {
        return tSet.contains(t);
    }

    public boolean check_dependency(Variable v) {
        return wr_vars.contains(v);
    }

    public boolean check_dependency(Lock l) {
        return rel_locks.contains(l);
    }

    public boolean tSetEmpty() {
        return tSet.isEmpty();
    }

    public void add(Thread t) {
        tSet.add(t);
    }

    public void add(Variable v) {
        wr_vars.add(v);
    }

    public void add(Lock l) {
        rel_locks.add(l);
    }

    public void remove(Variable v) {
        wr_vars.remove(v);
    }

    public void remove(Lock l) {
        rel_locks.remove(l);
    }
    
    public void addWriteCandidate(Variable var) {
        // boolean race = false;
        // if(rd_candidates.contains(var) || wr_candidates.contains(var)) {
        //     race = true;
        // }
        // wr_candidates.add(var);
        // if(!wr_var_to_thread.containsKey(var)) {
        //     wr_var_to_thread.put(var, new HashSet<>());
        // }
        // wr_var_to_thread.get(var).add(t);
        // return race;
        candidate_var = var;
        is_read_candidate = false;
    }

    public boolean checkWriteCandidate(Variable var) {
        return candidate_var != null && var.getId() == candidate_var.getId();
    }

    public void addReadCandidate( Variable var) {
        // boolean race = false;
        // if(wr_candidates.contains(var)) {
        //     race = true;
        // }
        // rd_candidates.add(var);        
        // if(!rd_var_to_thread.containsKey(var)) {
        //     rd_var_to_thread.put(var, new HashSet<>());
        // }
        // rd_var_to_thread.get(var).add(t);
        // return race;
        candidate_var = var;
        is_read_candidate = true;
    }

    public boolean checkReadCandidate(Variable var) {
        return candidate_var != null && var.getId() == candidate_var.getId() && !is_read_candidate;
    }

    // public void removeRdCandidate(Thread t, Variable var) {
    //     if(rd_var_to_thread.containsKey(var)) {
    //         rd_var_to_thread.get(var).remove(t);
    //         if(rd_var_to_thread.get(var).isEmpty()) {
    //             rd_var_to_thread.remove(var);
    //             rd_candidates.remove(var);
    //         }
    //     }
    // }

    // public void removeWrCandidate(Thread t, Variable var) {
    //     if(wr_var_to_thread.containsKey(var)) {
    //         wr_var_to_thread.get(var).remove(t);
    //         if(wr_var_to_thread.get(var).isEmpty()) {
    //             wr_var_to_thread.remove(var);
    //             wr_candidates.remove(var);
    //         }
    //     }
    // }
}
