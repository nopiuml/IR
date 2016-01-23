package MyPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;



public class SplitFile {
	
	
	/**
	 * Splits the given file to smaller files.
	 * @throws IOException
	 */
	
	@SuppressWarnings("resource")
	public void SplitGivenFile() throws IOException{
		
		BufferedWriter writer = null;
        System.out.println("Enter the path of the file:");
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String filename = br.readLine();
		String content = new Scanner(new File(filename)).useDelimiter("\r\n").next();
		String files[] = content.split("[0-9]+");
		
		// if the directory does not exist, create it
		System.out.println("Enter the path of the folder you want to put splitted file:");
        BufferedReader br1 = new BufferedReader(
                new InputStreamReader(System.in));
        String folder = br1.readLine();
		  File theDir = new File(folder);
		  if (!theDir.exists()) {
		    System.out.println("creating directory: " + folder);
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		     } catch(SecurityException se){
		        //handle it
		     }        
		     if(result) {    
		       System.out.println("DIR created");  
		     }
		  }
		    for (int i=1; i<files.length; i++){
			
			String file_part = files[i];
			try{
	
				writer = new BufferedWriter(new FileWriter(folder+"/doc-text"+i+".txt"));
	            writer.write(file_part);
	            
			} catch (Exception e) {
	            e.printStackTrace();
			}
			finally {
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
		    
			}
		}
	}
	
	

	}
	
	
	
	
	
	
