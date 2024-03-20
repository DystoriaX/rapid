import cmd.CmdOptions;
import cmd.GetOptions;
import engine.prefix.race.PrefixEngine;


public class RacePrefix {
    public static void main(String[] args) {		
        CmdOptions options = new GetOptions(args).parse();
        PrefixEngine prefixEngine = new PrefixEngine(options.parserType, options.path, options.prob);
        prefixEngine.analyzeTrace();
	}
}
