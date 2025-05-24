/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	public HeapNode min;
	public int size;
	public int numOfCuts;
	public int numOfLinks;
	public int numOfTrees;
	
	/**
	 *
	 * Constructor to initialize an empty heap.
	 *
	 */
	public FibonacciHeap()
	{
		this(null, 0, 0);
	}

	/**
	 * Constructor overload
	 */
	public FibonacciHeap(HeapNode min_node){
		this(min_node, 1, 1);
		min_node.next = min_node;
		min_node.prev = min_node;
	}

	/*
	 * Constructor overload
	 */
	public FibonacciHeap(HeapNode min_node, int size, int numOfTrees){
		this.min = min_node;
		this.size = size;
		this.numOfCuts = 0;
		this.numOfLinks = 0;
		this.numOfTrees = numOfTrees;
	}

	/**
	 * 
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public HeapNode insert(int key, String info) 
	{   
		// create a node with this info
		HeapNode new_node = new HeapNode(key, info);
		FibonacciHeap new_heap = new FibonacciHeap(new_node);
		
		// meld the heap with the new one node heap
		this.meldCall(new_heap, true);
		
		// return the newly generated HeapNode
		return new_node;
	}

	/**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 *
	 */
	public HeapNode findMin()
	{
		return this.min;
	}

	/**
	 * 
	 * Delete the minimal item
	 * WorstCase complexity: O(n)
	 * Amortized Complexity: O(log n)
	 *
	 */
	public void deleteMin()
	{
		boolean size_decreased = false;

		//Edge Case - heap is empty
		if (this.isEmpty())
			return; // nothing to do

		//Edge case - heap is one node
		if (this.size == 1){
			this.size = 0;
			this.min = null;
			this.numOfTrees = 0;
			return;
		}

		// Disconnect the min's children from their parent (their parent should be null now) + add to totalCuts
		set_childrens_parentField_null(this.min);

		// Create a new heap with the min's chidren
		FibonacciHeap new_heap = new FibonacciHeap();
		new_heap.min = this.min.child;
		new_heap.numOfTrees = this.min.rank;
		if (numOfTrees == 1){
			new_heap.size = this.size - 1;
			size_decreased = true;
			new_heap.numOfCuts = this.numOfCuts;
			new_heap.numOfLinks = this.numOfLinks;
		}
		else
			new_heap.size = 0; // doesn't really matter for meld_call

		//Edge Case - num of trees is 1
		if (this.numOfTrees == 1)
			this.min = null;
		else
			// skip the min pointer in the root's list
			skip_node_in_root_list(this, this.min, true);
		
		// meld heap with new heap
		this.meldCall(new_heap, false);

		// start consoliidating / successive linking
		consolidating_successive_linking();
		
		// Finally, decrease the tree size by 1 if not already decreased
		if (!size_decreased)
			this.size--;
	}

	/*
	 * Does consolidating / successive linking on the heap
	 * WorstCase complexity: O(T0 + logn)
	 * Amortized Complexity: O(log n)
	 */
	public void consolidating_successive_linking(){
		if (this.min == null)
			return;
		
		int n = this.size;
		double log_value = Math.log(n + 1) / Math.log(2);
		HeapNode[] bucket_arr = new HeapNode[(int) Math.ceil(log_value)];
		HeapNode curr_root = this.min;
		int times_to_iterate = this.numOfTrees;

		for (int i = 0; i < times_to_iterate; i++){
			HeapNode next_root = curr_root.next;  // keep a pointer to the next root (for cases when the pointers shift)
			HeapNode curr_inner = curr_root;  

			while (bucket_arr[curr_inner.rank] != null){
				HeapNode other = bucket_arr[curr_inner.rank];
				bucket_arr[curr_inner.rank] = null; // empty the bucket
				this.link_roots(curr_inner, other);
				
				if (curr_inner.parent != null) // always get the root of the linked tree
					curr_inner = curr_inner.parent; 
			}
			bucket_arr[curr_inner.rank] = curr_inner;
			if (curr_inner.key < this.min.key)
				this.min = curr_inner;
			curr_root = next_root;
		}
	}

	/*
	 * pre : numOfTrees > 1
	 * given a node it skips it in the root list.
	 * sets num_of_trees--
	 * if handleMinArbitrary, sets the heap's min pointer to some arbitrary root
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public void skip_node_in_root_list(FibonacciHeap heap, HeapNode node, boolean handleMinArbitrary){
		HeapNode old_min = heap.findMin();
		boolean isMin = false;
		if (old_min == node){
			isMin = true;
		}

		HeapNode node_next = node.next;
		HeapNode node_prev = node.prev;
		node_prev.next = node_next;
		node_next.prev = node_prev;
		heap.numOfTrees--;

		if (handleMinArbitrary && isMin)
		{
			heap.min = node_next;
		}
	}
	
	/*
	 * given a parent node, the function sets the children node's "parent" field to none
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public void set_childrens_parentField_null(HeapNode parent){
		int parent_rank = parent.rank;
		HeapNode curr_child = parent.child;
		for(int i = 0; i < parent_rank; i++){
			curr_child.parent = null;
			curr_child = curr_child.next;
		}
		
		this.numOfCuts += parent_rank;
	}

	/*
		* pre: this, other are roots to their trees
		* pre: this.rank = other.rank
		* 
		* links this node and the other root to one trees
		* WorstCase complexity: O(1)
	 	* Amortized Complexity: O(1)
		*/
	public void link_roots(HeapNode n1, HeapNode n2){
		HeapNode x = n1;
		HeapNode y = n2;
		// want to create two nodes such that x.key <= y.key. if not true, swap them.
		// also - if y is the minimum of heap - we want to keep it a root
		// if not - switch them
		if (n2.key < n1.key || (n1.key == n2.key && this.min == y)){
			x = n2;
			y = n1;
		}

		// remove y from the root list
		HeapNode Orig_next_y = y.next;
		HeapNode orig_prev_y = y.prev;
		orig_prev_y.next = Orig_next_y;
		Orig_next_y.prev = orig_prev_y;

		// Edge case - their ranks are zero
		if (x.rank == 0){
			y.next = y;
			y.prev = y;
		}

		else{ // their ranks >= 1
			y.next = x.child;
			y.prev = x.child.prev;
			x.child.prev.next = y;
			x.child.prev = y;
		}

		x.child = y;
		y.parent = x;
		x.rank++;

		// update the number of links for the heap
		this.numOfLinks++;

		// decrease number of trees in the heap
		this.numOfTrees--;
	}
	

	/**
	 * 
	 * pre: 0<diff<x.key
	 * 
	 * Decrease the key of x by diff and fix the heap. 
	 * WorstCase complexity: O(c)
	 * Amortized Complexity: O(1)
	 * 
	 */
	public void decreaseKey(HeapNode x, int diff) 
	{   
		if(x == null){
			return;
		}
		
		int newKey = x.key - diff;

		x.key = newKey;

		if(x.parent == null){ //case 1: x is a root
			if(this.min.key > newKey){
				this.min = x;
			}
			return;
		}

		if(newKey > x.parent.key){ //case 2: the decreased key is still larger than parent
			return;
		}
		
		HeapNode parent = x.parent;
		
		//case 3: the decreased key is smaller than parent
		this.cutNode(x);
	
		if(parent.mark == false){ //case 3.1: parent is unmarked
			parent.mark = true;
			return;
		}
		else{ //case 3.2: parent is marked
			parent.mark = false;
			this.cascadingCuts(parent);
		}
	}

	/*
	 * Receives a node that just had a child cut from it, keeps cutting node until reaching an unmarked node.
	 * WorstCase complexity: O(c)
	 * Amortized Complexity: O(1)
	 */
	public void cascadingCuts(HeapNode x){
		HeapNode y = x.parent;
		this.cutNode(x);
		if(y == null){
			return;
		}

		if(y.parent != null){
			if(y.mark == false)
				y.mark = true;
			else
				cascadingCuts(y);
		}
	}


	/*
	 * cut node from its parent
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public void cutNode(HeapNode x){
		if(x.parent == null){
			return;
		}

		HeapNode y = x.parent;

		//cut x from parent
		x.parent = null;
		this.numOfCuts ++;
		x.mark = false;
		y.rank = y.rank - 1;

		if(x.next == x){ // if x is the only child of y
			y.child = null;
		}
		else{ //x has brothers
			y.child = x.next;
			x.prev.next = x.next;
			x.next.prev = x.prev;

			x.next = x;
			x.prev = x;
		}

		//insert x and children back into the heap
		FibonacciHeap xHeap = new FibonacciHeap();
		xHeap.min = x;
		xHeap.numOfTrees = 1;
		this.meld(xHeap);


	}
	/**
	 * 
	 * Delete the x from the heap.
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public void delete(HeapNode x) 
	{    
		HeapNode oldMin = this.min;
		if(x == min){ // if x is the minimum
			this.deleteMin();
			return;
		}

		else if(x.parent != null){ // x is not a root - so we turn it into a root
			this.decreaseKey(x, x.key);
		}

		if(x.child == null){ // x has no children
			this.skip_node_in_root_list(this, x, false);

			this.min = oldMin;
			this.size--;
		}

		else{ // x has children
			this.set_childrens_parentField_null(x);

			FibonacciHeap childrensHeap = new FibonacciHeap();
			childrensHeap.min = x.child;
			childrensHeap.numOfTrees = x.rank;
			
			this.meld(childrensHeap);
			
			this.skip_node_in_root_list(this, x, false);

			this.min = oldMin;
			this.size--;
		}
	}



	/**
	 * 
	 * Return the total number of links.
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public int totalLinks()
	{
		return this.numOfLinks;
	}


	/**
	 * 
	 * Return the total number of cuts.
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public int totalCuts()
	{
		return this.numOfCuts;
	}


	/**
	 * 
	 * Meld the heap with heap2
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public void meld(FibonacciHeap heap2)
	{
		meldCall(heap2, true);
	}

	/*
	 * Does actual melding
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public void meldCall(FibonacciHeap heap2, Boolean sumLinksCuts){
		if(heap2 == null || heap2.isEmpty()){
			return;
		}
		
		if(this.isEmpty() && heap2.isEmpty()){
			return;
		}

		if(this.isEmpty()){
			this.min = heap2.min;
			this.numOfCuts = heap2.numOfCuts;
			this.numOfLinks = heap2.numOfLinks;
			this.size = heap2.size;
			this.numOfTrees = heap2.numOfTrees;
			return;
		}

		HeapNode first1 = this.min; //pointer to first
		HeapNode first2 = heap2.min;
		HeapNode last1 = this.min.prev;
		HeapNode last2 = heap2.min.prev;

		//connect roots
		last1.next = first2;
		first2.prev = last1;

		last2.next = first1;
		first1.prev = last2;

		//set new minimum
		if(this.min.key >= heap2.min.key){
			this.min = heap2.min;
		}
		
		this.numOfTrees = this.numOfTrees + heap2.numOfTrees;

		//meld all other fields - if outer heap
		if(sumLinksCuts){
			this.numOfCuts = this.numOfCuts + heap2.numOfCuts;
			this.numOfLinks = this.numOfLinks + heap2.numOfLinks;
			this.size = this.size + heap2.size;
		}
	}

	/**
	 * 
	 * Return the number of elements in the heap
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public int size()
	{
		return this.size;
	}


	/**
	 * 
	 * Return the number of trees in the heap.
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public int numTrees()
	{
		return this.numOfTrees;
	}


	/*
	 * Returns wether the heap contains no nodes
	 * WorstCase complexity: O(1)
	 * Amortized Complexity: O(1)
	 */
	public boolean isEmpty()
	{
		if(this.min == null){
			return true;
		}

		return false;
	}

	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *  
	 */
	public static class HeapNode{
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank;
		public boolean mark;

		public HeapNode(int key, String info){
			this.key = key;
			this.info = info;

			// default the other parametrs
			this.child = null;
			this.next = null;
			this.prev = null;
			this.next = null;
			this.rank = 0;
			this.mark = false;
		}

		/**
		 * Return wether a node is a root or not
		 * WorstCase complexity: O(1)
	 	 * Amortized Complexity: O(1)
		 */
		public boolean is_root(){
			return this.parent != null;
		}

	}
}
