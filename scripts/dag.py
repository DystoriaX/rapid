import sys
import os
import re

from typing import List, Tuple

Node = Tuple[int, str]
Edge = Tuple[int, int]
Graph = Tuple[List[Node], List[Edge]]

def get_sequence(filename: str):
    with open(filename, "r") as reader:
        sequence = [line.strip() for line in reader.readlines()]

        # Ignore first two lines
        sequence = sequence[2:]

    return sequence

def generate_dag(sequence: List[str]) -> Graph:
    # We only allow sequences of length 11
    # We limit the bug depth to be 5
    # It fans out and fans in interleavinglyy
    #           1     6
    #        0  2  5  7  10
    #           3     8
    #           4     9
    #
    # depth: 1  2  3  4  5

    width = 3
    depth = 10
    while len(sequence) > depth * (width + 1) + 1:
        sequence.pop()

    nodes = []

    for i in range(len(sequence)):
        nodes.append((i, sequence[i]))

    edges = []

    for i in range(0, len(sequence), width + 1):
        for j in range(1, width + 1, 1):
            if i > 1:
                edges.append((i - j, i))

            if i < len(sequence) - 1:
                edges.append((i, i + j))

    edges = sorted(edges)

    return (nodes, edges)

def generate_trie_from_dag(dag: Graph) -> Graph:
    def get_neighbours(u, edges):
        return [edge[1] for edge in edges if edge[0] == u]

    def get_label(id, nodes):
        for node in nodes:
            nid, label = node

            if id == nid:
                return label

        raise Exception

    dag_nodes, dag_edges = dag

    traversed = set()

    trie_nodes = dag_nodes[:]
    trie_edges = []

    def dfs(u, u_id):
        traversed.add(u)

        for v in get_neighbours(u, dag_edges):
            if v in traversed:
                v_id = len(trie_nodes)
                trie_nodes.append((v_id, get_label(v, dag_nodes)))
            else:
                v_id = v

            trie_edges.append((u_id, v_id))
            dfs(v, v_id)

    dfs(0, 0)
    return trie_nodes, trie_edges



def write_dag(dag: Graph, filename: str):
    nodes, edges = dag

    with open(filename, "w") as f:
        print(len(nodes), file=f)

        for node in nodes:
            id, label = node
            print(id, label, file=f)

        print(len(edges), file=f)

        for edge in edges:
            u, v = edge
            print(u, v, file=f)

if __name__ == '__main__':
    print("Running as main...")

    workdir = sys.argv[1]
    files = os.listdir(workdir)

    for file in files:
        if not re.match(r'pattern*', file):
            print("Skipping " + file)
            continue

        filepath = os.path.join(workdir, file)
        dag_filepath = os.path.join(workdir, "dag_" + file)
        trie_filepath = os.path.join(workdir, "trie_" + file)

        seq = get_sequence(filepath)
        dag = generate_dag(seq)
        trie = generate_trie_from_dag(dag)

        write_dag(dag, dag_filepath)
        write_dag(trie, trie_filepath)
