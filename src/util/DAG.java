package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

// This only works when T is hashable
public class DAG<T> {
    // We can have different nodes with the same data (label)
    public class Node {
        public int id;
        public T data;

        public Node(int id, T data) {
            this.id = id;
            this.data = data;
        }
    }

    private HashSet<Node> nodes = new HashSet<>();
    private HashMap<Node, ArrayList<Node>> adjList = new HashMap<>();

    public DAG() {
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Node u, Node v) {
        if (!nodes.contains(u) || !nodes.contains(v)) {
            throw new IllegalArgumentException("The given nodes are not in the graph");
        }

        adjList.getOrDefault(u, new ArrayList<>()).add(v);
    }

    public ArrayList<Node> getNeighbours(Node u) {
        return adjList.get(u);
    }

    public List<Node> getTopologicalOrder() {
        // Kahn's Algorithm
        HashMap<Node, Integer> inDegreeOf = new HashMap<>();

        for (Node node : nodes) {
            inDegreeOf.put(node, 0);
        }

        for (ArrayList<Node> neighbours : adjList.values()) {
            for (Node node : neighbours) {
                inDegreeOf.compute(node, (k, v) -> v + 1);
            }
        }

        ArrayList<Node> order = new ArrayList<>();
        Stack<Node> noIndegreeNodes = new Stack<>();

        for (HashMap.Entry<Node, Integer> e : inDegreeOf.entrySet()) {
            Node node = e.getKey();
            Integer inDeg = e.getValue();

            if (inDeg == 0) {
                noIndegreeNodes.push(node);
            }
        }

        while (!noIndegreeNodes.isEmpty()) {
            Node cur = noIndegreeNodes.pop();
            order.add(cur);

            for (Node next : adjList.get(cur)) {
                Integer inDeg = inDegreeOf.get(next);
                inDeg -= 1;

                if (inDeg == 0) {
                    noIndegreeNodes.push(next);
                }

                inDegreeOf.put(next, inDeg);
            }
        }

        return order;
    }
}
