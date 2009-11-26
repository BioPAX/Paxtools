
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class ExampleGet {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGet() throws HttpException, IOException {

		String url = "http://www.biopax.org/biopax-validator/";
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);

        //NameValuePair nvp1= new NameValuePair("firstName","fname");
        //method.setQueryString(new NameValuePair[]{nvp1});

		int statusCode = client.executeMethod(method);

		System.out.println("QueryString>>> " + method.getQueryString());
		System.out.println("Status Text>>>"
				+ HttpStatus.getStatusText(statusCode));

		// Get data as a String
		System.out.println(method.getResponseBodyAsString());

		// OR as a byte array
		byte[] res = method.getResponseBody();

		assertTrue(res != null && res.length > 0 && (new String(res)).contains("BioPAX Validator"));

		// release connection
		method.releaseConnection();

    }
	
}
