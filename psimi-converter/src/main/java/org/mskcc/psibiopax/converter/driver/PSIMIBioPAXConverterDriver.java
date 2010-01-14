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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.FileReader;
import java.io.BufferedReader;

/**
 * Driver class for PSI-MI to BioPax converter.
 *
 * @author Benjamin Gross
 */
public class PSIMIBioPAXConverterDriver {

	/**
	 * Method to determine PSI level.
	 *
	 * @param inputFile String
	 * @throws Exception
	 */
	static public void checkPSILevel(String inputFile) throws Exception {

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
