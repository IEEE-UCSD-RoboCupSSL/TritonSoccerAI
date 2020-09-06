package Triton.Computation.AStar;
import Triton.Shape.*;

public class MinHeap { 
    private Node[] Heap; 
    private int size; 
    private int maxsize; 
  
    private static final int FRONT = 1; 
  
    public MinHeap(int maxsize) 
    { 
        this.maxsize = maxsize; 
        this.size = 0; 
        Heap = new Node[this.maxsize + 1]; 
        Heap[0] = new Node(new Vec2D(0, 0), -1, -1); 
    } 
  
    // Function to return the position of 
    // the parent for the node currently 
    // at pos 
    private int parent(int pos) 
    { 
        return pos / 2; 
    } 
  
    // Function to return the position of the 
    // left child for the node currently at pos 
    private int leftChild(int pos) 
    { 
        return (2 * pos); 
    } 
  
    // Function to return the position of 
    // the right child for the node currently 
    // at pos 
    private int rightChild(int pos) 
    { 
        return (2 * pos) + 1; 
    } 
  
    // Function that returns true if the passed 
    // node is a leaf node 
    private boolean isLeaf(int pos) 
    { 
        if (pos >= (size / 2) && pos <= size) { 
            return true; 
        } 
        return false; 
    } 
  
    // Function to swap two nodes of the heap 
    private void swap(int fpos, int spos) 
    { 
        Node tmp; 
        tmp = Heap[fpos]; 
        Heap[fpos] = Heap[spos]; 
        Heap[spos] = tmp; 
    } 
  
    // Function to heapify the node at pos 
    private void minHeapify(int pos) 
    { 
  
        // If the node is a non-leaf node and greater 
        // than any of its child 
        if (!isLeaf(pos)) { 
            if (Heap[pos].getHCost() > Heap[leftChild(pos)].getHCost() 
                || Heap[pos].getHCost() > Heap[rightChild(pos)].getHCost()) { 
  
                // Swap with the left child and heapify 
                // the left child 
                if (Heap[leftChild(pos)].getHCost() < Heap[rightChild(pos)].getHCost()) { 
                    swap(pos, leftChild(pos)); 
                    minHeapify(leftChild(pos)); 
                } 
  
                // Swap with the right child and heapify 
                // the right child 
                else { 
                    swap(pos, rightChild(pos)); 
                    minHeapify(rightChild(pos)); 
                } 
            } 
        } 
    } 
  
    // Function to insert a node into the heap 
    public void insert(Node element) 
    { 
        if (size >= maxsize) { 
            return; 
        } 
        Heap[++size] = element; 
        int current = size; 
  
        while (Heap[current].getHCost() < Heap[parent(current)].getHCost()) { 
            swap(current, parent(current)); 
            current = parent(current); 
        } 
    } 
  
    // Function to print the contents of the heap 
    // public void print() 
    // { 
    //     for (int i = 1; i <= size / 2; i++) { // Need toString() for Node class
    //         System.out.print(" PARENT : " + Heap[i] 
    //                          + " LEFT CHILD : " + Heap[2 * i] 
    //                          + " RIGHT CHILD :" + Heap[2 * i + 1]); 
    //         System.out.println(); 
    //     } 
    // } 
  
    // Function to build the min heap using 
    // the minHeapify 
    public void minHeap() 
    { 
        for (int pos = (size / 2); pos >= 1; pos--) { 
            minHeapify(pos); 
        } 
    } 
  
    // Function to remove and return the minimum 
    // element from the heap 
    public Node remove() 
    { 
        Node popped = Heap[FRONT]; 
        Heap[FRONT] = Heap[size--]; 
        minHeapify(FRONT); 
        return popped; 
    } 
} 