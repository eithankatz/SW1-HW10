package il.ac.tau.cs.sw1.bufferedIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**************************************
 *  Add your code to this class !!!   *
 **************************************/
public class MyBufferReader implements IBufferedReader{

	/*
	 * @pre: bufferSize > 0
	 * @pre: fReader != null
	 */
	public MyBufferReader(FileReader fReader, int bufferSize){
		//Add your code here
	}
	

	@Override
	public void close() throws IOException {
		//Leave this empty
	}


	@Override
	public String getNextLine() throws IOException {
		//Add your code here
		return null; //replace this
	}
	
}
