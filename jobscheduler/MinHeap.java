package job;

import java.util.ArrayList;
import java.util.List;


public class MinHeap<T> {
	private static int currentSize;  //size
	private MinHeapNode<T>[] heap;  //using array to store minheap
	  
	@SuppressWarnings("unchecked")
	public MinHeap() {  
		heap = new MinHeapNode[1]; 
	    }
	
	@SuppressWarnings("unchecked")
	public MinHeap(MinHeapNode<T>[] jobs) {  
		currentSize = jobs.length;    
		heap = new MinHeapNode[currentSize + 1];
		int i = 1;  
		for (MinHeapNode<T> job : jobs)  
			heap[i++] = job;  
	        buildHeap();  
	    }  
	  
	public MinHeapNode<T> getMin(){
	    return heap[1];//first element in array is the min
	}

	public MinHeapNode<T> deleMin() { //delete job when finish or move into another round
		if(!isEmpty()) {  
			MinHeapNode<T> min = heap[1];  //min node
	        heap[1] = heap[currentSize--];  //move bottom node to root
	        heapDown(1);  //move root down
	        return min;  
	    }  
	    return null;  
	}  
	    
	public void insert(MinHeapNode<T> newNode) {  //insert a job
		if (currentSize >= heap.length - 1)  
			increaseArray(heap.length * 2 + 1); //array doubling whenever the current capacity is exceeded
	        int nodeIndex = ++currentSize; 
	        while (nodeIndex > 1 && newNode.getTime() < heap[nodeIndex / 2].getTime()) { //search from bottom to top to find place insert new node
	        	heap[nodeIndex] = heap[nodeIndex / 2];  
	            nodeIndex /= 2;  
	        }  
	        heap[nodeIndex] = newNode;//insert node  
	    }  
	    
	public boolean isEmpty() {  
		return currentSize == 0;  
	}  
	    
	private void heapDown(int position) {  //move node into correct position    	
		MinHeapNode<T> temp = heap[position];  
		int child;  
		for (; position * 2 <= currentSize; position = child) {  
			child = 2 * position;  //get child
			if (child != currentSize && heap[child + 1].getTime() < heap[child].getTime())  
				child++;  
	            if (heap[child].getTime() < temp.getTime())  //child < node
	            	heap[position] = heap[child];  //move child up  
	            else  
	                break;  
	        }  
	        heap[position] = temp;  
	    }  
	  
	private void buildHeap() {  
		for (int i = currentSize / 2; i > 0; i--)  
			heapDown(i);  
	}    
	  
	private void increaseArray(int capacity) { 
		MinHeapNode<T>[] oldArr = heap;  
		MinHeapNode<T>[] newArr = new MinHeapNode[capacity];  
	    for (int i = 1; i < heap.length; i++)  
	    	newArr[i] = oldArr[i];  
	        heap = newArr;  
	    }  
	      
	  
	    public String toString() {  
	        StringBuffer sb = new StringBuffer();  
	        for (int i = 1; i <= currentSize; i++)  
	            sb.append(heap[i] + " ");  
	        return new String(sb);  
	    }
}


class MinHeapNode<AnyType>{
	private int jobId;
	private int totalTime;
	private int excutedTime = 0; 
	public int excutedTimePreRound = 0;//should <= 5
	
	public MinHeapNode(){}
    public MinHeapNode(int id, int time){this.jobId = id; this.totalTime = time;}
    
    int getTime(){ 
    	 return excutedTime;
     }
	
    int getRemainTime(){
    	return totalTime-excutedTime;
    }
	public void setTime(int t){		 
		 this.excutedTime += t;
		 
	 }
	
	
	public List<Integer> getInfo(){//return job information
		List<Integer> list = new ArrayList<Integer>();
		list.add(jobId);
		list.add(excutedTime);
		list.add(totalTime);
		return list;
	}
	
	public void run(){ //increase executed time by 1 ms
		excutedTime ++;
		excutedTimePreRound ++ ;
	}
}
