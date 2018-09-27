# Jobscheduler
----
    
## General    
This is a simple jobscheduler for operating system. When the processor becomes free, the scheduler will assign to it a job that has been run for the least amount of time so far. This job will run for the smaller of 5ms and the amount of remaining time it needs to complete. In case the job does not complete in 5ms it becomes a candidate for the next scheduling round. The scheduler contains a `min heap` and a `Red Black Tree`. Detailed information please see: [Project_SP18.pdf](https://github.com/tangni31/data-structure/blob/master/jobscheduler/Project_SP18.pdf)      
    
## Overall Architecture     
The program mainly has 3 parts: `jobscheduler`, `RBTre`e and `Min-heap`. It has 5 class: `class jobscheduler`; `class
RBTree`, `class RBtreeNode`, `class MinHeap` and `class MinheapNode`.   
<img width="550" height="350" src="https://github.com/tangni31/data-structure/blob/master/jobscheduler/architecture.png?raw=true"/>    
As figure above, `jobscheduler` reads commands from input file and runs commands in input file, schedule job running and output a file contains all output information. `RBTreeNode` stores jobID and a pointer to corresponding minheap node, the key of RBTree is jobID. `MinHeapNode` stores jobID, executedTime and totalTime. Since jobID won’t change during the whole process, MinHeapNode doesn’t need to store a pointer to RBTreeNode, it just need to store an integer value jobID, the key of MinHeap is the executedTime.

- `Class jobscheduler`: Jobscheduler has following functions: readInputFile (String filename); getTimeStamp(String line); runJobs(); doInputCommand (String line).    
`readInputFile (String filename)` takes filename as input, it caches all input commands in a list.         
`getTimeStamp(String line)` take one line in input file as input, using regular expression to catch the time stamp in that line and convert time stamp into int and return it.        
`runJobs()` schedules job running and job inserting. Jobscheduler initiates 2 MinHeap: currentRound and otherRound when start the program. runJobs() get job with least executed time from Minheap and run it for 1 ms (executed time +1). Then it checks this job’s remain time (total time – executed time) and executed time per round. If remain time = 0, it deletes this job from both RBTree and Minheap. If executed time per round = 5, runJobs() removes this job from currentRound and send it into otherRound. runJobs() also checks the size of currentRound, if size = 0, it exchanges currentRound and otherRound.        
`doInputCommand (String line)` takes lines in input file as input, using regular expression to catch command and parameters.   
    
- Different commands in Input file:    
`InsertJob` insert a RBTree node into RBTree and a MinHeap node into Minheap.     
`PrintJob`, `PreviousJob` and `NextJob` call RBT’s function to get jobs it wants.  
    
- `Class RBTree` has 4 functions to implement `PrintJob`, `PreviousJob` and `NextJob`: getNode(Integer i); getPrevious(Integer i); getNext(Integer i); getRange(Integer i, Integer j, RBTreeNode<AnyType> node).    
`getNode(Integer i)` takes a job id as input and return i’s RBTree node and using this RBTree node to find the corresponding minheap node, then we can get all the information about this job.    
`getPrevious(Integer i)` returns the greatest job id less than i.     
 `getNext(Integer i)` returns the smallest job id larger than i.    
`getRange(Integer i, Integer j, RBTreeNode<AnyType> node)` returns all jobs with id in range (i,j).    
     
- `Class MinHeap` has functions insert(), getMin(), deleMin() and heapDown().     
`Insert()` can insert a new Minheap node into Minheap.     
`getMin()` returns the job with smallest executed time in minheap(min element).    
`deleteMin()` delete the min element in minheap.            
`heapDown()` can maintain minheap’s property after deleteMin.    
         
## Run    
Operating System: `unbuntu 16.04`    
Lanuage: `Java 8`    
To execut the program: `java jobscheduler file_name`  file_name is the name of the file that has the input test data.
