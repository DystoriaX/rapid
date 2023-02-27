import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.Bertoni.BertoniEngine;
import engine.pattern.PatternGenerator.LocalPatternGenerator;
import engine.pattern.Vectorclock.VectorClockEngine;

public class PatternProperties {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		int num = 10;
		String patternFile = options.path.substring(0, options.path.lastIndexOf('/') + 1) + "pattern";
		// int num = 1;
		// String patternFile = "benchmark/exp4j/patternshort.rr";
		// LocalPatternGenerator generator = new LocalPatternGenerator(5, options.path, patternFile, num, 0.05);
		// generator.generatePatterns();

		for(int i = 0; i < num; i++) {
			VectorClockEngine engine = new VectorClockEngine(options.parserType, options.path, patternFile + i);
			// VectorClockEngine engine = new VectorClockEngine(options.parserType, options.path, patternFile);
			// BertoniEngine engine = new BertoniEngine(options.parserType, options.path, patternFile);
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
}
