package job;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Jobscheduler {
	private Deque<String> inputs = new ArrayDeque<>();
	private RBTree<Integer> RBTree = new RBTree<>();//initialize RBTree
	private boolean getNewJob = true; 
	//there are 2 rounds, one job finished 5ms will send to another round
	private MinHeap<Integer> currentRoundHeap = new MinHeap<>(); //initialize min-heap
	private MinHeap<Integer> otherRoundHeap = new MinHeap<>();
    private MinHeapNode<Integer> currentRunNode = null;
    private int totalJobTime = 0; //total time need to finished all jobs.
    private Deque<Integer[]> insertJobList = new ArrayDeque<>();
	private int idleTime = 0;
    
    public Jobscheduler(){
	}
    
    public static void main(String[] args) throws IOException {
    	Jobscheduler js = new Jobscheduler();
    	IO io = new IO("sample_input1.txt", "out_file.txt");	    
	    js.setInputs(io.readInputFile());
		String lastLine = js.inputs.getLast();//read last line in input file
		int largestTimeStamp = getTimeStamp(lastLine);//get largest time stamp
		int clockTime = largestTimeStamp;
		Deque<String> inputs = js.getInputs();
		for (int clock=0; clock<=clockTime; clock++){						
			if (!inputs.isEmpty()){
				String line = inputs.getFirst(); //get first command
				int time = getTimeStamp(line);
				if (clock == time){	//execute input command only when the global time matches the time in the input command
					js.doInputCommand(line, io);
					inputs.removeFirst();
				}					
			}
			js.insertJob();
			if (js.RBTree.getRoot() != null){ //has some jobs need to run
				js.runJobs(); //run job for 1ms						
			}else{
				js.idleTime ++;
			}
			//finish all command in input_file, need more time to finish executing remain jobs
			if (clock == largestTimeStamp && js.totalJobTime-largestTimeStamp>0){
				clockTime = js.totalJobTime+js.idleTime;//time need to finish executing remain jobs				
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
    
	public void insertJob(){ //when a job is running, can't insert new jobs into data structures, need to wait
		if (!insertJobList.isEmpty()){ //has some jobs need to be inserted
			if (currentRunNode==null || currentRunNode.excutedTimePreRound==0 || currentRunNode.excutedTimePreRound==5 ){
				//no job is running, insert
				Integer[] newJob = insertJobList.pollFirst();
				MinHeapNode<Integer> node = new MinHeapNode<Integer>(newJob[0],newJob[1]); //(jobId, time)
				RBTree.insert(newJob[0], node);
				currentRoundHeap.insert(node); 	
			}
		}
	}
	
	public Deque<String> getInputs() {
		return inputs;
	}

	public void setInputs(Deque<String> inputs) {
		this.inputs = inputs;
	}
	
	public void runJobs(){
		if (currentRoundHeap.isEmpty()) { //cur round is empty, switch
			MinHeap<Integer> temp = currentRoundHeap;
			currentRoundHeap = otherRoundHeap;
			otherRoundHeap = temp;
		}
		if (getNewJob) { //can switch only after a job run 5ms or previous job finished execution or current running job =null			
			currentRunNode = currentRoundHeap.getMin();			
			getNewJob = false;			
		}	
		currentRunNode.run(); //job runs for 1 ms	
		if (currentRunNode.getRemainTime() <= 0) {//job finished 
			RBTree.remove((int)(currentRunNode.getInfo().get(0))); //remove from RBT			
			currentRoundHeap.deleMin();//remove from minHeap			
			getNewJob = true;
		} else if (currentRunNode.excutedTimePreRound >= 5) { //executed 5 ms, move to another round		
			currentRoundHeap.deleMin();
			currentRunNode.excutedTimePreRound = 0;
			otherRoundHeap.insert(currentRunNode);
			getNewJob = true;
		}			
	}
	
	public void doInputCommand (String line, IO io) throws IOException{ //insert a job when global time is equal to arrival time
		int jobId = 0;
		int parameter1 = 0; //parameter1 & 2 represent 2 numbers in command  eg. insert(5,25)
		int parameter2 = 0;
		
		if (line.contains("Insert") || (line.contains(",") && line.contains("Print"))){ 
			//only InsertJob(x,y) & PrintJob(x,y) have 2 parameters
	    	 String pattern = "(\\(\\d+)(,\\d+)"; //using regular expression to get 2 parameters
	    	 Pattern r = Pattern.compile(pattern);
	    	 Matcher m = r.matcher(line);
	    	 m.find();
	    	 String num1 = m.group(1);	    	
	    	 parameter1 = Integer.parseInt(num1.replace("(",""));
	    	 String num2 = m.group(2);
	    	 parameter2 = Integer.parseInt(num2.replace(",",""));
	     } else { //NextJob(x),PreviousJob(x),PrintJob(x) have one parameter.
	    	 String pattern = "(\\(\\d+)";
		     Pattern r = Pattern.compile(pattern);		      
		     Matcher m = r.matcher(line);
		     m.find();
		     String num1 = m.group(1);
		     jobId = Integer.parseInt(num1.replace("(",""));     
	     }
		if (line.contains("Insert")){  //InsertJob(x,y)
			totalJobTime += parameter2; //add time to totalJobTime
			Integer[] newJob = {parameter1,parameter2};
			insertJobList.add(newJob);//add insert job to waiting list
		} else if (line.contains("Print") && line.contains(",")) {//PrintJob(x,y)
			List<RBTNode<Integer>> nodeList = new ArrayList<>();
			RBTree.getRange(parameter1, parameter2, RBTree.getRoot(), nodeList);
			if(nodeList.size()==0){	//no such jobs, print (0,0,0)
				io.writeTofile("(0,0,0)");
			} else {
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<nodeList.size(); i++)    {   
					RBTNode<Integer> job = nodeList.get(i);
					List<Integer> info = job.getHeapNode().getInfo();
					String lines = "(" + info.get(0) + "," +  info.get(1) + "," +  info.get(2) + ")" + ",";
					sb.append(lines);
				}
				sb.deleteCharAt(sb.length()-1); //delete last ";"
				io.writeTofile(sb.toString());
				
			}
		} else if (line.contains("Previous") || line.contains("Next")){ //PreviousJob(x) & NextJob(x) 
			RBTNode<Integer> job;
			if (line.contains("Previous")) {
				job = RBTree.getPrevious(jobId);
			} else {
				job = RBTree.getNext(jobId);
			}
			if (job==null){
				io.writeTofile("(0,0,0)");
			} else {
				List<Integer> info = job.getHeapNode().getInfo(); //get job's information and print
				String line1 = "(" + info.get(0) + "," +  info.get(1) + "," +  info.get(2) + ")";
				io.writeTofile(line1);
			}
		} else { //PrintJob(x)
			RBTNode<Integer> job = RBTree.getNode(jobId);
			if (job==null) {
				io.writeTofile("(0,0,0)");
			} else {
				List<Integer> info = job.getHeapNode().getInfo();
				String line2 = "(" + info.get(0) + "," +  info.get(1) + "," +  info.get(2) + ")";
				io.writeTofile(line2);
			}
		}	
	}
}
