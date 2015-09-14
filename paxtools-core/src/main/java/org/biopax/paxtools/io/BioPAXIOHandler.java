package org.biopax.paxtools.io;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface defines IO related operations that can be performed on
 * BioPAX models.
 */
public interface BioPAXIOHandler
{

 /**
  * This option is only applicable two level 2 models.
  * When enabled it will replicate illegally reused
  * pysicalEntityParticipants in Level2 files.
  * @param fixReusedPEPs true or false
  */
 void fixReusedPEPs(boolean fixReusedPEPs);

 /**
  * This flag will fix a common legacy bug from BioCyc where the "NIL" string
  * was used for representing unknown values.
  * @param treatNILasNull true or false
  */
 void treatNilAsNull(boolean treatNILasNull);

 /**
  * This method will read the OWL document given by the input stream
  * and will convert it into an in memory BioPAX model.
  * @param in a BioPAX data input stream (RDF/XML format)
  * @return new BioPAX object Model
  */
 Model convertFromOWL(InputStream in);

 /**
  * This method will read multiple OWL document
  * and will merge them into an in memory BioPAX model.
  * @param files input BioPAX files (RDF/XML)
  * @return new BioPAX object model
  * @throws FileNotFoundException when a file does not exist.
  */
 Model convertFromMultipleOwlFiles(String... files)
   throws FileNotFoundException;

 /**
  * This method will write the model to the output stream. Default encoding
  * is RDF/XML.
  * @param model a BioPAX model
  * @param outputStream output stream
  */
 void convertToOWL(Model model, OutputStream outputStream);

 /**
  * This flag will allow reader to automatically convert level1 classes to
  * corresponding level 2 classes.
  * @param convertingFromLevel1ToLevel2 true/false, whether actual BioPAX data is of Level1
  * @deprecated BioPAX Level 1 exports are obsolete (anyway, L1 input is always converted to L2 automatically).
  */
 void setConvertingFromLevel1ToLevel2(boolean convertingFromLevel1ToLevel2);

 /**
  * This flag will fix a common legacy bug from BioCyc where the "NIL" string
  * was used for representing unknown values.
  * @return true if this option is enabled.
  */
 boolean isTreatNilAsNull();

 /**
  * This flag will allow reader to automatically convert level1 classes to
  * corresponding level 2 classes.
  * @return true if this option is enabled (e.g. auto-enabled if Level1 data were detected).
  */
 boolean isConvertingFromLevel1ToLevel2();

 /**
  * This option is only applicable two level 2 models.
  * When enabled it will replicate illegally reused
  * pysicalEntityParticipants in Level2 files.
  * @return true if this option is enabled.
  */
 boolean isFixReusedPEPs();

 /**
  * @return the factory that is used to create new BioPAX POJOs during a BioPAXIOHandler operation.
  */
 BioPAXFactory getFactory();

 /**
  * @param factory used for creating objects
  */
 void setFactory(BioPAXFactory factory);

 /**
  * @return EditorMap used for this handler.
  */
 EditorMap getEditorMap();

 /**
  * @param editorMap used for this handler.
  */
 void setEditorMap(EditorMap editorMap);

 /**
  * @return The level of the model that is being read.
  */
 BioPAXLevel getLevel();

 /**
  * This method will "excise" a new model from the given model that contains
  * the objects with given ids and their dependents.
  * @param model BioPAX object model to be exported to the output stream as RDF/XML
  * @param outputStream the stream
  * @param ids optional list of absolute URIs of BioPAX objects - roots/seeds for extracting a sub-model to be exported
  */
 void convertToOWL(Model model, OutputStream outputStream, String... ids);
}
