package org.biopax.paxtools.io.pathwayCommons.util;

import com.thoughtworks.xstream.XStream;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.pathwayCommons.model.Error;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.BioPaxIOException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts the REST response into a BioPAX model, if the model is not valid
 * throws a PathwayCommons exception.
 *
 * @see org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException
 */
public class BioPAXHttpMessageConverter implements HttpMessageConverter<Model> {
    private BioPAXIOHandler bioPAXIOHandler;
    private List<MediaType> mediaList = null;

    public BioPAXHttpMessageConverter(BioPAXIOHandler bioPAXIOHandler) {
        this.bioPAXIOHandler = bioPAXIOHandler;
    }

    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return Model.class.equals(clazz);
    }

    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    public List<MediaType> getSupportedMediaTypes() {
        if(mediaList == null) {
            mediaList = new ArrayList<MediaType>();
            mediaList.add(MediaType.APPLICATION_XHTML_XML);
            mediaList.add(MediaType.APPLICATION_XML);
            mediaList.add(MediaType.TEXT_HTML);
            mediaList.add(MediaType.TEXT_XML);
            mediaList.add(MediaType.TEXT_PLAIN);
        }

        return mediaList;
    }

    public Model read(Class<? extends Model> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException
    {
        /* OK, a little bit of hacking here:

           The PC2 server returns either a valid BioPAX model or an error coded in XML.
           So if the BioPAX IO Handler fails, we have to re-read the stream from the
           scratch in order to parse the error details.

           This is why we copy the stream into a buffered one.
         */
        BufferedInputStream bis = new BufferedInputStream(inputMessage.getBody());
        bis.mark(0);

        try {
            return bioPAXIOHandler.convertFromOWL(bis);
        } catch(BioPaxIOException ioe) { // Not a BioPAX file, so go with the error parsing
            bis.reset();
            XStream xStream = new XStream();
            xStream.alias("error", Error.class);
            Error error = (Error) xStream.fromXML(bis);
            throw ErrorUtil.createException(error);
        }
    }

    public void write(Model model, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException
    {
        throw new UnsupportedOperationException("Not supported!");
    }
}
