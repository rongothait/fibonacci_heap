# Fibonacci Heap Implementation

An efficient Java implementation of a **Fibonacci Heap** data structure, built as part of a university-level data structures course. This heap supports key operations such as insert, findMin, deleteMin, meld, decreaseKey, and delete, with proven amortized time complexities.

## Special Thank You's

special thanks to Sean on collaborating with me on this project

## Features

- **Efficient Operations**:
  - `insert` – O(1) amortized
  - `findMin` – O(1)
  - `deleteMin` – O(log n) amortized
  - `meld` – O(1)
  - `decreaseKey` – O(1) amortized
  - `delete` – O(log n) amortized

- **Node Structure**:
  - Each node contains key, info, child/parent/sibling references, rank, and mark.

- **Heap Structure**:
  - Maintains fields such as min pointer, total size, and counters for cuts, links, and trees.

## File Structure

- `FibonacciHeap.java` – Contains both `HeapNode` and `FibonacciHeap` classes with all heap operations and utilities.

## How to Use

```java
FibonacciHeap heap = new FibonacciHeap();
heap.insert(10, "a");
heap.insert(2, "b");
heap.insert(15, "c");

HeapNode min = heap.findMin();
System.out.println("Min key: " + min.getKey());

heap.deleteMin();
