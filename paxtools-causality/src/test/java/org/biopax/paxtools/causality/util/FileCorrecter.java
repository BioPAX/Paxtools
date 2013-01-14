package org.biopax.paxtools.causality.util;

import java.io.*;

/**
 * @author Ozgun Babur
 */
public class FileCorrecter
{
	/**
	 * Changes \n characters to real line breaks in the second line of the file, and saves as another
	 * file. 
	 */
	public static void correct(String origFile, String newFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(origFile));
		reader.readLine();
		String line = reader.readLine();
		reader.close();
		line = line.replaceAll("\\\\n", "\n");
		BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
		writer.write(line);
		writer.close();
	}

	public static void main(String[] args) throws IOException
	{
		correct("/home/ozgun/Desktop/nci_201201.owl", "/home/ozgun/Desktop/nci.owl");
	}
}
