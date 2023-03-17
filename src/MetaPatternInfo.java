import cmd.CmdOptions;
import cmd.GetOptions;
import engine.pattern.MetaInfo.MetaPatternEngine;

public class MetaPatternInfo {
    public static void main(String[] args) {		
		CmdOptions options = new GetOptions(args).parse();
		String patternFile = options.excludeList;
		MetaPatternEngine engine = new MetaPatternEngine(options.parserType, options.path, patternFile);
		engine.analyzeTrace();
	}
}
