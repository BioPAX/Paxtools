package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static javax.swing.JOptionPane.*;

/**
 * This is the user interface with GUI for selecting a model, a pattern miner, and an output file.
 * The dialog then executes the search and writes the result.
 *
 * @author Ozgun Babur
 */
public class Dialog extends JFrame implements ActionListener, KeyListener
{
	/**
	 * Checkbox for downloading and using PC data.
	 */
	private JCheckBox pcBox;

	/**
	 * Text fiels for model filename.
	 */
	private JTextField modelField;

	/**
	 * Button for loading the model.
	 */
	private JButton loadButton;

	/**
	 * Combo box for pattern to use.
	 */
	private JComboBox patternCombo;

	/**
	 * Area for description of the pattern.
	 */
	private JTextArea descArea;

	/**
	 * Field for output file name.
	 */
	private JTextField outputField;

	/**
	 * Button for searching the model.
	 */
	private JButton runButton;

	/**
	 * URL of the Pathway Commons data.
	 */
	private static final String PC_DATA_URL =
		"http://www.pathwaycommons.org/pc2/downloads/Pathway%20Commons%20all.BIOPAX.owl.gz";

	/**
	 * Name of the Pathway Commons data file.
	 */
	private static final String PC_FILE = "PC.owl";

	/**
	 * Runs the program showing the dialog.
	 * @param args ignored
	 */
	public static void main(String[] args)
	{
		Dialog d = new Dialog();
		d.setVisible(true);
	}

	/**
	 * Constructor for the dialog.
	 * @throws HeadlessException
	 */
	public Dialog() throws HeadlessException
	{
		super("Pattern Miner");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		init();
	}

	/**
	 * Initializes GUI elements.
	 */
	private void init()
	{
		setSize(400, 400);
		getContentPane().setLayout(new BorderLayout());

		JPanel modelPanel = new JPanel(new BorderLayout());

		pcBox = new JCheckBox(new File(PC_FILE).exists() ?
			"Use Pathway Commons data" : "Download and use Pathway Commons data");
		pcBox.addActionListener(this);
		modelPanel.add(pcBox, BorderLayout.NORTH);

		JPanel modelChooserPanel = new JPanel(new FlowLayout());
		modelField = new JTextField(20);
		modelChooserPanel.add(modelField);
		loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		modelChooserPanel.add(loadButton);
		modelPanel.add(modelChooserPanel, BorderLayout.CENTER);

		modelPanel.setBorder(BorderFactory.createTitledBorder("Source model"));

		getContentPane().add(modelPanel, BorderLayout.NORTH);


		JPanel minerPanel = new JPanel(new BorderLayout());
		minerPanel.setBorder(BorderFactory.createTitledBorder("Pattern to search"));
		JPanel comboPanel = new JPanel(new FlowLayout());
		JLabel patternLabel = new JLabel("Pattern: ");
		patternCombo = new JComboBox(getAvailablePatterns());
		patternCombo.addActionListener(this);
		comboPanel.add(patternLabel);
		comboPanel.add(patternCombo);
		minerPanel.add(comboPanel, BorderLayout.NORTH);

		descArea = new JTextArea(30, 3);
		descArea.setEditable(false);
		descArea.setBorder(BorderFactory.createTitledBorder("Description"));
		descArea.setText(((Miner) patternCombo.getSelectedItem()).getDescription());
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		minerPanel.add(descArea, BorderLayout.CENTER);
		getContentPane().add(minerPanel, BorderLayout.CENTER);

		JPanel finishPanel = new JPanel(new BorderLayout());

//		JPanel outPanel = new JPanel(new FlowLayout());
//		outPanel.setBorder(BorderFactory.createTitledBorder("Output file"));
		outputField = new JTextField(20);
		outputField.setBorder(BorderFactory.createTitledBorder("Output file"));
		outputField.addActionListener(this);
		outputField.addKeyListener(this);

//		outPanel.add(outputField);
		finishPanel.add(outputField, BorderLayout.WEST);

		runButton = new JButton("Run");
		runButton.addActionListener(this);
		runButton.setEnabled(false);
		finishPanel.add(runButton, BorderLayout.EAST);

		getContentPane().add(finishPanel, BorderLayout.SOUTH);
	}

	/**
	 * Performs interactive operations.
	 * @param e current event
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == pcBox)
		{
			modelField.setEnabled(!pcBox.isSelected());
			loadButton.setEnabled(!pcBox.isSelected());
		}
		else if (e.getSource() == loadButton)
		{
			String current = modelField.getText();
			String initial = current.trim().length() > 0 ? current : ".";

			JFileChooser fc = new JFileChooser(initial);
			fc.setFileFilter(new FileNameExtensionFilter("BioPAX file (*.owl)", "owl"));
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				modelField.setText(file.getPath());
			}
		}
		else if (e.getSource() == patternCombo)
		{
			Miner m = (Miner) patternCombo.getSelectedItem();
			descArea.setText(m.getDescription());
		}
		else if (e.getSource() == runButton)
		{
			run();
		}

		checkRunButton();
	}

	/**
	 * Checks if the run button should be enabled.
	 */
	private void checkRunButton()
	{
		runButton.setEnabled((pcBox.isSelected() || !modelField.getText().trim().isEmpty()) &&
			!outputField.getText().trim().isEmpty());
	}

	/**
	 * Listens key pressing events. Enables or disables run button.
	 * @param keyEvent event
	 */
	@Override
	public void keyTyped(KeyEvent keyEvent)
	{
		checkRunButton();
	}

	@Override
	public void keyPressed(KeyEvent keyEvent){}
	@Override
	public void keyReleased(KeyEvent keyEvent){}

	/**
	 * Gets the available pattern miners.
	 * @return pattern miners
	 */
	private Object[] getAvailablePatterns()
	{
		return new Object[]{
			new ControlsStateChangeMiner(),
			new TranscriptionalRegulationMiner()};
	}

	/**
	 * Executes the pattern search.
	 */
	private void run()
	{
		// Get the model file

		File modFile;

		if (pcBox.isSelected())
		{
			modFile = new File(PC_FILE);
			if (!modFile.exists())
			{
				if (!downloadPC())
				{
					showMessageDialog(this,
						"Cannot download Pathway Commons data for some reason. Sorry.");
					return;
				}
				assert modFile.exists();
			}
		}
		else
		{
			modFile = new File(modelField.getText());
		}

		// Get the output file

		File outFile = new File(outputField.getText());

		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			writer.write("x");
			writer.close();
			outFile.delete();
		} catch (IOException e)
		{
			e.printStackTrace();
			showMessageDialog(this, "Cannot write to file: " + outFile.getPath());
			return;
		}

		// Load model

		SimpleIOHandler io = new SimpleIOHandler();
		Model model = null;

		try
		{
			model = io.convertFromOWL(new FileInputStream(modFile));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			showMessageDialog(this, "File not found: " + modFile.getPath());
			return;
		}

		// Search

		Miner min = (Miner) patternCombo.getSelectedItem();

		Pattern p = min.getPattern();
		Map<BioPAXElement,List<Match>> matches = Searcher.search(model, p);

		try
		{
			min.writeResult(matches, new FileOutputStream(outFile));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			showMessageDialog(this, "Error occurred while writing the results");
			return;
		}

		// Dispose if the search went through
		dispose();
	}

	/**
	 * Downloads the PC data.
	 * @return true if download successful
	 */
	private boolean downloadPC()
	{
		try
		{
			URL url = new URL(PC_DATA_URL);
			URLConnection con = url.openConnection();
			GZIPInputStream in = new GZIPInputStream(con.getInputStream());

			// Open the output file
			OutputStream out = new FileOutputStream(PC_FILE);
			// Transfer bytes from the compressed file to the output file
			byte[] buf = new byte[1024];

			int lines = 0;
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
				lines++;
			}

			// Close the file and stream
			in.close();
			out.close();

			return lines > 0;
		}
		catch (IOException e){return false;}
	}

}
