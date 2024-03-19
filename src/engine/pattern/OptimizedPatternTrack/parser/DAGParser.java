package engine.pattern.OptimizedPatternTrack.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import util.DAG;

public class DAGParser {
    private String filename;

    public DAGParser(String filename) {
        this.filename = filename;
    }

    // Format:
    // NUM_NODES
    // NODE_ID LABEL
    // ...
    // NUM_EDGES
    // NODE_ID NODE_ID
    public DAG<String> parse() {
        DAG<String> g = new DAG<>();

        try {
            Scanner sc = new Scanner(new File(filename));

            int numNodes = sc.nextInt();

            for (int i = 0; i < numNodes; i++) {
                int nodeId = sc.nextInt();
                String label = sc.next();

                g.addNode(nodeId, label);
            }

            int numEdges = sc.nextInt();

            for (int i = 0; i < numEdges; i++) {
                int u, v;

                u = sc.nextInt();
                v = sc.nextInt();

                g.addEdge(u, v);
            }

            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("DAG Pattern file " + filename + " not found");
            e.printStackTrace();
        }

        return g;
    }

    public static void main(String[] args) {
        DAGParser parser = new DAGParser("benchmark/batik/pattern/dagpattern");
        DAG<String> g = parser.parse();
        List<DAG<String>.Node> nodes = g.getTopologicalOrder();

        for (DAG<String>.Node node : nodes) {
            System.out.println(node);
        }
    }
}
