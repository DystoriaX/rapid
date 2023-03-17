import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.Bertoni.BertoniEngine;

public class BertoniPatternProperties {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		String patternFile = options.excludeList;
		BertoniEngine engine = new BertoniEngine(options.parserType, options.path, patternFile);
		boolean time_reporting = true;
		long startTimeAnalysis = 0;
		if(time_reporting){
			startTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
		}
		
		engine.analyzeTrace();
			
		if(time_reporting){
			long stopTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
			long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
			System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
		}
	}
}
