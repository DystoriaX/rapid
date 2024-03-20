package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

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
        HashMap<Node, Integer> inDegreeOf = getIndegree();

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

    private HashMap<Node, Integer> getIndegree() {
        HashMap<Node, Integer> inDegreeOf = new HashMap<>();

        for (Node node : idToNodes.values()) {
            inDegreeOf.put(node, 0);
        }

        for (ArrayList<Node> neighbours : adjList.values()) {
            for (Node node : neighbours) {
                inDegreeOf.compute(node, (k, v) -> v + 1);
            }
        }

        return inDegreeOf;
    }

    public <U> DAG<U> map(Function<T, U> f) {
        DAG<U> newG = new DAG<>();

        for (Node node : idToNodes.values()) {
            newG.addNode(node.id, f.apply(node.data));
        }

        for (Node node : idToNodes.values()) {
            for (Node otherNode : adjList.get(node)) {
                newG.addEdge(node.id, otherNode.id);
            }
        }

        return newG;
    }

    public void dfs(Consumer<Node> preFunction, Consumer<Node> postFunction) {
        HashMap<Node, Integer> inDegreeOf = getIndegree();

        for (HashMap.Entry<Node, Integer> e : inDegreeOf.entrySet()) {
            if (e.getValue() != 0) {
                continue;
            }

            dfs(e.getKey(), preFunction, postFunction);
        }
    }

    private void dfs(Node u, Consumer<Node> preFunction, Consumer<Node> postFunction) {
        preFunction.accept(u);
        for (Node v : adjList.get(u)) {
            dfs(v, preFunction, postFunction);
        }
        postFunction.accept(u);
    }
}
