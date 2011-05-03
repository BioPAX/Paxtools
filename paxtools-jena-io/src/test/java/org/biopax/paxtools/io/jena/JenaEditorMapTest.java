package org.biopax.paxtools.io.jena;

import org.biopax.paxtools.model.BioPAXLevel;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 21, 2008
 * Time: 3:14:05 PM
 */
public class JenaEditorMapTest
{
	@Test
	public void writeOutEditorProperties() throws IOException
	{
		for (BioPAXLevel level : BioPAXLevel.values())
		{
			JenaEditorMap editorMap = new JenaEditorMap(level);
			FileOutputStream outputStream = new FileOutputStream( // to 'target' dir ;)
			                                                      getClass().getResource("").getFile() +
			                                                      File.separator + level + "Editor.properties");
			editorMap.writeSimpleEditorMapProperties(outputStream);
			outputStream.close();

		}
	}
}
