# Jobscheduler
----

This is a simple jobscheduler for operating system. When the processor becomes free, the scheduler will assign to it a job that has been run for the least amount of time so far. This job will run for the smaller of 5ms and the amount of remaining time it needs to complete. In case the job does not complete in 5ms it becomes a candidate for the next scheduling round. THe scheduler contains a `min heap` and a `Red Black Tree`. Detailed information please see: [Project_SP18.pdf](https://github.com/tangni31/data-structure/blob/master/jobscheduler/Project_SP18.pdf)  
 
To execut the program: `java jobscheduler file_name`   file_name is the name of the file that has the input test data.
