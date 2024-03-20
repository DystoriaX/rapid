import cmd.CmdOptions;
import cmd.GetOptions;
import engine.atomicity.conflictserializability.summarizedGraph.SummarizedGraphEngine;

public class SummarizedGraph {

	public SummarizedGraph() {

	}
	
	public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		SummarizedGraphEngine engine = new SummarizedGraphEngine(options.parserType, options.path, options.verbosity);

		boolean time_reporting = true;
		long startTimeAnalysis = 0;
		if(time_reporting){
			startTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
		}
		
		engine.analyzeTrace(false);
		
		System.out.println("Number of transactions remaining = " + engine.numTransactionsActive());
		
		if(time_reporting){
			long stopTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
			long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
			System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
		}
	}
}
