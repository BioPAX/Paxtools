package org.biopax.paxtools.io;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.ReusedPEPHelper;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO:Class description User: demir Date: Jun 30, 2009 Time: 2:43:17 PM
 */
public interface BioPAXIOHandler
{

	void fixReusedPEPs(boolean fixReusedPEPs);

	void treatNilAsNull(boolean treatNILasNull);

	Model convertFromOWL(InputStream in);

	Model convertFromMultipleOwlFiles(String... files)
			throws FileNotFoundException;

	void convertToOWL(Model model, OutputStream outputStream);

	void setConvertingFromLevel1ToLevel2(boolean convertingFromLevel1ToLevel2);

	boolean isTreatNilAsNull();

	void setTreatNilAsNull(boolean treatNilAsNull);

	boolean isConvertingFromLevel1ToLevel2();

	boolean isFixReusedPEPs();

	ReusedPEPHelper getReusedPEPHelper();

	BioPAXFactory getFactory();

	void setFactory(BioPAXFactory factory);

	EditorMap getEditorMap();

	void setEditorMap(EditorMap editorMap);

	BioPAXLevel getLevel();
}
