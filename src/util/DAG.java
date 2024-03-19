package util;

import java.util.ArrayList;
import java.util.HashMap;
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

        public String toString() {
            return "Node{id=" + id + ", data=" + data + "}";
        }
    }

    private HashMap<Integer, Node> idToNodes = new HashMap<>();
    private HashMap<Node, ArrayList<Node>> adjList = new HashMap<>();

    public DAG() {
    }

    public void addNode(int id, T data) {
        Node node = new Node(id, data);

        idToNodes.put(id, node);
        adjList.put(node, new ArrayList<>());
    }

    public void addEdge(Node u, Node v) {
        if (!idToNodes.containsValue(u) || !idToNodes.containsValue(v)) {
            throw new IllegalArgumentException("The given nodes are not in the graph");
        }

        adjList.get(u).add(v);
    }

    public void addEdge(int uId, int vId) {
        addEdge(idToNodes.get(uId), idToNodes.get(vId));
    }

    public ArrayList<Node> getNeighbours(Node u) {
        return adjList.get(u);
    }

    public List<Node> getTopologicalOrder() {
        // Kahn's Algorithm
        HashMap<Node, Integer> inDegreeOf = new HashMap<>();

        for (Node node : idToNodes.values()) {
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
