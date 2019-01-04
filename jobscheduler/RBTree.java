package job;

import java.util.List;
import java.util.Stack;


public class RBTree<T> {
	private RBTNode<T> root; //root

    private static final boolean RED   = false;
    private static final boolean BLACK = true;

    public RBTree() {
        root=null;
    }

    private RBTNode<T> parentOf(RBTNode<T> node) {
        return node!=null ? node.parent : null;
    }
    private boolean colorOf(RBTNode<T> node) {
       return node!=null ? node.color : BLACK;
    }
    private boolean isRed(RBTNode<T> node) {
        return ((node!=null)&&(node.color==RED)) ? true : false;
    }
    private boolean isBlack(RBTNode<T> node) {
        return !isRed(node);
    }
    private void setBlack(RBTNode<T> node) {
        if (node!=null)
            node.color = BLACK;
    }
    private void setRed(RBTNode<T> node) {
        if (node!=null)
            node.color = RED;
    }
    private void setParent(RBTNode<T> node, RBTNode<T> parent) {
        if (node!=null)
            node.parent = parent;
    }
    private void setColor(RBTNode<T> node, boolean color) {
        if (node!=null)
            node.color = color;
    }
    
    public RBTNode<T> getRoot(){
    	return root;
    }

    public RBTNode<T> getPrevious(Integer i){ //get previous job, inorder
    	RBTNode<T> root = this.root;
    	RBTNode<T> prv = null;
    	if(root == null){
    		return null;
    	}
    	Stack<RBTNode<T>> stack = new Stack<>();
    	while(!stack.isEmpty() || root != null){
    		if(root!=null){
    			stack.push(root);
    			root = root.left;
    		}else{
    			RBTNode<T> node = stack.pop();
    			if(node.getValue()==i){
    				return prv;
    			}
    			prv = node;
    			if(node.right!=null){
    				root = node.right;
    			}
    		}
    	}
    	return null;
    }
    
    public RBTNode<T> getNext(Integer i){ //get next job
    	RBTNode<T> root = this.root;
    	if(root == null){
    		return null;
    	}
    	Stack<RBTNode<T>> stack = new Stack<>();
    	while(!stack.isEmpty() || root != null){
    		if(root!=null) {
    			stack.push(root);
    			root = root.left;
    		} else {
    			RBTNode<T> node = stack.pop();
    			if(node.getValue()==i){
    				if(node.right!=null){
    					return node.right;
    				} else if(!stack.isEmpty()) {
    					return stack.pop();
    				} else {
    					return null;
    				}
    			}
    			if(node.right!=null) {
    				root = node.right;
    			}
    		}
    	}
    	return null;	
    }
    
    public void getRange(Integer i, Integer j, RBTNode<T> root, List<RBTNode<T>> nodes){//printJob(i,j)
    	if(root != null){
    		if(root.getValue() < i){
    		getRange(i, j, root.right, nodes);
    		} else if(root.getValue() > j) {
    			getRange(i, j, root.left, nodes);
    		} else{
    			nodes.add(root);
    			getRange(i, j, root.left, nodes);
    			getRange(i, j, root.right, nodes);
    		}
    	}  		
    }
    
    public RBTNode<T> getNode(Integer jobId){ //input a jobId, return RBTree node
    	RBTNode<T> node = getRoot(); //Search from root
    	if (node==null){//empty tree
    		return null;
    	}
    	else{   		
    		while((int)node.getValue() != (int)jobId){	//node's value != jobId, keep searching
    			if ((int)node.getValue() < (int)jobId){ //node's value < jobId, search right child
                 	node = node.right;
             	} else if((int)node.getValue() > (int)jobId){  //node's value > jobId, search left child
            		node = node.left;
             	} else{
             		return node;
             	}
    			if(node == null){
    				return null;
    			}
    		}
    	}
    	return node; //never reach here
    }      
 
    private void leftRotate(RBTNode<T> node) {
        RBTNode<T> rightchild = node.right;
        node.right = rightchild.left; //node's right child's left child's parent to node
        if (rightchild.left != null)
        	rightchild.left.parent = node;
        rightchild.parent = node.parent; //set node's right child's new parent to node's parent
        if (node.parent == null) {
            this.root = rightchild;   // if node's parent is empty, set node's right child = new root
        } else {
            if (node.parent.left == node)	//if node is its parent's left child
            	node.parent.left = rightchild;   // set node's right child = node's parent's new left child
            else							// if node is its parent's right child
            	node.parent.right = rightchild;    //set node's right child = node's parent's new right child
        }

        rightchild.left = node; //set node to its right child's left child
        node.parent = rightchild;//node's new parent = its previous right child
    }

  
    private void rightRotate(RBTNode<T> node) {    
        RBTNode<T> leftchild = node.left;       
        node.left = leftchild.right;//set node's leftchild to its leftchild's right child
        if (leftchild.right != null) // if node's left child's rightchild is not empty
        	leftchild.right.parent = node; //set node = its leftchild's rightchild's new parent 
        leftchild.parent = node.parent;//set node's parent = node's leftchild's new parent 

        if (node.parent == null) { //if node is the root
            this.root = leftchild;            // set node's leftchild = new root
        } else {
            if (node == node.parent.right)	//if node is its parent's rightchild
            	node.parent.right = leftchild;    // set node's parent's new rightchild = node's leftchild
            else									//if node is its parent's leftchild            	   
            	node.parent.left = leftchild;    // set node's parent's new leftchild = node's leftchild
        }
        leftchild.right = node; //node's leftchild's new rightchild = node
        node.parent = leftchild;//node's new parent = node's leftchild
    }

    public void insert(Integer key, MinHeapNode<Integer> heapNode ) {
        RBTNode<T> node=new RBTNode<T>(key,BLACK,heapNode,null,null,null);//create a new RBTNode
        if (node != null)
            insert(node); //insert
    }
    
    private void insert(RBTNode<T> node) {        
        RBTNode<T> newNodeParent = null; // new inserted node's parent
        RBTNode<T> temp = this.root; //begin search at root
        //find the position to insert node and insert it
        while (temp != null) {
        	newNodeParent = temp;          
            if ((int)node.getValue()<(int)temp.getValue())          	
            	temp = temp.left;
            else
            	temp = temp.right;
        }
        node.parent = newNodeParent;
        if (newNodeParent!=null) {         
            if ((int)node.getValue()<(int)newNodeParent.getValue())
            	newNodeParent.left = node; 
            else
            	newNodeParent.right = node; 
        } 
        else { //empty tree, insert node at root
            this.root = node;
        }
        //set new inserted node's color = red
        node.color = RED;
        //third, fix the tree
        insertFix(node);
    }

    private void insertFix(RBTNode<T> node) {
        RBTNode<T> parent, gparent;       
        while (((parent = parentOf(node))!=null) && isRed(parent)) {//node has parent and parent is red
            gparent = parentOf(parent);         
            if (parent == gparent.left) { //if node's parent is node's grandparent's leftchild
            	//XYr
            	RBTNode<T> uncle = gparent.right;
                if ((uncle!=null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }
              //LRb
                if (parent.right == node) { 
                    RBTNode<T> tmp;
                    leftRotate(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
              //LLb
                setBlack(parent);
                setRed(gparent);
                rightRotate(gparent);
            } 
            else {    //if node's parent is node's grandparent's rightchild
            	//XYr
                RBTNode<T> uncle = gparent.left;
                if ((uncle!=null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }
               //RLb
                if (parent.left == node) {
                    RBTNode<T> tmp;
                    rightRotate(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
               //RRb
                setBlack(parent);
                setRed(gparent);
                leftRotate(gparent);
            }
        }    
        setBlack(this.root); //root must be black
    }   
    
    public void remove(Integer jobId) {
        RBTNode<T> node; 

        if ((node = getNode(jobId)) != null) //jobId is in the tree
            remove(node);
    } 
    
    private void remove(RBTNode<T> node) {
        RBTNode<T> child, parent;
        boolean color;       
        if ( (node.left!=null) && (node.right!=null) ) { //node has both left and rightchild          
            RBTNode<T> y = node.right; //y is replaceNode use to fill deleted node's position          
            //replaceNode = replaceNode.right;
            while (y.left != null)
            	y = y.left; //use deleted node's rightchild's left most sub node as replace node           
            if (parentOf(node)!=null) { //node != root
                if (parentOf(node).left == node)
                    parentOf(node).left = y; //replace node with replaceNode
                else
                    parentOf(node).right = y;
            } 
            else {                
                this.root = y; //node is root
            }          
            child = y.right;
            parent = parentOf(y);       
            color = colorOf(y);
            if (parent == node) { //deleted node is replaceNode's parent
                parent = y;
            } 
            else {
                if (child!=null)//replaceNode's has a rightchild
                    setParent(child, parent);//replaceNode's right child is replaceNode's parent's leftchild
                parent.left = child;
                y.right = node.right; //move deleted node's rightchild to replaceNode's rightchild
                setParent(node.right, y);
            }

            y.parent = node.parent;//replace deleted node with replaceNode
            y.color = node.color;
            y.left = node.left;
            node.left.parent = y;
            if (color == BLACK) //y is a black node,need fix color
                removeFix(child, parent);

            node = null;
            return ;
        }

        if (node.left !=null) {
            child = node.left;
        } 
        else {
            child = node.right;
        }

        parent = node.parent;       
        color = node.color;

        if (child!=null)
            child.parent = parent;       
        if (parent!=null) {
            if (parent.left == node)
                parent.left = child;
            else
                parent.right = child;
        } 
        else {
            this.root = child;
        }

        if (color == BLACK)
            removeFix(child, parent);
        node = null;
    }

       
    private void removeFix(RBTNode<T> y, RBTNode<T> py) {
        RBTNode<T> v;

        while ((y==null || isBlack(y)) && (y != this.root)) {
            if (py.left == y) {
                v = py.right;
                if (isRed(v)) {
                    // Lr	 
                    setBlack(v);
                    setRed(py);
                    leftRotate(py);
                    v = py.right;
                }

                if ((v.left==null || isBlack(v.left)) &&
                    (v.right==null || isBlack(v.right))) {
                    // Lb0 Case 1
                    setRed(v);
                    y = py;
                    py = parentOf(y);
                } 
                else {
                    if (v.right==null || isBlack(v.right)) {
                        // Lb1 Case 1 
                        setBlack(v.left);
                        setRed(v);
                        rightRotate(v);
                        v = py.right;
                    }
                    // Lb1 Case2 & LB2
                    setColor(v, colorOf(py));
                    setBlack(py);
                    setBlack(v.right);
                    leftRotate(py);
                    y = this.root;
                    break;
               }
           } 
            else {
                v = py.left;
                if (isRed(v)) {
                    //Rr
                    setBlack(v);
                    setRed(py);
                    rightRotate(py);
                    v = py.left;
                }
                if ((v.left==null || isBlack(v.left)) &&
                    (v.right==null || isBlack(v.right))) {
                    // Rb0
                    setRed(v);
                    y = py;
                    py = parentOf(y);
                } 
                else {
                    if (v.left==null || isBlack(v.left)) {
                        // Rb1 Case 2 & Rb2
                        setBlack(v.right);
                        setRed(v);
                        leftRotate(v);
                        v = py.left;
                    }
                    // Rb1 Case1
                    setColor(v, colorOf(py));
                    setBlack(py);
                    setBlack(v.left);
                    rightRotate(py);
                    y = this.root;
                    break;
                }
            }
        }

        if (y!=null)
            setBlack(y);
    }
}

class RBTNode<T> {
	public boolean color;        //color
    private Integer jobId;   //jobid
    public RBTNode<T> left;    // leftchild
    public RBTNode<T> right;    // rightchild
    public RBTNode<T> parent;    //parent
    private MinHeapNode<Integer> heapNode;//pointer to minheap
    
    public RBTNode(Integer jobId, boolean color,MinHeapNode<Integer> heapNode, RBTNode<T> parent, RBTNode<T> left, RBTNode<T> right) {
        this.jobId = jobId;
        this.color = color;
        this.parent = parent;
        this.left = left;
        this.right = right;
        this.heapNode = heapNode;
    }
 
    public Integer getValue() {
        return jobId;
    }
    
    public MinHeapNode<Integer> getHeapNode(){
    	return heapNode;
    }
    
    public String toString() {
        return ""+jobId;
    }
}
