package org.biopax.paxtools.examples;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: emek
 * Date: 10/8/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class Macros

{
	public static Model open(String filename) throws FileNotFoundException
	{

		BioPAXIOHandler handler = new SimpleIOHandler();
		FileInputStream fileInputStream = new FileInputStream(filename);
		return handler.convertFromOWL(fileInputStream);
	}

}
