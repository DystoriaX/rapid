package event;

import java.util.HashSet;

public class Variable extends Decoration {

	public static int variableCountTracker = 0;
	public HashSet<Thread> touchedThreads = new HashSet<>();

	public Variable() {
		this.id = variableCountTracker;
		variableCountTracker++;
		this.name = "__variable::" + Integer.toString(this.id) + "__";
	}

	public Variable(String sname) {
		this.id = variableCountTracker;
		variableCountTracker++;
		this.name = sname;
	}

}
