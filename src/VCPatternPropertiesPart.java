import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.Vectorclock.VectorClockEngine;

public class VCPatternPropertiesPart {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		String patternFile = options.excludeList;
		VectorClockEngine engine = new VectorClockEngine(options.parserType, options.path, patternFile);
        engine.partition = true;
		boolean time_reporting = false;
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
