package org.center.datastructure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 自訂 Graph（directed, adjacency list）。
 * 節點插入順序另外用 List 維護，供走訪／拓撲排序使用。
 */
public class CustomGraph<T> implements IGraph<T> {

    private final Map<T, List<T>> adjacencyList = new HashMap<>();
    private final List<T> nodes = new ArrayList<>();

    @Override
    public void addNode(T node) {
        if (!adjacencyList.containsKey(node)) {
            adjacencyList.put(node, new ArrayList<>());
            nodes.add(node);
        }
    }

    @Override
    public void addEdge(T from, T to) {
        addNode(from);
        addNode(to);
        adjacencyList.get(from).add(to);
    }

    @Override
    public List<T> getNeighbors(T node) {
        List<T> neighbors = adjacencyList.get(node);
        return neighbors == null ? List.of() : new ArrayList<>(neighbors);
    }

    @Override
    public List<T> bfs(T start) {
        List<T> order = new ArrayList<>();
        if (!adjacencyList.containsKey(start)) {
            return order;
        }
        Set<T> visited = new HashSet<>();
        Queue<T> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            order.add(current);
            for (T neighbor : adjacencyList.get(current)) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return order;
    }

    @Override
    public List<T> dfs(T start) {
        List<T> order = new ArrayList<>();
        if (!adjacencyList.containsKey(start)) {
            return order;
        }
        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            T current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            order.add(current);
            List<T> neighbors = adjacencyList.get(current);
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                T neighbor = neighbors.get(i);
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
        return order;
    }

    @Override
    public List<T> topologicalSort() {
        Map<T, Integer> inDegree = new HashMap<>();
        for (T node : nodes) {
            inDegree.put(node, 0);
        }
        for (T node : nodes) {
            for (T neighbor : adjacencyList.get(node)) {
                inDegree.merge(neighbor, 1, Integer::sum);
            }
        }

        Queue<T> queue = new ArrayDeque<>();
        for (T node : nodes) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        List<T> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            T current = queue.poll();
            order.add(current);
            for (T neighbor : adjacencyList.get(current)) {
                int updated = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, updated);
                if (updated == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (order.size() != nodes.size()) {
            throw new IllegalStateException("Graph contains a cycle; topological sort does not exist");
        }
        return order;
    }

    @Override
    public boolean hasCycle() {
        Map<T, Integer> state = new HashMap<>();
        for (T node : nodes) {
            if (state.getOrDefault(node, 0) == 0 && hasCycleFrom(node, state)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleFrom(T node, Map<T, Integer> state) {
        state.put(node, 1);
        for (T neighbor : adjacencyList.get(node)) {
            int neighborState = state.getOrDefault(neighbor, 0);
            if (neighborState == 1) {
                return true;
            }
            if (neighborState == 0 && hasCycleFrom(neighbor, state)) {
                return true;
            }
        }
        state.put(node, 2);
        return false;
    }
}
