package org.biopax.paxtools;


import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.io.sif.level2.*;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.controller.Merger;
import org.biopax.paxtools.controller.Integrator;
import org.biopax.paxtools.model.Model;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * A command line accessible utility for basic Paxtools functionalities.
 *
 */
public class PaxtoolsMain {

    public static Log log = LogFactory.getLog(PaxtoolsMain.class);
    private final static String CLASS_NAME = "PaxtoolsMain";

    // Default
    private static BioPAXIOHandler ioHandler;

    private static BioPAXIOHandler io()
    {
       if(ioHandler == null)
       {
           ioHandler = new SimpleReader();
       }
       return ioHandler;
    }

    public static void main(String[] argv) throws IOException, InvocationTargetException, IllegalAccessException {


        if( argv.length < 1 ) {
            showHelp();
        }

        for(int count = 0; count < argv.length; count++) {
            if( argv[count].equals("--help") ) {
                showHelp();
            } else if( argv[count].equals("--merge") ) {
                if( argv.length <= count+3 )
                    showHelp();
                
                Model model1 = getModel(io(), argv[count+1]);
                Model model2 = getModel(io(), argv[count+2]);

                Merger merger = new Merger(new SimpleEditorMap());
                merger.merge(model1, model2);

                SimpleExporter simpleIO = new SimpleExporter(model1.getLevel());
                simpleIO.convertToOWL(model1, new FileOutputStream(argv[count+3]));

            } else if( argv[count].equals("--integrate") ) {
                if( argv.length <= count+3 )
                    showHelp();
                
                Model model1 = getModel(io(), argv[count+1]);
                Model model2 = getModel(io(), argv[count+2]);

                Integrator integrator = 
                	new Integrator(new SimpleEditorMap(), model1, model2);
                integrator.integrate();

                SimpleExporter simpleIO = new SimpleExporter(model1.getLevel());
                simpleIO.convertToOWL(model1, new FileOutputStream(argv[count+3]));

            } else if( argv[count].equals("--to-sif") ) {
                if( argv.length <= count+2 )
                    showHelp();

                Model model = getModel(io(), argv[count+1]);

                SimpleInteractionConverter sic
                        = new SimpleInteractionConverter(
                                new ComponentRule(),
                                new ConsecutiveCatalysisRule(),
                                new ControlRule(),
                                new ControlsTogetherRule(),
                                new ParticipatesRule()
                        );

                sic.writeInteractionsInSIF(model, new FileOutputStream(argv[count+2]));

            } else if( argv[count].equals("--validate") ) {
                try {
                    Model m = getModel(io(), argv[count+1]);
                    System.out.println("Model that contains " 
                    		+ m.getObjects().size()
                    		+ " elements is created (check the log messages)");
                } catch(Exception e)
                {
                    System.err.println("Error during validation" + e);
                    e.printStackTrace();
                    log.error("Error during validation", e);
                    System.exit(-1);
                }

            }
        }

        System.exit(0);

    }

    private static void showHelp() {
        System.err.println(
                    "Invalid usage: " + CLASS_NAME + "\n"
                +   "\n"
                +   "Avaliable operations:\n"
                +   "--merge file1 file2 output" +   "\t\t" + "merges file2 into file1 and writes it into output\n"
                +   "--to-sif file1 output" +      "\t\t\t" + "converts model to simple interaction format\n"
                +   "--validate file1" +         "\t\t\t\t" + "validates BioPAX model file1\n"
                +   "--integrate file1 file2 output" + "\t" + "integrates file2 into file1 and writes it into output (experimental)\n"
                +   "\n"
                +   "--help" +               "\t\t\t\t\t\t" + "prints this screen and exits"
            );

        System.exit(-1);
    }

    private static Model getModel(BioPAXIOHandler io,
                                 String fName) throws FileNotFoundException {
        FileInputStream file = new FileInputStream(fName);
        return io.convertFromOWL(file);
    }
}
