package parse.rr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

import event.Event;
import event.Lock;
import event.Thread;
import event.Variable;
import parse.util.CannotParseException;
import parse.util.EventInfo;

public class ParseRoadRunner {
	private HashMap<String, Thread> threadMap;
	private HashMap<String, Lock> lockMap;
	private HashMap<String, Variable> variableMap;
	
	int totThreads;
	public BufferedReader bufferedReader;
	String line;
	Parse parser;
	EventInfo eInfo;
	public long totEvents;
	public long tot;
	
	private int locIdIndex;
	public HashMap<String, Integer> locationToIdMap;
	public HashMap<Integer, String> idToLocationMap;
	public HashSet<String> excludedPatterns;

	public long start = 0, end = 0;

	public ParseRoadRunner(String traceFile){
		threadMap = new HashMap<String, Thread>();
		lockMap = new HashMap<String, Lock>();
		variableMap = new HashMap<String, Variable>();
		totThreads = 0;
		totEvents = 0;
		
		locIdIndex = 0;
		locationToIdMap = new HashMap<String, Integer> ();
		idToLocationMap = new HashMap<>();

		bufferedReader = null;
		try{
			bufferedReader = new BufferedReader(new FileReader(traceFile));
		}
		catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + traceFile + "'");
		}
		
		excludedPatterns = new HashSet<String> ();
		
		parser = new Parse();
		eInfo = new EventInfo();
		line = null;
	}
	
	public ParseRoadRunner(String traceFile, long start, long end){
		threadMap = new HashMap<String, Thread>();
		lockMap = new HashMap<String, Lock>();
		variableMap = new HashMap<String, Variable>();
		totThreads = 0;
		totEvents = 0;
		
		locIdIndex = 0;
		locationToIdMap = new HashMap<String, Integer> ();
		idToLocationMap = new HashMap<>();

		bufferedReader = null;
		try{
			bufferedReader = new BufferedReader(new FileReader(traceFile));
		}
		catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + traceFile + "'");
		}
		
		excludedPatterns = new HashSet<String> ();
		
		parser = new Parse();
		eInfo = new EventInfo();
		line = null;
		this.start = start;
		this.end = end;
	}

	public ParseRoadRunner(String traceFile, String excludeFile){
		this(traceFile);
		String exclude_pattern;
		try {
			BufferedReader excludeFileBuffer = new BufferedReader(new FileReader(excludeFile));
			while(true) {
				try {
					exclude_pattern = excludeFileBuffer.readLine();
				}
				catch (IOException ioe) {
					System.err.println("Error occured when reading '" + excludeFile + "'");
					break;
				}
				boolean endOfFile = (exclude_pattern == null);
				if(endOfFile){
					try {
						excludeFileBuffer.close();
					} catch (IOException e) {
						System.err.println("Error closing buffered reader");
					}
					break;
				}
				excludedPatterns.add(exclude_pattern);
			}
		}
		catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + excludeFile + "'");
		}
	}
	
	private void computeThreadsAndResetState(String traceFile, boolean computeThreadSetAPriori) {
		if(computeThreadSetAPriori){
			Event e = new Event ();
			while(this.checkAndGetNext(e)){} //Read this trace to calculate threadSet
			this.tot = totEvents;
			this.totEvents = 0; //Resetting totEvents is required to ensure correct AuxId
			try{
				bufferedReader = new BufferedReader(new FileReader(traceFile));
			}
			catch (FileNotFoundException ex) {
				System.out.println("Unable to open file '" + traceFile + "'");
			}
		}
	}
	
	public ParseRoadRunner(String traceFile, boolean computeThreadSetAPriori){
		this(traceFile);
		computeThreadsAndResetState(traceFile, computeThreadSetAPriori);
	}

	public ParseRoadRunner(String traceFile, boolean computeThreadSetAPriori, long start, long end){
		this(traceFile, start, end);
		computeThreadsAndResetState(traceFile, computeThreadSetAPriori);
	}
	
	public ParseRoadRunner(String traceFile, String excludeFile, boolean computeThreadSetAPriori){
		this(traceFile, excludeFile);
		computeThreadsAndResetState(traceFile, computeThreadSetAPriori);
	}
	
	public HashSet<Thread> getThreadSet(){
		return new HashSet<Thread> (this.threadMap.values());
	}

	public void eInfo2Event(Event e) {
		String tname = eInfo.thread;
		if (!(threadMap.containsKey(tname))) {
			threadMap.put(tname, new Thread(tname));
			totThreads = totThreads + 1;
		}
		Thread t = threadMap.get(tname);

		
		if(!locationToIdMap.containsKey(eInfo.locId)){
			locationToIdMap.put(eInfo.locId, locIdIndex);
			idToLocationMap.put(locIdIndex, eInfo.locId);
			locIdIndex = locIdIndex + 1;
		}
		int LID = locationToIdMap.get(eInfo.locId); 
		String ename = "E" + Long.toString(totEvents);

		if (eInfo.type.isRead()) {
			String vname = eInfo.decor;
			if (!(variableMap.containsKey(vname))) {
				variableMap.put(vname, new Variable(vname));
			}
			Variable v = variableMap.get(vname);
			if(start <= end || (totEvents + 1 >= start && totEvents + 1 <= end)) {
				v.touchedThreads.add(t);
			}
			e.updateEvent(totEvents, LID, ename, eInfo.type, t, null, v, null);
		}

		else if (eInfo.type.isWrite()) {
			String vname = eInfo.decor;
			if (!(variableMap.containsKey(vname))) {
				variableMap.put(vname, new Variable(vname));
			}
			Variable v = variableMap.get(vname);
			if(start <= end || (totEvents + 1 >= start && totEvents + 1 <= end)) {
				v.touchedThreads.add(t);
			}
			e.updateEvent(totEvents, LID, ename, eInfo.type, t, null, v, null);
		}

		else if (eInfo.type.isAcquire()) {
			String lname = eInfo.decor;
			if (!(lockMap.containsKey(lname))) {
				lockMap.put(lname, new Lock(lname));
			}
			Lock l = lockMap.get(lname);

			e.updateEvent(totEvents, LID, ename, eInfo.type, t, l, null, null);
		}

		else if (eInfo.type.isRelease()) {
			String lname = eInfo.decor;
			if (!(lockMap.containsKey(lname))) {
				lockMap.put(lname, new Lock(lname));
			}
			Lock l = lockMap.get(lname);

			e.updateEvent(totEvents, LID, ename, eInfo.type, t, l, null, null);
		}

		else if (eInfo.type.isFork()) {
			String target_name = eInfo.decor;
			if (!(threadMap.containsKey(target_name))) {
				threadMap.put(target_name, new Thread(target_name));
			}
			Thread target = threadMap.get(target_name);

			e.updateEvent(totEvents, LID, ename, eInfo.type, t, null, null, target);
		}

		else if (eInfo.type.isJoin()) {
			String target_name = eInfo.decor;
			if (!(threadMap.containsKey(target_name))) {
				threadMap.put(target_name, new Thread(target_name));
			}
			Thread target = threadMap.get(target_name);

			e.updateEvent(totEvents, LID, ename, eInfo.type, t, null, null, target);
		}
		
		else if (eInfo.type.isBegin()) {
			e.updateEvent(totEvents, LID, ename, eInfo.type, t, null, null, null);
		}
		
		else if (eInfo.type.isEnd()) {
			e.updateEvent(totEvents, LID, ename, eInfo.type, t, null, null, null);
		}

		else {
			throw new IllegalArgumentException("Illegal type of event " + eInfo.type.toString());
		}

		totEvents = totEvents + 1;
	}

	/*
	private void getNextEvent(Event e){ //e is supposed to be over-written (deep copy) by the event-generated from the line read
		try {
			parser.getInfo(eInfo, line);
		} catch (CannotParseException ex) {
			System.err.println("Cannot parse line -> " + line);
		}
		eInfo2Event(e);
	}
	*/
	
	private boolean hasNext(){
		try {
			line = bufferedReader.readLine() ;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Error reading buffered reader");
		}

		boolean endOfFile = (line == null);
		if(endOfFile){
			try {
				bufferedReader.close();
			} catch (IOException e) {
				System.err.println("Error closing buffered reader");
			}
		}
		return !endOfFile;
	}
	
	public boolean checkAndGetNext(Event e){
		boolean EOF = !hasNext();
		boolean validEvent = false;
		while(!EOF){
			try {
				boolean shouldExclude = false;
				// for(String pattern: this.excludedPatterns) {
				// 	if(line.contains(pattern)) {
				// 		if(line.contains("Enter(") || line.contains("Exit(")) {
				// 			shouldExclude = true;
				// 			break;
				// 		}
				// 	}
				// }
				if(!line.startsWith("@")) {
					// System.out.println(line);
					shouldExclude = true;
				}
				if(!shouldExclude) {
					parser.getInfo(eInfo, line);
					validEvent = true;
				}
//				else {
//					System.out.println("Skipping line " + line);
//				}
			} catch (CannotParseException ex) {}
			if(validEvent) break;
			else{
				EOF = !hasNext();
			}
		}
		eInfo2Event(e);
		return !EOF;
	}

	public int getTotalThreads(){
		return totThreads;
	}

	public static void preProcessing(String trace_file, String result_file) {
		// String excludeFile = "/Users/umang/Repositories/doublechecker-single-run/avd/at_spec/sunflow.txt";
		Event e = new Event();
		System.out.println("Start Parsing");
		ParseRoadRunner parser = new ParseRoadRunner(trace_file);
		
		HashMap<Variable, Thread> varToThr = new HashMap<>();
		HashSet<Variable> concVariables = new HashSet<>();
		while(parser.checkAndGetNext(e)){
			if(e.getType().isAccessType()) {
				if(varToThr.keySet().contains(e.getVariable()) && !varToThr.get(e.getVariable()).equals(e.getThread())) {
					concVariables.add(e.getVariable());
				}
				if(!varToThr.keySet().contains(e.getVariable())) {
					varToThr.put(e.getVariable(), e.getThread());
				}
			}
		}
		System.out.println(concVariables.size());
		parser = new ParseRoadRunner(trace_file);
		// check whether the result file already exists
		try {
			FileWriter myWriter = new FileWriter(result_file);
			while(parser.checkAndGetNext(e)){
				if(!e.getType().isAccessType() || concVariables.contains(e.getVariable())) {
					myWriter.write(parser.line + "\n");
				}
			}
			myWriter.close();
		} catch (IOException exec) {
			System.out.println("An error occurred.");
			exec.printStackTrace();
		}
	}
	
	public static void demo(){
		String traceFile = "/Users/umang/Repositories/rapid-internal/traces/atomicity_tests/sunflow.rr";
		String excludeFile = "/Users/umang/Repositories/doublechecker-single-run/avd/at_spec/sunflow.txt";
		Event e = new Event();
		ParseRoadRunner parser = new ParseRoadRunner(traceFile, excludeFile);
		for(String s: parser.excludedPatterns) {
			System.out.println(s);
		}
		while(parser.checkAndGetNext(e)){
			System.out.println(e.toCompactString());
		}
//		System.out.println(parser.locationToIdMap);
		
	}

	public static void demo1(){
		String traceFile = "/Users/askarzhendongang/Code/rapid/benchmark/patternTest/parse1.rr";
		// String excludeFile = "/Users/umang/Repositories/doublechecker-single-run/avd/at_spec/sunflow.txt";
		Event e = new Event();
		System.out.println("Start Parsing");
		ParseRoadRunner parser = new ParseRoadRunner(traceFile);
		while(parser.checkAndGetNext(e)){
			System.out.println(e.toString());
		}
		System.out.println(parser.locationToIdMap);
	}
	
	public static void main(String args[]){
		demo1();
	}

}
