package org.biopax.validator;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.methods.*;

public class BiopaxValidatorClient {
    private String url = "http://www.biopax.org/biopax-validator/validator/checkFile.html";
    private boolean asHtml;
    private static HttpClient httpClient = new HttpClient();
    
    public BiopaxValidatorClient(String url, boolean asHtml) {
		if(url != null) this.url = url;
		this.asHtml = asHtml;
	}
    
    public BiopaxValidatorClient(String url) {
    	this(url, false);
	}
    
    public BiopaxValidatorClient() {
    	this(null);
	}
    
    public static void main(String[] args) throws IOException {  
    	if(args.length == 0) {
    		System.out.println("\nAt least one argument, a directory or " +
    				"BioPAX file name, has to be provided.\n");
    		System.exit(0);
    	}

        Collection<File> files = new HashSet<File>();
		for (String name : args) {
			File fileOrDir = new File(name);
			if (!fileOrDir.canRead()) {
				System.out.println("Cannot read " + name);
				continue;
			}
			if (fileOrDir.isDirectory()) {
				// validate all the OWL files in the folder
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return (name.endsWith(".owl"));
					}
				};
				for (String s : fileOrDir.list(filter)) {
					files.add(new File(fileOrDir.getCanonicalPath() 
							+ File.separator + s));
				}
			} else {
				files.add(fileOrDir);
			} 
		}
		
		if(!files.isEmpty()) {
			BiopaxValidatorClient val = new BiopaxValidatorClient(null, true);
			val.validate(files.toArray(new File[]{}), System.out);
		} else {
			System.out.println("Nothing to do.");
		}
    }
    
    
    public void validate(File[] biopaxFiles, OutputStream out) throws IOException {
        Collection<Part> parts = new HashSet<Part>();
        
        // set result type either to HTML or XML
        StringPart resultTypePart = (asHtml) 
        	? new StringPart("retDesired", "html") 
        	: new StringPart("retDesired", "xml");
        parts.add(resultTypePart);
        
        // add files to check
        for(File f : biopaxFiles) {
        	parts.add(new FilePart(f.getName(), f));
        }
        
        PostMethod post = new PostMethod(url);
        post.setRequestEntity(
        		new MultipartRequestEntity(
        				parts.toArray(new Part[]{}), post.getParams()
        			)
        		);
        int status = httpClient.executeMethod(post);
		System.out.println("HTTP Status Text>>>"	+ HttpStatus.getStatusText(status));
		BufferedReader res = new BufferedReader(
				new InputStreamReader(post.getResponseBodyAsStream())
			);
		String line;
		PrintWriter writer = new PrintWriter(out);
		while((line = res.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		res.close();
		post.releaseConnection();
    }
    
    public void setUrl(String url) {
		this.url = url;
	}
    
    public String getUrl() {
		return url;
	}

    public void setAsHtml(boolean asHtml) {
		this.asHtml = asHtml;
	}
    
    public boolean isAsHtml() {
		return asHtml;
	}
}