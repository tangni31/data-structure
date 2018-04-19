import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class jobscheduler {
	public static List<String> inputs = new ArrayList<String>(); //a list saves all inputs
	public static RBTree<Integer> RBTree = new  RBTree<Integer>();//initialize RBTree
	static boolean getNewJob = true;
	static int idleTime = 0;//total time that no job running
	@SuppressWarnings("rawtypes")
	static MinHeapNode[] arr = new MinHeapNode[] {}; 
	static MinHeap<Integer> minHeapRound0 = new MinHeap<Integer>(arr); //initialize min-heap   
	static MinHeap<Integer> minHeapRound1 = new MinHeap<Integer>(arr);//there are 2 rounds, one job finished 5ms will send to another round
        static int currentRound = 0;	//current is 0 or 1
        public static File outputFile;	//out_file.txt
        public static BufferedWriter output;
        static MinHeapNode<Integer> currentRunNode = null;
        static String crlf=System.getProperty("line.separator");//make sure line separator works correct in both Linux and windows
        static int totalJobTime = 0; //total time need to finished all jobs.
        public static List<Integer> insertIdList = new ArrayList<Integer>();
        public static List<Integer> insertTimeList = new ArrayList<Integer>();
    
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub  
		String filename = String.valueOf(args[0]); //get input file name
		inputs = readInputFile(filename); //read input file, cache all input commands		
	    	outputFile = new File( "output_file.txt" );//create output_file.txt
		outputFile.createNewFile();
		output = new BufferedWriter(new FileWriter(outputFile));	    
		String lastLine = inputs.get(inputs.size()-1);//read last line in input file
		int largestTimeStamp = getTimeStamp(lastLine);//get largest time stamp
		int clockTime = largestTimeStamp;
		for (int clock=0; clock<=clockTime; clock++){
			if (!inputs.isEmpty()){
				String line = inputs.get(0); //get first command
				int time = getTimeStamp(line); //get first command's time stamp
				if (clock == time){	//read the input command only when the global time matches the time in the input command
					doInputCommand(line);
					inputs.remove(0);//after finished processing command, delete it from inputs			
				}					
			}
			insertJob();
			if (RBTree.getRoot() != null){ //has some jobs need to run
				runJobs(); //run job for 1ms						
			}
			else{
				idleTime ++;//Tree is empty, no job can be run, idle time +1
			}		
			if (clock == largestTimeStamp && totalJobTime-largestTimeStamp>0)
			{	//finish all command in input_file, need more time to finish executing remain jobs
				clockTime = totalJobTime+idleTime;//time need to finish executing remain jobs				
			}
		}
	}
	

	public static int getTimeStamp(String line){//get time stamp by using regular expression
		String pattern = "(\\d+)";
	    Pattern r = Pattern.compile(pattern); 
	    Matcher m = r.matcher(line);
	    m.find();
	    int timeStamp  = Integer.parseInt(m.group(0));
		return timeStamp;
	}		
		
	public static List<String> readInputFile (String filename) throws IOException{//read and cache all commands in a list
		InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
        BufferedReader f = new BufferedReader(reader);  
        String line = "";   
        while (true){  //line != null
            line = f.readLine();
            if (line == null){
            	break;
            }
            inputs.add(line);
        } 
        f.close();
		return inputs;
	}
	
	public static void insertJob(){ //when a job is running, can't insert new jobs into data structures, need to wait
		if (!insertIdList.isEmpty()){ //has some jobs need to be inserted
			if (currentRunNode==null || currentRunNode.excutedTimePreRound==0 || currentRunNode.excutedTimePreRound==5 ){//no job is running, insert			 		   
				
					int id = insertIdList.get(0);
					int time = insertTimeList.get(0);
				
					MinHeapNode<Integer> node = new MinHeapNode<Integer>(id,time); //(jobId, time)
					RBTree.insert(id,node);
					if (currentRound == 0){ //insert jobs into current round
						minHeapRound0.insert(node); 
					}
					else{
						minHeapRound1.insert(node);
					}
					insertIdList.remove(0);
					insertTimeList.remove(0);
				
			}
		}
	}
	
	public static void runJobs(){
		
		MinHeap<Integer> currentRoundHeap;
		MinHeap<Integer> otherRoundHeap;
		if (currentRound == 0){
			if (minHeapRound0.isEmpty()){ //no jobs in round0, change to round1
				currentRound = 1;	
			}
		}
		if (currentRound == 1){
				if (minHeapRound1.isEmpty()){ //no jobs in round0, change to round1
					currentRound = 0;
				}
		}
			
		if (currentRound == 0){
			currentRoundHeap = minHeapRound0;
			otherRoundHeap = minHeapRound1;
		}
		else{
			currentRoundHeap = minHeapRound1;
			otherRoundHeap = minHeapRound0;
		}
		
		if (getNewJob){ //can switch only after a job run 5ms or previous job finished execution or current running job =null			
			currentRunNode = currentRoundHeap.getMin();			
			getNewJob = false;			
		}	
		
		currentRunNode.run(); //job runs for 1 ms	
		
		//int remainTime = currentRunNode.getRemainTime();
		if (currentRunNode.getRemainTime() <= 0){	//job finished 
			RBTree.remove((int)(currentRunNode.getInfo().get(0))); //remove from RBT			
			currentRoundHeap.deleMin();//remove from minHeap			
			getNewJob = true;
			currentRunNode = null;
			
		}
		else if (currentRunNode.excutedTimePreRound >= 5){ //executed 5 ms, move to another round		
			currentRoundHeap.deleMin();
			currentRunNode.excutedTimePreRound = 0;
			otherRoundHeap.insert(currentRunNode);
			getNewJob = true;
		}			
	}
	
	
	public static void doInputCommand (String line) throws IOException{ //insert a job when global time is equal to arrival time
		int jobId = 0;
		int parameter1 = 0; //parameter1 & 2 represent 2 numbers in command  eg. insert(5,25)
		int parameter2 = 0;
		
		if (line.contains("Insert") || (line.contains(",") && line.contains("Print"))  ){ //only InsertJob(x,y) & PrintJob(x,y) have 2 parameters
	    	 String pattern = "(\\(\\d+)(,\\d+)"; //using regular expression to get 2 parameters
	    	 Pattern r = Pattern.compile(pattern);
	    	 Matcher m = r.matcher(line);
	    	 m.find();
	    	 String num1 = m.group(1);	    	
	    	 parameter1 = Integer.parseInt(num1.replace("(",""));
	    	 String num2 = m.group(2);
	    	 parameter2 = Integer.parseInt(num2.replace(",",""));
	     }
		
		else { 				//NextJob(x),PreviousJob(x),PrintJob(x) have one parameter.
	    	     String pattern = "(\\(\\d+)";
		     Pattern r = Pattern.compile(pattern);		      
		     Matcher m = r.matcher(line);
		     m.find();
		     String num1 = m.group(1);
		     jobId = Integer.parseInt(num1.replace("(",""));     
	     }
	     
		if (line.contains("Insert")){  		 //InsertJob(x,y)
			totalJobTime += parameter2; //add time to totalJobTime
			insertIdList.add(parameter1);//add insert job to waiting list
			insertTimeList.add(parameter2);
		}
		else if (line.contains("Print") && line.contains(",")){ 	//PrintJob(x,y)
			List<RBTNode<Integer>> nodeList = new ArrayList<RBTNode<Integer>>();
			nodeList = RBTree.getRange(parameter1, parameter2, RBTree.getRoot());//getRange return a list contains all jobs in the range
			if(nodeList.size()==0){		//no such jobs, print (0,0,0)
				output.write("(0,0,0)"+crlf);
				output.flush();
				//System.out.println("(0,0,0)");
			}
			else{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<nodeList.size(); i++)    {   
					RBTNode<Integer> job = nodeList.get(i);
					List<Integer> info = job.getHeapNode().getInfo();
					String lines = "(" + info.get(0) + "," +  info.get(1) + "," +  info.get(2) + ")" + ",";
					sb.append(lines);
				}
				sb.deleteCharAt(sb.length()-1); //delete last ";"
				output.write(sb.toString()+crlf);
				output.flush();
				//System.out.println(sb);
			}
		}
		else if (line.contains("Previous") || line.contains("Next")){ //PreviousJob(x) & NextJob(x) 
			RBTNode<Integer> Job;
			if (line.contains("Previous")){
				Job = RBTree.getPrevious(jobId);
			}
			else{
				Job = RBTree.getNext(jobId);
			}
			if (Job==null){
				//System.out.println("(0,0,0)"); //no such jobs
				output.write("(0,0,0)"+crlf);
				output.flush();
			}
			else{
				List<Integer> info = Job.getHeapNode().getInfo(); //get job's information and print
				String line1 = "(" + info.get(0) + "," +  info.get(1) + "," +  info.get(2) + ")";
				//System.out.println(line1);
				output.write(line1+crlf);
				output.flush();
			}
		}
		else{ //PrintJob(x)
			RBTNode<Integer> Job = RBTree.getNode(jobId);
			if (Job==null){
				output.write("(0,0,0)"+crlf);
				output.flush();
				//System.out.println("(0,0,0)");
			}
			else{
				List<Integer> info = Job.getHeapNode().getInfo();
				String line2 = "(" + info.get(0) + "," +  info.get(1) + "," +  info.get(2) + ")";
				output.write(line2+crlf);
				output.flush();
				//System.out.println(line2);
			}
		}
		
	}		
}

class RBTNode<AnyType> {
    public boolean color;        //color
    private Integer jobId;   //jobid
    public RBTNode<AnyType> left;    // leftchild
    public RBTNode<AnyType> right;    // rightchild
    public RBTNode<AnyType> parent;    //parent
    private MinHeapNode<Integer> heapNode;
    
    public RBTNode(Integer jobId, boolean color,MinHeapNode<Integer> heapNode, RBTNode<AnyType> parent, RBTNode<AnyType> left, RBTNode<AnyType> right) {
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

class RBTree<AnyType> {

    private RBTNode<AnyType> root; //root

    private static final boolean RED   = false;
    private static final boolean BLACK = true;

    public RBTree() {
        root=null;
    }

    private RBTNode<AnyType> parentOf(RBTNode<AnyType> node) {
        return node!=null ? node.parent : null;
    }
    private boolean colorOf(RBTNode<AnyType> node) {
       return node!=null ? node.color : BLACK;
    }
    private boolean isRed(RBTNode<AnyType> node) {
        return ((node!=null)&&(node.color==RED)) ? true : false;
    }
    private boolean isBlack(RBTNode<AnyType> node) {
        return !isRed(node);
    }
    private void setBlack(RBTNode<AnyType> node) {
        if (node!=null)
            node.color = BLACK;
    }
    private void setRed(RBTNode<AnyType> node) {
        if (node!=null)
            node.color = RED;
    }
    private void setParent(RBTNode<AnyType> node, RBTNode<AnyType> parent) {
        if (node!=null)
            node.parent = parent;
    }
    private void setColor(RBTNode<AnyType> node, boolean color) {
        if (node!=null)
            node.color = color;
    }
    
    public RBTNode<AnyType> getRoot(){
    	return root;
    }

   
    public RBTNode<AnyType> getPrevious(Integer i){ //get previous job
    	RBTNode<AnyType> node = getRoot(); //search from root
    	RBTNode<AnyType> temp;
    	if(node != null){
    		while(true){
    			temp = node;
    			if(node != null){
    				if((int)node.getValue() < (int)i){	//node's value < jobId, search right child
    					node = node.right;
    				}
    			}
    			if(node != null){
    				temp = node;
    			}
    			if (node != null){ 
    				if((int)node.getValue() >= (int)i ){ //node's value >= jobId, search left child
    					node = node.left;
    				}
    			}
    			if (node == null){	//reach the bottom of the tree
    				if((int)temp.getValue() < (int) i ){//if this node's value<index, this is the previousJob
           			 	return temp;
           			 	}
    				else{	//if this node's value>=index, try its parent
           			 	node = temp.parent;	
           			 	if (node == null){
           			 		return null;
           			 	}
           			 	while ((int)node.getValue() >= (int) i){ //keep searching, until find a parent < i
           			 		node = node.parent;
           			 		if (node == null){
           			 			return null; // i is the smallest node, return null
           			 		}	
           			 	}
           			 	return node;
    				}        	
    			}
    		}
    	
    	}
    	else{
    		return null;//empty tree
    	}   	
    }
    
    public RBTNode<AnyType> getNext(Integer i){ //get next job
    	RBTNode<AnyType> node = getRoot();
    	RBTNode<AnyType> temp;
    	if (node != null){
    		while(true){
    			temp = node;
    			if(node != null){
    				if((int)node.getValue() <= (int)i){ //node's value <= jobId, search right child
    					node = node.right;    	
    				}
    			}
    			if(node != null){
    				temp = node;
    			}
    			if (node != null){ 
    				if((int)node.getValue() > (int)i ){	//node's value > jobId, search left child
    					node = node.left;          	
    				}
    			}  
    			if (node == null){	//reach the bottom of the tree, temp is the bottom node
    				if((int)temp.getValue() > (int) i ){ //if this node's value>index, this is the nextJob
           			 	return temp;
           			 }
    				else{ //if this node's value<=index, try its parent
           			 	node = temp.parent;	
           			 	if (node == null){ //no parent
           			 		return null;
           			 	}
           			 	while ((int)node.getValue() <= (int) i){ //keep searching, until find a parent >i
           			 		node = node.parent;
           			 		if (node == null){
           			 			return null; // i is the largest node, return null
           			 		}	
           			 	}
           			 	return node;
    				}        	
    			}
    		} 
    	}
    	else{
    		return null;
    	} 	
    	
    }
    
    public  List<RBTNode<AnyType>>  getRange(Integer i, Integer j, RBTNode<AnyType> node){	//printJob(i,j)
    	List<RBTNode<AnyType>> nodeList = new ArrayList<RBTNode<AnyType>>(); //use a list to store all the nodes in range(i,j)
    	List<RBTNode<AnyType>>listTemp = new ArrayList<RBTNode<AnyType>>();
    	RBTNode<AnyType> nodeSmall = null; //left child
    	RBTNode<AnyType> nodeLarge = null; //right child
    	//if node < i, try its right child:
    	if (node != null){ //make sure node is not empty   		
    		if ((int)node.getValue()< (int)i){//			
    			nodeLarge = node.right;//right child
    			if (nodeLarge != null){
    				if ((int)nodeLarge.getValue()<= (int)j && (int)nodeLarge.getValue() >= (int)i && nodeList.contains(nodeLarge)==false){
        					nodeList.add(nodeLarge);
        				}
    				listTemp = getRange(i, j, nodeLarge);
    				nodeList.removeAll(listTemp); //make sure not replicated nodes in list
    				nodeList.addAll(listTemp);
    				}
    		}
    		//if node > j, try its left child:
    		if ((int)node.getValue() > (int)j){  
    			nodeSmall = node.left; //left child
    			if (nodeSmall != null){
    				if ((int)nodeSmall.getValue()<= (int)j && (int)nodeSmall.getValue() >= (int)i && nodeList.contains(nodeSmall)==false){
        					nodeList.add(nodeSmall);
    				}
    				listTemp = getRange(i, j, nodeSmall);//search its child
    				nodeList.removeAll(listTemp);
    				nodeList.addAll(listTemp);
    			}
    		}  	
    		//if i<node< j, search both left & right child
    		if ((int)node.getValue()<= (int)j && (int)node.getValue() >= (int)i ){ 
    			if (nodeList.contains(node)==false){ //node not in the list
    				nodeList.add(node); // i<node< j, in range, save it into list
    			}
    			nodeSmall = node.left;//search left child
    			if (nodeSmall != null){
    				if ((int)nodeSmall.getValue()<= (int)j && (int)nodeSmall.getValue() >= (int)i ){ //in range
    					if (nodeList.contains(nodeSmall)==false){
    						nodeList.add(nodeSmall);	//add into list
    					}
    				}
    					listTemp = getRange(i, j, nodeSmall); //search its child
    					nodeList.removeAll(listTemp);
    					nodeList.addAll(listTemp);	
    				
    			}
    			nodeLarge = node.right;//search right child
    			if (nodeLarge != null){
    				if ((int)nodeLarge.getValue()<= (int)j && (int)nodeLarge.getValue() >= (int)i ){
    					if (nodeList.contains(nodeLarge)==false){
    						nodeList.add(nodeLarge);
    					}
    				}
    					listTemp = getRange(i, j, nodeLarge);//search its child
    					nodeList.removeAll(listTemp);
    					nodeList.addAll(listTemp);   				
    			}
    		}
	 }	
    	return 	nodeList;   		
  }
    
    public RBTNode<AnyType> getNode(Integer jobId){ //input a jobId, return RBTree node
    	RBTNode<AnyType> node = getRoot(); //Search from root
    	if (node==null){	//empty tree
    		return null;
    	}
    	else{   		
    		while((int)node.getValue() != (int)jobId){	//node's value != jobId, keep searching
             		if ((int)node.getValue() < (int)jobId){ //node's value < jobId, search right child
                 		node = node.right;
				if (node==null){//no such job
					return null;
				}
             		}
             	else if((int)node.getValue() > (int)jobId){  //node's value > jobId, search left child
            		 	node = node.left;
            		 	if (node==null){//no such job
            		 		return null;
            		 	}
             	}
    		}
    	}
    	return node;
    	
    }      
 
    private void leftRotate(RBTNode<AnyType> node) {
        RBTNode<AnyType> rightchild = node.right;
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

  
    private void rightRotate(RBTNode<AnyType> node) {    
        RBTNode<AnyType> leftchild = node.left;       
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
        RBTNode<AnyType> node=new RBTNode<AnyType>(key,BLACK,heapNode,null,null,null);//create a new RBTNode
        if (node != null)
            insert(node); //insert
    }
    
    private void insert(RBTNode<AnyType> node) {        
        RBTNode<AnyType> newNodeParent = null; // new inserted node's parent
        RBTNode<AnyType> temp = this.root; //begin search at root
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

    private void insertFix(RBTNode<AnyType> node) {
        RBTNode<AnyType> parent, gparent;       
        while (((parent = parentOf(node))!=null) && isRed(parent)) {//node has parent and parent is red
            gparent = parentOf(parent);         
            if (parent == gparent.left) { //if node's parent is node's grandparent's leftchild
            	//XYr
            	RBTNode<AnyType> uncle = gparent.right;
                if ((uncle!=null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }
              //LRb
                if (parent.right == node) { 
                    RBTNode<AnyType> tmp;
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
                RBTNode<AnyType> uncle = gparent.left;
                if ((uncle!=null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }
               //RLb
                if (parent.left == node) {
                    RBTNode<AnyType> tmp;
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
        RBTNode<AnyType> node; 

        if ((node = getNode(jobId)) != null) //jobId is in the tree
            remove(node);
    } 
    
    private void remove(RBTNode<AnyType> node) {
        RBTNode<AnyType> child, parent;
        boolean color;       
        if ( (node.left!=null) && (node.right!=null) ) { //node has both left and rightchild          
            RBTNode<AnyType> y = node.right; //y is replaceNode use to fill deleted node's position          
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

       
    private void removeFix(RBTNode<AnyType> y, RBTNode<AnyType> py) {
        RBTNode<AnyType> v;

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


class MinHeap<AnyType> {  
    private static int currentSize;  //size
    @SuppressWarnings("rawtypes")
	private static MinHeapNode[] heap;  //using array to store minheap
  
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public MinHeap(MinHeapNode[] jobs) {  
        currentSize = jobs.length;    
        heap =  new MinHeapNode[currentSize + 1];
        int i = 1;  
        for (MinHeapNode<AnyType> job : jobs)  
        	heap[i++] = job;  
        buildHeap();  
    }  
  
    @SuppressWarnings("unchecked")
	public MinHeapNode<AnyType> getMin(){
    	return heap[1];//first element in array is the min
    }

    
    public MinHeapNode<AnyType> deleMin() { //delete job when finish or move into another round
        if (!isEmpty()) {  
        	@SuppressWarnings("unchecked")
			MinHeapNode<AnyType> min = heap[1];  //min node
        	heap[1] = heap[currentSize--];  //move bottom node to root
        	heapDown(1);  //move root down
            return min;  
        }  
        return null;  
    }  
    
    public void insert(MinHeapNode<AnyType> newNode) {  //insert a job
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
		@SuppressWarnings("unchecked")
		MinHeapNode<AnyType> temp = heap[position];  
        int child;  
        for (; position * 2 <= currentSize; position = child) {  
            child = 2 * position;  //get child
            if (child != currentSize  
                    && heap[child + 1].getTime() < heap[child].getTime())  
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
  
    private static void increaseArray(int capacity) { 
    	@SuppressWarnings("rawtypes")
		MinHeapNode[] oldArr = heap;  
    	@SuppressWarnings("rawtypes")
		MinHeapNode[] newArr = new MinHeapNode[capacity];  
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
