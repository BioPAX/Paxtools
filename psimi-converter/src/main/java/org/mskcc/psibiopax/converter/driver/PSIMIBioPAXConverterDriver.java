// $Id: PSIMIBioPAXConverterDriver.java,v 1.1 2009/11/22 15:50:28 rodche Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.psibiopax.converter.driver;

// imports
import org.mskcc.psibiopax.converter.PSIMIBioPAXConverter;

import org.biopax.paxtools.model.BioPAXLevel;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Driver class for PSI-MI to BioPax converter.
 *
 * @author Benjamin Gross
 */
public class PSIMIBioPAXConverterDriver {

	/** 
	 * The big deal main.
	 */
	public static void main(String[] args) {

		// some utility info
		System.err.println("PSI-MI to BioPAX Conversion Tool v2.0");
		System.err.println("Supports PSI-MI Level 2.5 (compact) model and BioPAX Level 2 or 3.");

		// check args
		if (args.length != 3) {
			System.err.println("Usage java PSIMIBioPAXConverterDriver <biopax level (2 or 3)> <input filename> <output filename>");
			System.exit(0);
		}

		// check args - proper bp level
		Integer bpLevelArg = null;
		try {
			bpLevelArg = Integer.valueOf(args[0]);
			if (bpLevelArg != 2 && bpLevelArg != 3) {
				throw new NumberFormatException();
			}
		}
		catch (NumberFormatException e) {
			System.err.println("Incorrect BioPAX level specified: " + args[0] + " .  Please select level 2 or 3.");
			System.exit(0);
		}

		// set strings vars
		String inputFile = args[1];
		String outputFile = args[2];

		// check args - input file exists
		if (!((File)(new File(args[1]))).exists()) {
			System.err.println("input filename: " + args[1] + " does not exist!");
			System.exit(0);
		}

		// create converter and convert file
		try {
			// set bp level
			BioPAXLevel bpLevel = (bpLevelArg == 2) ? BioPAXLevel.L2 : BioPAXLevel.L3;

			// create input/output streams
			FileInputStream fis = new FileInputStream(inputFile);
			FileOutputStream fos = new FileOutputStream(outputFile);

			// create converter
			checkPSILevel(inputFile);
			PSIMIBioPAXConverter converter = new PSIMIBioPAXConverter(bpLevel);

			// note streams will be closed by converter
			converter.convert(fis, fos);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Method to determine PSI level.
	 *
	 * @param inputFile String
	 * @throws Exception
	 */
	static private void checkPSILevel(String inputFile) throws Exception {

		// create file input stream
		BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

		// parse input file looking for level tag
		String lineText = null;
		Pattern pattern = Pattern.compile("^.*level=\"(\\d)\".*$");

		// look for "<entrySet"
		while ( (lineText = bufferedReader.readLine()) != null) {
			if (lineText.matches("^\\s*<entrySet.*$")) {
				Matcher matcher = pattern.matcher(lineText);
				if (matcher.find()) {
					String levelStr = matcher.group(1);
					if (!levelStr.equals("2")) {
						bufferedReader.close();
						throw new IllegalArgumentException("Only PSI-MI Level 2.5 is supported.");
					}
					break;
				}
			}
		}

		// outta here
		bufferedReader.close();
	}
}
