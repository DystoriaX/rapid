import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.PatternGenerator.LocalPatternGenerator;

public class PatternGeneration {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		int num = 20;
		String patternFile = options.path.substring(0, options.path.lastIndexOf('/') + 1) + "pattern_pre/pattern";
		LocalPatternGenerator generator = new LocalPatternGenerator(options.parserType, options.path, patternFile, num);
		generator.generatePatterns();
	}
}
