package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Trie<T> {
  public static int nodeCount = 0;

  public class Node {
    private int id;
    private HashMap<T, Node> children;
    private boolean isTerminal;

    public Node() {
      nodeCount += 1;

      id = nodeCount;
      children = new HashMap<>();
      isTerminal = false;
    }

    public int getId() {
      return id;
    }

    public boolean hasChild(T value) {
      return children.containsKey(value);
    }

    public Node getChild(T value) {
      return children.get(value);
    }

    public void addChild(T value, Node node) {
      children.put(value, node);
    }

    public void setIsTerminal(boolean flag) {
      isTerminal = flag;
    }

    public boolean isTerminalNode() {
      return isTerminal;
    }
  }

  private Node root;

  public Trie() {
    root = new Node();
  }

  public void add(List<T> list) {
    Node current = root;

    for (T element : list) {
      if (!current.hasChild(element)) {
        current.addChild(element, current);
      }

      current = current.getChild(element);
    }

    current.setIsTerminal(true);
  }

  public ArrayList<Integer> getIds(List<T> list) {
    Node current = root;

    ArrayList<Integer> ids = new ArrayList<>();

    for (T element : list) {
      if (!current.hasChild(element)) {
        return null;
      }

      current = current.getChild(element);
      ids.add(current.getId());
    }

    return ids;
  }

  // Dangerous method if not used properly
  // Users are not supposed to mutate the Node object
  // and its predecessors.
  public Node getRoot() {
    return root;
  }
}
