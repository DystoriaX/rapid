package engine.pattern.PatternGenerator;

public class LocalPatternGenerator extends PatternGenerator {

    private int start;
    private int end;

    public LocalPatternGenerator(int k, String sourceFile, int start, int end) {
        super(k, sourceFile);
        this.start = start;
        this.end = end;
    }

    @Override
    public void generatePattern() {
        // TODO Auto-generated method stub
        
    }
    
}
