import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.biopax.validator.BiopaxValidatorClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

// remove @Ignore when biopax.org/biopax-validator/  is available
@Ignore
public class BiopaxValidatorClientTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testClientHtml() throws IOException {
		BiopaxValidatorClient client = new BiopaxValidatorClient(null, true);
		File[] files = new File[] {
				new File(getClass().getResource(File.separator 
						+ "testBiopaxElementIdRule.owl").getFile())
		};
		client.validate(files, new FileOutputStream("target"
				+ File.separator + "result.html"));
    }
	
	@Test
	public void testClientXml() throws IOException {
		BiopaxValidatorClient client = new BiopaxValidatorClient();
		File[] files = new File[] {
				new File(getClass().getResource(File.separator 
						+ "testBiopaxElementIdRule.owl").getFile())
		};
		client.validate(files, new FileOutputStream("target" 
				+ File.separator + "result.xml"));
    }
	
}
