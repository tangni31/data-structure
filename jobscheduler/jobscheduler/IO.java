package job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;


public class IO {
	private BufferedWriter output;
	private BufferedReader reader;
	
	public IO(String inputFilename, String outputFilename) throws IOException {
    	this.output = new BufferedWriter(new FileWriter(new File(outputFilename)));
		this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilename)));
	}

	public BufferedWriter getOutput() {
		return output;
	}
	
	public void writeTofile(String line) throws IOException{
		output.write(line);
		output.flush();
		System.out.println(line);
	}

	public Deque<String> readInputFile() throws IOException{//read and cache all commands in a list
		Deque<String> inputs = new ArrayDeque<>();
		String line = "";   
		while (true) {  //line != null
			line = reader.readLine();
			if (line == null) {
				break;
			}
			inputs.add(line);
		} 
		reader.close();
		return inputs;
	}
}
