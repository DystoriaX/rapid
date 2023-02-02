import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.PatternEngine;

public class PatternProperties {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		PatternEngine engine = new PatternEngine(options.parserType, options.path);

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
