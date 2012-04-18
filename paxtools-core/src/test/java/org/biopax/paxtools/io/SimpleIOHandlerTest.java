package org.biopax.paxtools.io;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class SimpleIOHandlerTest
{
	
	private void outputModel(Model m, OutputStream out) {
		(new SimpleIOHandler()).convertToOWL(m, out);
	}
	
	@Test
	public final void testExportL2() throws FileNotFoundException	{
		Model model = BioPAXLevel.L2.getDefaultFactory().createModel();
		FileOutputStream out = new FileOutputStream(
				getClass().getResource("").getFile()
				+ File.separator + "simple.owl"
		);
		outputModel(model, out);
	}

	@Test
	public final void testReadWriteL2() throws IOException
	{
		BioPAXIOHandler io = new SimpleIOHandler();
		io.fixReusedPEPs(false);
		Model model = getL2Model(io);
		assertNotNull(model);
		assertFalse(model.getObjects().isEmpty());
		System.out.println("Model has " + model.getObjects().size() + " objects)");
		FileOutputStream out =
			new FileOutputStream(
				getClass().getResource("").getFile()
				+ File.separator + "simpleReadWrite.owl"
			);
		io.convertToOWL(model, out);
	}

	public static Model getL2Model(BioPAXIOHandler io)
	{
		String s = "L2" + File.separator
		           + "biopax_id_557861_mTor_signaling.owl";

		System.out.println("file = " + s);

		System.out.println("starting " + s);
		InputStream in = SimpleIOHandlerTest.class.getClassLoader().getResourceAsStream(s);
		assertNotNull(in);
		return io.convertFromOWL(in);
	}

	@Test
	public final void testReadWriteL3() throws IOException
	{
		BioPAXIOHandler io = new SimpleIOHandler(); //auto-detects level
		Model model = getL3Model(io);
		assertNotNull(model);
		assertFalse(model.getObjects().isEmpty());
		System.out.println("Model has " + model.getObjects().size()
				+ " objects)");
		
		// test if tailing spaces are removed from values
		Xref x = (Xref) model.getByID("http://www.biopax.org/examples/myExample#taxon_562");
		assertNotNull(x);
		assertEquals("562", x.getId());
		assertEquals("taxonomy", x.getDb());
		
		FileOutputStream out = new FileOutputStream(getClass().getResource("")
				.getFile() + File.separator + "simpleReadWrite.owl");
		io.convertToOWL(model, out);
	}

	public static Model getL3Model(BioPAXIOHandler io)
	{
		String s = "L3" + File.separator + "biopax3-short-metabolic-pathway.owl";
		InputStream in = SimpleIOHandlerTest.class.getClassLoader().getResourceAsStream(s);
		
		return io.convertFromOWL(in);
	}

	@Test
	public final void testDuplicateNamesByExporter() throws IOException
	{
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Protein p = factory.create(Protein.class, "myProtein");
		String name = "aDisplayName";
		p.setDisplayName(name);
		p.addComment("Display Name should not be repeated again in the Name property!");
		Model m = factory.createModel();
		m.add(p);

		FileOutputStream out =
				new FileOutputStream( // to the target test dir
						getClass().getResource("").getFile()
						+ File.separator + "testDuplicateNamesByExporter.xml"
				);
		
		outputModel(m, out);

		// read
		BufferedReader in = new BufferedReader(
				new FileReader(getClass().getResource("").getFile()
				               + File.separator + "testDuplicateNamesByExporter.xml"));
		char[] buf = new char[1000];
		in.read(buf);
		String xml = new String(buf);
		if (xml.indexOf(name) != xml.lastIndexOf(name))
		{
			fail("displayName gets duplicated by the SimpleIOHandler!");
		}

	}

	@Test
	public final void testhibernateFile() throws IOException
	{
		System.out.println("export");
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model m = factory.createModel();
		Protein p = m.addNew(Protein.class, "myProtein");
		MolecularInteraction mi = m.addNew(MolecularInteraction.class, "myInteraction");
		mi.addParticipant(p);
		FileOutputStream out =
				new FileOutputStream( // to the target test dir
						getClass().getClassLoader()
						.getResource("").getPath() 
						+ File.separator + "hibtest.owl"
				);
		outputModel(m, out);
	}


	@Test
	public final void testBioPAXDocument()
	{

		String ID_COMPARTMENT_1 = "compartment_1";
		String ID_PROTEIN_1 = "PROTEIN_1";
		String ID_PROTEIN_2 = "PROTEIN_2";
		String ID_PROTEIN_3 = "PROTEIN_3";

		String ID_PROTEIN_REFERENCE_1 = "PROTEIN_REFERENCE_1";
		String ID_PROTEIN_REFERENCE_2 = "PROTEIN_REFERENCE_2";
		String ID_PROTEIN_REFERENCE_3 = "PROTEIN_REFERENCE_3";

		BioPAXFactory level3Factory = BioPAXLevel.L3.getDefaultFactory();
		Model biopaxModel = level3Factory.createModel();

		// Create a compartment
		CellularLocationVocabulary clv =
				biopaxModel.addNew(CellularLocationVocabulary.class, ID_COMPARTMENT_1);
		String compartmentName = "golgi";
		clv.addTerm(compartmentName);


		// Create three proteins in golgi
		Protein p1 = biopaxModel.addNew(Protein.class, ID_PROTEIN_1);
		p1.setDisplayName("PE-Pro-BACE-1");
		Protein p2 = biopaxModel.addNew(Protein.class, ID_PROTEIN_2);
		p2.setDisplayName("PE-BACE-1");
		Protein p3 = biopaxModel.addNew(Protein.class, ID_PROTEIN_3);
		p3.setDisplayName("Furin");
		p1.setCellularLocation(clv);
		p2.setCellularLocation(clv);
		p3.setCellularLocation(clv);

		// Create entity references for the proteins
		ProteinReference pr1 = biopaxModel.addNew(ProteinReference.class, ID_PROTEIN_REFERENCE_1);
		pr1.setStandardName("Pro-BACE-1");
		p1.setEntityReference(pr1);
		Stoichiometry stoichiometry1 = biopaxModel.addNew(Stoichiometry.class, "ST1");
		stoichiometry1.setPhysicalEntity(p1);
		stoichiometry1.setStoichiometricCoefficient(1);

		ProteinReference pr2 = biopaxModel.addNew(ProteinReference.class, ID_PROTEIN_REFERENCE_2);
		pr2.setStandardName("BACE-1");
		p2.setEntityReference(pr2);
		Stoichiometry stoichiometry2 = biopaxModel.addNew(Stoichiometry.class, "ST2");
		stoichiometry2.setPhysicalEntity(p2);
		stoichiometry2.setStoichiometricCoefficient(1);

		ProteinReference pr3 = biopaxModel.addNew(ProteinReference.class, ID_PROTEIN_REFERENCE_3);
		pr3.setStandardName("Furin");
		p3.setEntityReference(pr3);
		Stoichiometry stoichiometry3 = biopaxModel.addNew(Stoichiometry.class, "ST3");
		stoichiometry3.setPhysicalEntity(p3);
		stoichiometry3.setStoichiometricCoefficient(1);

		// Create a reaction involving the three proteins
		BiochemicalReaction r = biopaxModel.addNew(BiochemicalReaction.class, "r1");
		r.addLeft(p1);
		r.addRight(p2);

		Control c = biopaxModel.addNew(Catalysis.class, "cat1");
		c.setControlType(ControlType.ACTIVATION);
		c.addControlled(r);


		// Write out the owl file
		try
		{
			System.out.println("test");
			File f = new File(getClass().getClassLoader()
					.getResource("").getPath() + File.separator + "test.owl");
			FileOutputStream anOutputStream = new FileOutputStream(f);
			outputModel(biopaxModel, anOutputStream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	@Test
	public final void testIoNoBpNamespacePrefix() throws IOException
	{
		BioPAXIOHandler io = new SimpleIOHandler(); //auto-detects level
		String s = "L3" + File.separator + "biopax3-short-metabolic-pathway2.owl";
		InputStream in = SimpleIOHandlerTest.class.getClassLoader().getResourceAsStream(s);
		Model model = io.convertFromOWL(in);
		assertNotNull(model);
		assertFalse(model.getObjects().isEmpty());
		System.out.println("Model2 has " + model.getObjects().size()
				+ " objects)");
		FileOutputStream out = new FileOutputStream(getClass().getResource("")
				.getFile() + File.separator + "simpleReadWrite2.owl");
		model.setXmlBase(null);
		io.convertToOWL(model, out);
	}

    @Test
    public final void testReadWriteReadL3() throws IOException
    {
        SimpleIOHandler io = new SimpleIOHandler();

        // No flags, just check
        readWriteReadCheckModel(io);

        // Let's use different flags and do the same
        io.mergeDuplicates(!io.isMergeDuplicates());
        readWriteReadCheckModel(io);

        // One more?
        io.mergeDuplicates(!io.isMergeDuplicates());
        io.normalizeNameSpaces(!io.isNormalizeNameSpaces());
        readWriteReadCheckModel(io);

        // And one more
        io.normalizeNameSpaces(!io.isNormalizeNameSpaces());
        io.fixReusedPEPs(!io.isFixReusedPEPs());
        readWriteReadCheckModel(io);
    }

    public void readWriteReadCheckModel(SimpleIOHandler io) throws IOException
    {
        // Read
		Model model = getL3Model(io);

        // Write
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        io.convertToOWL(model, outputStream);
        outputStream.flush();

        // Read
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        outputStream.close();
        Model newModel = io.convertFromOWL(inputStream);
        assertTrue(newModel != null);
        inputStream.close();

        // Let's do a simple check to see if whether new model is OK
        for(BioPAXElement bpe: model.getObjects())
        {
            assertTrue(newModel.containsID(bpe.getRDFId()));
        }
    }
    
	@Test
	public final void testBiopaxAndNotBiopaxMixXml() throws IOException
	{
		BioPAXIOHandler io = new SimpleIOHandler(); //auto-detects level
		String s = "L3" + File.separator + "biopax_otherxml_mix.xml";
		InputStream in = SimpleIOHandlerTest.class.getClassLoader().getResourceAsStream(s);
		Model model = io.convertFromOWL(in);
		assertNotNull(model);
		Catalysis cat = (Catalysis) model.getByID("catalysis1");
		assertNotNull(cat);
		assertEquals(1, cat.getControlled().size());
		assertEquals("Catalysis 1", cat.getDisplayName());
	}

    @Test
    public final void testXMLEscapes() throws IOException {
        BioPAXIOHandler io = new SimpleIOHandler(); //auto-detects level


        Mock mock = new Mock();
        Protein[] pr = mock.create(Protein.class, 1);

        String s = "\" \' < > & ";
        System.out.println(s);
        pr[0].getName().clear();
        pr[0].addName(s);

        // Write
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        io.convertToOWL(mock.model, outputStream);
        outputStream.flush();

        // Read
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        outputStream.close();
        Model newModel = io.convertFromOWL(inputStream);

        Protein prot = (Protein) newModel.getObjects().iterator().next();
        assertTrue(prot.getName().iterator().next().equals(s));


    }
}
