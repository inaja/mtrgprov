/**
 * This class is to read and writes Models to and from files. 
 */
package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

public class Utilities 
{

	//private static final Writer BufferedWriter = null;

	/**
	 * @param filename: String pointing to the location of the file on disk.
	 * @return loaded Model that was read from file.
	 */
	public static Model readModelFromFile(String filename) {
		Model results = ModelFactory.createDefaultModel();
		try {
			results = FileManager.get().loadModel(filename);
		}
		catch (org.apache.jena.riot.RiotException e) {
			System.err.println("Error in Method readModelFromFile in Class Utilities, \n"
							 + "when reading file: " + filename +"\n"
							 + e.getMessage());
		}
		return results;
	}

	/**
	 * @param model: the model to be written to screen
	 * @param format: jena supports either of:
	 * Turtle, N-Triples, NQuads, TriG, JSON-LD, RDF/XML, RDF/JSON, TriX, RDF Binary
	 */
	public static void writeModelToScreen(Model model, String format) {
		model.write(System.out, format);
	}

	/**
	 * @param model: the model to be written to file
	 * @param fileName: the filename of the file to be created and saved.
	 * @param format: jena supports either of:
	 * Turtle, N-Triples, NQuads, TriG, JSON-LD, RDF/XML, RDF/JSON, TriX, RDF Binary
	 */
	public static void writeModelToFile(Model model, String fileName, String format) {
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			model.write(writer, format);
			//System.out.println("Writing file " + fileName + ": Success");
		} catch (FileNotFoundException e) {
			System.err.println("--------------------------------------------\n"
							 + "Failed to write " + fileName + " to file!\n"
							 + e.getMessage() + "\n"
							 + "--------------------------------------------\n");

		} catch (UnsupportedEncodingException e) {
			System.err.println("--------------------------------------------\n"
							+ "Failed to write " + fileName + " to file!\n"
							+ e.getMessage() + "\n"
							+ "--------------------------------------------\n");
		}
	}
	
	/**
	 * @param model: the model to be written to file
	 * @param fileName: the filename of the file to be created and saved.
	 * @param format: jena supports either of:
	 * Turtle, N-Triples, NQuads, TriG, JSON-LD, RDF/XML, RDF/JSON, TriX, RDF Binary
	 */
	public static void writeModelToFileWithSystemOut(Model model, String fileName, String format) {
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			model.write(writer, format);
			System.out.println("Writing file " + fileName + ": Success");
		} catch (FileNotFoundException e) {
			System.err.println("--------------------------------------------\n"
							 + "Failed to write " + fileName + " to file!\n"
							 + e.getMessage() + "\n"
							 + "--------------------------------------------\n");

		} catch (UnsupportedEncodingException e) {
			System.err.println("--------------------------------------------\n"
							+ "Failed to write " + fileName + " to file!\n"
							+ e.getMessage() + "\n"
							+ "--------------------------------------------\n");
		}
	}
	
	public static void writeToFile (String fname, String toBeWritten) {
		File fileName = new File(fname);
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
			} catch (IOException e) {
				System.err.println("Error, could not create file: " + e.getMessage());
			}
		}
		try (PrintWriter out = new PrintWriter(fileName)) {
			out.println(toBeWritten);
		}
		catch (FileNotFoundException e){
			System.err.println("Error Writing to File: " + e.getMessage());
		}
	}
	
	public static void writeToFile (String fname, ArrayList<String> toBeWritten) {
		File fileName = new File(fname);
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
			} catch (IOException e) {
				System.err.println("Error, could not create file: " + e.getMessage());
			}
		}
		try (PrintWriter out = new PrintWriter(fileName)) {
			for (String text : toBeWritten) {
				out.println(text);
			}
		}
		catch (FileNotFoundException e){
			System.err.println("Error Writing to File: " + e.getMessage());
		}
	}
	
	public static void writeToFile (String fname, ArrayList<String> toBeWritten, ArrayList<String> toBeWritten2) {
		File fileName = new File(fname);
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
			} catch (IOException e) {
				System.err.println("Error, could not create file: " + e.getMessage());
			}
		}
		try (PrintWriter out = new PrintWriter(fileName)) {
			for (String text : toBeWritten) {
				out.println(text);
			}
			System.out.println();
			for (String text : toBeWritten2) {
				out.println(text);
			}
		}
		catch (FileNotFoundException e){
			System.err.println("Error Writing to File: " + e.getMessage());
		}
	}
	
	public static String[] getTime () {
		
		//LocalDateTime timePoint = LocalDateTime.now();
		//String timeString = "";
		//timeString += timePoint.getYear() + "" + timePoint.getMonthValue() + "" + timePoint.getDayOfMonth()
		//				+ "" + timePoint.getHour() + "" + timePoint.getMinute() + "" + 
		//				timePoint.getSecond() + "";
		//System.out.println(timeString);
		String timestamp0 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z").format(Calendar.getInstance().getTime());
		String timestamp1 = new SimpleDateFormat("dd-MM-yyyy_HHmmssz").format(Calendar.getInstance().getTime());
		String timestamp2 = new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime());
		
		String results []  = new String[3];
		results[0] = timestamp0;
		results[1] = timestamp1;
		results[2] = timestamp2;
		//System.out.println(timeStamp);
		return results;
	}
	
	public static long getTimeInMillis () {
		return  System.currentTimeMillis() % 1000;
	}
	
	public static void goToSleep(int howLongInSecs) {
		try {
	  		System.out.println("Sleeping for: " + howLongInSecs + " seconds.");
			TimeUnit.SECONDS.sleep(howLongInSecs);
		} catch (InterruptedException e) {
			System.out.println("Warning, Interrupted");
			e.printStackTrace();
		}
	}
	
}
