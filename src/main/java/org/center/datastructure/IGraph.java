package org.center.datastructure;

import java.util.List;

/**
 * 自訂 Graph 介面（adjacency list）。
 * 用於課程先修關係、學習進程或人員關係。
 */
public interface IGraph<T> {
    void addNode(T node);
    void addEdge(T from, T to);
    List<T> getNeighbors(T node);
    List<T> bfs(T start);
    List<T> dfs(T start);
    List<T> topologicalSort();
    boolean hasCycle();
}
