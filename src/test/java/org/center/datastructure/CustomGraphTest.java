package org.center.datastructure;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomGraphTest {

    @Test
    void addEdgeCreatesNodesAndNeighborLink() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");

        assertEquals(List.of("B"), graph.getNeighbors("A"));
        assertEquals(List.of(), graph.getNeighbors("B"));
    }

    @Test
    void getNeighborsOnUnknownNodeReturnsEmptyList() {
        IGraph<String> graph = new CustomGraph<>();
        assertEquals(List.of(), graph.getNeighbors("ghost"));
    }

    @Test
    void getNeighborsReturnsDefensiveCopy() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");

        graph.getNeighbors("A").add("C");

        assertEquals(List.of("B"), graph.getNeighbors("A"));
    }

    @Test
    void bfsVisitsEachReachableNodeOnceInLevelOrder() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        List<String> order = graph.bfs("A");

        assertEquals(List.of("A", "B", "C", "D"), order);
    }

    @Test
    void bfsFromUnknownStartReturnsEmptyList() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addNode("A");

        assertEquals(List.of(), graph.bfs("ghost"));
    }

    @Test
    void dfsVisitsEachReachableNodeOnce() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");

        List<String> order = graph.dfs("A");

        assertEquals(4, order.size());
        assertEquals("A", order.get(0));
        assertTrue(order.containsAll(List.of("A", "B", "C", "D")));
    }

    @Test
    void topologicalSortRespectsAllEdgeOrderings() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        List<String> order = graph.topologicalSort();
        Map<String, Integer> position = indexOf(order);

        assertTrue(position.get("A") < position.get("B"));
        assertTrue(position.get("A") < position.get("C"));
        assertTrue(position.get("B") < position.get("D"));
        assertTrue(position.get("C") < position.get("D"));
    }

    @Test
    void topologicalSortOnCyclicGraphThrows() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        assertThrows(IllegalStateException.class, graph::topologicalSort);
    }

    @Test
    void hasCycleDetectsCycle() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        assertTrue(graph.hasCycle());
    }

    @Test
    void hasCycleReturnsFalseForDag() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        assertFalse(graph.hasCycle());
    }

    @Test
    void hasCycleDetectsSelfLoop() {
        IGraph<String> graph = new CustomGraph<>();
        graph.addEdge("A", "A");

        assertTrue(graph.hasCycle());
    }

    private Map<String, Integer> indexOf(List<String> order) {
        Map<String, Integer> position = new java.util.HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            position.put(order.get(i), i);
        }
        return position;
    }
}
