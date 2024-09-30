/* 
	input will be	java programname filename timequantum
filename will look like
	pid,arrive,burst
	1,  0,     5,
	2,  1,     7,
	3,  0,     2,
	4,  2,     6,

	Your simulation should include the following
Clock = timestamps all events for each process, such as creation time, exec time, completion time
Process Creator = creates processes at arival time
CPU = runs processes at a time quantum
Queue = first in first out ready queue used by both the process creator and CPU
Process Arrival Time = arrival time of new processes into the ready queue
Process Service Time = amount of time required by the processes to complete execution
Time Quantum 
Context Switch = number of times a process is switched

	program should print out following performace evaluation criteria
cpu util = percentage of cpu being used
throughput = number of jobs processed/completed
turnaround = time to complete a process on average
waiting time = total time processes are waiting in ready que.

	round robin works
complete time quantum of first process with lowest arive time
	if process finishes in time quantum remove it from que
	if process does not finish move the process to the end of que
complete time quantum of process with the 2nd to lowest arival time
	if process finishes in time quantum remove it from que
	if process does not finish move the process to the end of que
etc until reached end of process list
re-do the new que made after execing the round robin once.
	
*/ 
import java.util.*;
import java.io.*;

//target is the class that shows running time info etc
// and it is how we are going to display everything run by round robin.
public class target{
	
	String filnme;
	
	int currenttme = 0; 
	int timequantum;
	int cpuutilization;
	int processess; 
	int processesremoved = 0;
	int arvtime = 0;
	int contextswitch = 0;

	
	Processes our_prosseses = new Processes();
	Dispatcher disptch = new Dispatcher();
	
	
	
	public static void main (String[]args)throws FileNotFoundException{
		//give the file name and the time quantum from the command line argument to the constructor
		//constructor will take the file name, read the file and properly assign values needed.
		target tgt = new target(args);
		tgt.RoundRobin();
		
		System.out.print("\nProcess Number \tTurnaround Time \tCompletion Time \tWait Time \tNumber of Context Switches \tCPU Util with Context \n"); 
		for (int d = 0; d < tgt.our_prosseses.giveTIDs().size(); d++){
			System.out.print(" "+(d+1)+"\t\t"+(tgt.our_prosseses.giveturnaroundtime(d))+"\t\t\t"+(tgt.our_prosseses.givecomptime(d))+"\t\t\t"+(tgt.our_prosseses.givewaittime(d))+"\t\t"+(tgt.our_prosseses.getContextS(d))+"\t\t\t\t"+(1 -((tgt.our_prosseses.getCStime(d)*tgt.our_prosseses.getContextS(d))/tgt.currenttme))+"\n");
		}
	}
	
	public target(String[] args)throws FileNotFoundException{
		this.filnme = args[0];
		this.timequantum = Integer.parseInt(args[1]);
		
		BufferedReader reader = null;
		String line = "";
		
		try {
			//read the file and assign the proccess id, arive time, and burst time respectively
			File myFile = new File(filnme);
			reader = new BufferedReader(new FileReader((myFile)));
			
			int c = 0;
			while((line = reader.readLine()) != null){
				String[] row = line.split(",");
				//skip the first line being the names of the row
				if (c == 0){
					c++;
				}
				else {
					c++;
					//add values to respective arraylists
					our_prosseses.addProccess(Integer.parseInt(row[0]),Integer.parseInt(row[1]),Integer.parseInt(row[2]));
					
					
				}
				processess = c-1;
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
		
	}
	public void RoundRobin(){
		
		initalizeReadyqueue();
		
		System.out.println("Processes Left = "+our_prosseses.giveIDs());
		System.out.println("Ready Que = "+disptch.returnReady());

		//ready que initialized, now increment timer until the first arrival time is matched with the time
		while (arvtime != our_prosseses.giveArrivetime(our_prosseses.giveIDs().indexOf(disptch.returnReady(0)))){
			System.out.println("Arrival time = "+arvtime);
			//everything is idle to increment idle time
			arvtime++;
			currenttme++;
		}
		
		ArrayList<Integer> proctoterminate = new ArrayList<Integer>();
		
		//FIFO Scheduling, first process in, first process out
		//run the process / minus the time quantum with the burst time.
		for (int d = 0; d < disptch.returnReady().size(); d++){
			
			//if the burst time is less than the time quantum
			if (our_prosseses.giveBursttime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d))) < timequantum){
				//increment until arrival time is the same as the processes arrival time
				int procnum = (our_prosseses.giveTIDs().indexOf(disptch.returnReady(d)));
				
				currenttme+=timequantum;
				
				System.out.println("Terminating process "+disptch.returnReady(d)+ " at time "+currenttme);
				proctoterminate.add(disptch.returnReady(d));
				
				our_prosseses.editcompletiontime(procnum , currenttme);
				
				//Turn around time = Completion time - Arrival time
				our_prosseses.editTAT(procnum, (currenttme - our_prosseses.giveArrivetime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d)))));
				//Waiting time = Turn Around Time - Burst Time
				our_prosseses.editwaittme(procnum, (((currenttme - our_prosseses.giveArrivetime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d))))) - our_prosseses.giveBurstt(our_prosseses.giveIDs().indexOf(disptch.returnReady(d)))));

				our_prosseses.addContextS(procnum);
				//0.1 for 10 ms
				our_prosseses.incrementCStime(procnum, 0.1);
				contextswitch++;
				continue;
				
				
			}
			//if the burst time is the same as the time quantum
			else if(our_prosseses.giveBursttime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d))) == timequantum){
				int procnum = (our_prosseses.giveTIDs().indexOf(disptch.returnReady(d)));
				currenttme+=timequantum;
				
				System.out.println("Terminating process "+disptch.returnReady(d)+" at time "+currenttme);
				proctoterminate.add(disptch.returnReady(d));
				
				our_prosseses.editcompletiontime(procnum , currenttme);
				
				//Turn around time = Completion time - Arrival time
				our_prosseses.editTAT(procnum, (currenttme - our_prosseses.giveArrivetime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d)))));
				//Waiting time = Turn Around Time - Burst Time
				our_prosseses.editwaittme(procnum, (((currenttme - our_prosseses.giveArrivetime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d))))) - our_prosseses.giveBurstt(our_prosseses.giveIDs().indexOf(disptch.returnReady(d)))));

				our_prosseses.addContextS(procnum);
				//0.1 for 10 ms
				our_prosseses.incrementCStime(procnum, 0.1);
				contextswitch++;
				continue;
				
				
			}
			//if the burst time is more than the time quantum
			else {
				int procnum = (our_prosseses.giveTIDs().indexOf(disptch.returnReady(d)));
				currenttme+=timequantum;
				our_prosseses.editBursttime(our_prosseses.giveIDs().indexOf(disptch.returnReady(d)), (timequantum));

				our_prosseses.addContextS(procnum);
				//0.1 for 10 ms
				our_prosseses.incrementCStime(procnum, 0.1);
				contextswitch++;
				
			}
			
			
		}

		for (int i : proctoterminate){
			disptch.terminateQueue(our_prosseses.giveIDs().indexOf(i), our_prosseses);
			processess--;
			processesremoved++;
		}
		
		proctoterminate.clear();
		disptch.clearQueue();
		
		System.out.println(our_prosseses);
		System.out.println("Total Context switches "+contextswitch);
		System.out.println(processesremoved+" total processes removed");
		System.out.println(" ");
		
		while (!(our_prosseses.giveIDs().isEmpty())){
			RoundRobin();
		}
		
		
		
	}
	public void initalizeReadyqueue(){
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Integer> tempid = new ArrayList<Integer>();
		
		for (int d = 0; d < processess; d++){
			if (our_prosseses.giveArrivetime(d) == 0){
				System.out.println("Process "+(d+1)+" added to ready queue");
				disptch.addtoReady(our_prosseses.giveProcessID(d));
			}else {
				temp.add(our_prosseses.giveArrivetime(d));
				tempid.add(our_prosseses.giveProcessID(d));
			}
			
		}
			//sort numbers from lowest to highest based off arrive time
				for (int a = 0; a < temp.size(); a++){
					for (int n = a + 1; n < temp.size(); n++){
						if(temp.get(a) > temp.get(n)){
							int tmp = tempid.get(a);
							int tmp2 = temp.get(a);
							tempid.set(a, tempid.get(n));
							temp.set(a, temp.get(n));
							temp.set(n,tmp2);
							tempid.set(n, tmp);
						}
					}
					
				}
			for (int i : tempid){
				disptch.addtoReady(i);
				System.out.println("Process "+i+" added to ready queue");
			}
	}
	
	public int getcurrent(){
		return currenttme;
	}
	public void editcurnttime(int elapsedtime){
		currenttme+=elapsedtime;
	}
	
	//Dispatcher class holds all queues and gives power to cpu when a process reaches ready que
	//once process reaches end of ready it goes to terminate que and terminate que removes it from respective lists.
	public class Dispatcher {
		//arraylists full of respective process IDs
		ArrayList<Integer> readyqueue = new ArrayList<Integer>();
		ArrayList<Integer> waitingqueue = new ArrayList<Integer>();
		
		public Dispatcher () {
		
		}
		
		public void clearQueue(){
			readyqueue.clear();
			waitingqueue.clear();
		}
		public void addtoReady (int p_id){
			readyqueue.add(p_id);
		}
		public void addtoWaiting (int p_id){
			waitingqueue.add(p_id);
		}
		public void terminateQueue (int p_id, Processes proc){
			readyqueue.remove(p_id);
			proc.removeProcess(p_id);
		}
		public ArrayList<Integer> returnReady (){
			return readyqueue;
		}
		public int returnReady (int id){
			return readyqueue.get(id);
		}
		public void removefromready (int id){
			readyqueue.remove(id);
		}
		public ArrayList<Integer> returnWaiting(){
			return waitingqueue;
		}
		
	}
	
	//processes class handles processes by giving their id, arive time and burst time.
	public class Processes {
		//arraylists of information about process
		ArrayList<Integer> p_id = new ArrayList<Integer>();
		ArrayList<Integer> tempID = new ArrayList<Integer>();
		ArrayList<Integer> tempBrst = new ArrayList<Integer>();
		ArrayList<Integer> arve_tme = new ArrayList<Integer>();
		ArrayList<Integer> brst_tme = new ArrayList<Integer>();
		ArrayList<Integer> waittime = new ArrayList<Integer>();
		ArrayList<Integer> comptime = new ArrayList<Integer>();
		ArrayList<Integer> TAT = new ArrayList<Integer>();
		ArrayList<Integer> numofcs = new ArrayList<Integer>();
		ArrayList<Double> cstime = new ArrayList<Double>();
		public Processes(){
		
		}

		public void addContextS(int p_id){
			numofcs.set(p_id, (numofcs.get(p_id)+1));
		}
		public void incrementCStime (int p_id, double time){
			cstime.set(p_id, (cstime.get(p_id)+time));
		}
		public Double getCStime (int p_id){
			return cstime.get(p_id);
		}
		public int getContextS (int p_id){
			return numofcs.get(p_id);
		}
		public ArrayList<Integer> giveIDs (){
			return p_id;
		}
		
		public ArrayList<Integer> givecomptime (){
			return comptime;
		}
		public int givecomptime (int p_id){
			return comptime.get(p_id);
		}
		public ArrayList<Integer> giveturnaroundtime(){
			return TAT;
		}
		
		public int giveturnaroundtime(int p_id){
			return TAT.get(p_id);
		}
		
		public void editTAT(int p_id, int time){
			TAT.set(p_id, time);
		}
		public void editwaittme (int p_id, int time){
			waittime.set(p_id, time);
		}
		
		public int givewaittime (int p_id){
			return waittime.get(p_id);
		}
		public void addProccess (int id, int ariv, int brst){
			p_id.add(id);
			tempID.add(id);
			arve_tme.add(ariv);
			tempBrst.add(brst);
			brst_tme.add(brst);
			cstime.add(0.0);
			waittime.add(0);
			numofcs.add(0);
			comptime.add(0);
			TAT.add(0);
		}
		public void editcompletiontime (int p_id, int time){
			comptime.set(p_id, time);
		}
		public ArrayList<Integer> giveTIDs(){
			return tempID;
		}
		public void removeProcess (int id){
			p_id.remove(id);
			arve_tme.remove(id);
			brst_tme.remove(id);
		}
		public int giveProcessID (int id){
			return p_id.get(id);
		}
		public int giveArrivetime (int p_id){
			return arve_tme.get(p_id);
		}
		public int giveBursttime (int p_id){
			return brst_tme.get(p_id);
		}
		public int giveBurstt (int p_id){
			return tempBrst.get(p_id);
		}
		public ArrayList<Integer> giveBursttimes(){
			return brst_tme;
		}
		public void editBursttime (int p_id, int time){
			brst_tme.set((p_id), (brst_tme.get(p_id) - time));
		}
		public String toString(){
			String stg = "";
			stg+="proccesses = "+(p_id);
			stg+=" processes arrive time = "+(arve_tme);
			stg+=" processes burst time = "+(brst_tme);
			return stg;
		}
	}
}