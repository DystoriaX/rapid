import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.Bertoni.BertoniEngine;
import engine.pattern.PatternGenerator.LocalPatternGenerator;
import engine.pattern.Vectorclock.VectorClockEngine;

public class PatternProperties {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		// VectorClockEngine engine = new VectorClockEngine(options.parserType, options.path);
		// BertoniEngine engine = new BertoniEngine(options.parserType, options.path);
		LocalPatternGenerator generator = new LocalPatternGenerator(5, options.path, "benchmark/exp4j/pattern", 10, 0.05);
		generator.generatePatterns();
		// boolean time_reporting = true;
		// long startTimeAnalysis = 0;
		// if(time_reporting){
		// 	startTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
		// }
		
		// engine.analyzeTrace();
        	
		// if(time_reporting){
		// 	long stopTimeAnalysis = System.currentTimeMillis(); //System.nanoTime();
		// 	long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
		// 	System.out.println("Time for full analysis = " + timeAnalysis + " milliseconds");
		// }

	}
}
