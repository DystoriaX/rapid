package engine.atomicity.conflictserializability.summarizedGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import engine.atomicity.State;
import event.Lock;
import event.Thread;
import event.Variable;
import util.Transaction;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.*;

public class SummarizedGraphState extends State {

	// Internal data
	public HashMap<Thread, Integer> threadToIndex;
	private HashMap<Lock, Integer> lockToIndex;
	private HashMap<Variable, Integer> variableToIndex;
	private int numThreads;
	private int numLocks;
	private int numVariables;
	
	private int transaction_ctr;
	
	// S(v_i)
	public HashMap<Thread, HashSet<Lock>> threadToLockOfThisTr;
	public HashMap<Thread, HashSet<Variable>> threadToReadOfThisTr;
	public HashMap<Thread, HashSet<Variable>> threadToWriteOfThisTr;
	// C(v_i)
	public HashMap<Thread, HashSet<Lock>> threadToLockAftThisTr;
	public HashMap<Thread, HashSet<Variable>> threadToReadAftThisTr;
	public HashMap<Thread, HashSet<Variable>> threadToWriteAftThisTr;
    public HashMap<Thread, HashSet<Thread>> threadToThreadAftThisTr;
    // E
	public Graph<Integer, DefaultEdge> thb_graph;
    public CycleDetector<Integer, DefaultEdge> cycleDetector_thb_graph;
	public HashMap<Thread, Integer> threadToNestingDepth;
	
    public HashMap<Integer, Thread> indexToThread;
	//parameter flags
	public int verbosity;

	public SummarizedGraphState(HashSet<Thread> tSet, int verbosity) {
		this.verbosity = verbosity;
		initInternalData(tSet);
		initData(tSet);
	}

	private void initInternalData(HashSet<Thread> tSet) {
		this.threadToIndex = new HashMap<Thread, Integer>();
        this.indexToThread = new HashMap<Integer, Thread>();
		this.numThreads = 0;
		Iterator<Thread> tIter = tSet.iterator();
		while (tIter.hasNext()) {
			Thread thread = tIter.next();
			this.threadToIndex.put(thread, (Integer)this.numThreads);
            this.indexToThread.put((Integer)this.numThreads, thread);
			this.numThreads ++;
		}
		
		this.lockToIndex = new HashMap<Lock, Integer>();
		this.numLocks = 0;
		this.variableToIndex = new HashMap<Variable, Integer>();
		this.numVariables = 0;
		this.transaction_ctr = 0;
	}

	public void initData(HashSet<Thread> tSet) {
		this.threadToLockOfThisTr = new HashMap<Thread, HashSet<Lock>>();
        this.threadToLockAftThisTr = new HashMap<Thread, HashSet<Lock>>();

		this.threadToReadOfThisTr = new HashMap<Thread, HashSet<Variable>>();
        this.threadToReadAftThisTr = new HashMap<Thread, HashSet<Variable>>();

		this.threadToWriteOfThisTr = new HashMap<Thread, HashSet<Variable>>();
		this.threadToWriteAftThisTr = new HashMap<Thread, HashSet<Variable>>();

        this.threadToThreadAftThisTr = new HashMap<Thread, HashSet<Thread>>();

		this.thb_graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		this.cycleDetector_thb_graph = new CycleDetector<Integer, DefaultEdge>(this.thb_graph);
		this.threadToNestingDepth = new HashMap<Thread, Integer> ();

		for(Thread t: tSet){
			this.threadToNestingDepth.put(t, 0);
            initInternalDateNewThread(t);
		}
	}
	
	private int getFreshTransactionId(){
		this.transaction_ctr ++;
		return this.transaction_ctr;
	}
	
	public Transaction getFreshTransaction(Thread t){
		return new Transaction(t, this.getFreshTransactionId());
	}

    public boolean isThreadNoCurrentTransaction(Thread t) {
        return this.threadToNestingDepth.get(t) == 0;
    }

    public void initInternalDateNewThread(Thread t) {
        this.threadToLockOfThisTr.put(t, new HashSet<Lock>());
        this.threadToLockAftThisTr.put(t, new HashSet<Lock>());

        this.threadToReadOfThisTr.put(t, new HashSet<Variable>());
        this.threadToReadAftThisTr.put(t, new HashSet<Variable>());

        this.threadToWriteOfThisTr.put(t, new HashSet<Variable>());
        this.threadToWriteAftThisTr.put(t, new HashSet<Variable>());

        this.threadToThreadAftThisTr.put(t, new HashSet<Thread>());	
    }
	
	public int checkAndAddLock(Lock l){
		if(!lockToIndex.containsKey(l)){
			lockToIndex.put(l, this.numLocks);
			this.numLocks ++;
		}
		return lockToIndex.get(l);
	}
	
	public int checkAndAddVariable(Variable v){
		if(!variableToIndex.containsKey(v)){
			variableToIndex.put(v, this.numVariables);
			this.numVariables ++;
		}
		return variableToIndex.get(v);
	}

    public boolean updateGraphAndCheckLoop(Thread t) {
        Integer id = this.threadToIndex.get(t);
        List<Integer> predecessors = Graphs.predecessorListOf(this.thb_graph, id);
        List<Integer> successors = Graphs.successorListOf(this.thb_graph, id);
        HashSet<Integer> intersection = new HashSet<>(predecessors);
        intersection.retainAll(successors);
        if(!intersection.isEmpty()) {
            return true;
        }
        Graphs.removeVertexAndPreserveConnectivity(this.thb_graph, id);
        for(Integer idPre: predecessors){
            Thread tprime = this.indexToThread.get(idPre);

            this.threadToLockAftThisTr.get(tprime).addAll(this.threadToLockOfThisTr.get(t));
            this.threadToLockAftThisTr.get(tprime).addAll(this.threadToLockAftThisTr.get(t));

            this.threadToReadAftThisTr.get(tprime).addAll(this.threadToReadOfThisTr.get(t));
            this.threadToReadAftThisTr.get(tprime).addAll(this.threadToReadAftThisTr.get(t));

            this.threadToWriteAftThisTr.get(tprime).addAll(this.threadToWriteOfThisTr.get(t));
            this.threadToWriteAftThisTr.get(tprime).addAll(this.threadToWriteAftThisTr.get(t));

            this.threadToThreadAftThisTr.get(tprime).addAll(this.threadToThreadAftThisTr.get(t));
            this.threadToThreadAftThisTr.get(tprime).add(t);
        }
        this.initInternalDateNewThread(t);
        return false;
    }
	
	public void printMemory(){
		System.err.println("Number of threads = " + Integer.toString(this.numThreads));
		System.err.println("Number of locks = " + Integer.toString(this.numLocks));
		System.err.println("Number of variables = " + Integer.toString(this.numVariables));
	}

	public int numTransactionsActive() {
		return this.thb_graph.vertexSet().size();
	}
}
