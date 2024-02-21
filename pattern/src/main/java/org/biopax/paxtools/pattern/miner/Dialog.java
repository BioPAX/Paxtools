package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.ProgressWatcher;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

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
	 * User specified miners to use.
	 */
	private Miner[] miners;

	/**
	 * Checkbox for downloading and using PC data.
	 */
	private JRadioButton pcRadio;

	/**
	 * Checkbox for downloading and using PC data.
	 */
	private JRadioButton customFileRadio;

	/**
	 * Checkbox for downloading and using PC data.
	 */
	private JRadioButton customURLRadio;

	/**
	 * Text fiels for model filename.
	 */
	private JTextField modelField;

	/**
	 * Text fiels for model filename.
	 */
	private JTextField urlField;

	/**
	 * Button for loading the model.
	 */
	private JButton loadButton;

	/**
	 * Combo box for pattern to use.
	 */
	private JComboBox pcCombo;

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
	 * Text for the progress.
	 */
	private JLabel prgLabel;

	/**
	 * The progress bar.
	 */
	private JProgressBar prgBar;

	/**
	 * Prefix of URL of the Pathway Commons data.
	 */
	private static final String PC_DATA_URL_PREFIX =
		"http://www.pathwaycommons.org/pc2/downloads/Pathway%20Commons.4.";
//		"http://webservice.baderlab.org:48080/downloads/Pathway%20Commons%202%20";

	/**
	 * Suffix of URL of the Pathway Commons data.
	 */
	private static final String PC_DATA_URL_SUFFIX = ".BIOPAX.owl.gz";

	/**
	 * Background color.
	 */
	private static final Color BACKGROUND = Color.WHITE;

	/**
	 * Names of Pathway Commons resources.
	 */
	private static final Object[] PC_RES_NAMES = new Object[]{
		"All-Data", "Reactome", "NCI-PID", "HumanCyc", "PhosphoSitePlus", "PANTHER"};

	/**
	 * The URL components of the Pathway Commons resources.
	 */
	private static final String[] PC_RES_URL = new String[]{
		"All", "Reactome", "NCI_Nature", "HumanCyc", "PhosphoSitePlus", "PANTHER%20Pathway"};

	/**
	 * The name of the file for IDs of ubiquitous molecules.
	 */
	private static final String UBIQUE_FILE = "blacklist.txt";

	/**
	 * The url of the file for IDs of ubiquitous molecules.
	 */
	private static final String UBIQUE_URL = "http://www.pathwaycommons.org/pc2/downloads/blacklist.txt";

	/**
	 * Blacklist for detecting ubiquitous small molecules.
	 */
	private static Blacklist blacklist;

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
	 *
	 * @param miners a list of BioPAX pattern miners
	 * @throws HeadlessException when the initialization fails
	 */
	public Dialog(Miner... miners) throws HeadlessException
	{
		super("Pattern Miner");
		this.miners = miners;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init();
	}

	/**
	 * Initializes GUI elements.
	 */
	private void init()
	{
		setSize(600, 400);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(BACKGROUND);

		JPanel modelPanel = new JPanel(new GridBagLayout());

		pcRadio = new JRadioButton("Use Pathway Commons");
		pcRadio.addActionListener(this);
		pcRadio.setBackground(BACKGROUND);
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.anchor = GridBagConstraints.LINE_START;
		modelPanel.add(pcRadio, con);

		pcCombo = new JComboBox(PC_RES_NAMES);
		pcCombo.setBackground(BACKGROUND);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 0;
		con.anchor = GridBagConstraints.CENTER;
		con.ipadx = 5;
		modelPanel.add(pcCombo, con);

		customFileRadio = new JRadioButton("Use custom file");
		customFileRadio.addActionListener(this);
		customFileRadio.setBackground(BACKGROUND);
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 1;
		con.anchor = GridBagConstraints.LINE_START;
		modelPanel.add(customFileRadio, con);

		JPanel modelChooserPanel = new JPanel(new FlowLayout());
		modelField = new JTextField(15);
		modelField.addKeyListener(this);
		modelField.setEnabled(false);
		modelChooserPanel.add(modelField);
		loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		loadButton.setEnabled(false);
		modelChooserPanel.add(loadButton);
		modelChooserPanel.setBackground(BACKGROUND);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 1;
		con.anchor = GridBagConstraints.CENTER;
		modelPanel.add(modelChooserPanel, con);

		customURLRadio = new JRadioButton("Use the owl at URL");
		customURLRadio.addActionListener(this);
		customURLRadio.setBackground(BACKGROUND);
		con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 2;
		con.anchor = GridBagConstraints.LINE_START;
		modelPanel.add(customURLRadio, con);

		urlField = new JTextField(15);
		urlField.addKeyListener(this);
		urlField.setEnabled(false);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 2;
		con.anchor = GridBagConstraints.LINE_START;
		modelPanel.add(urlField, con);

		ButtonGroup group = new ButtonGroup();
		group.add(pcRadio);
		group.add(customFileRadio);
		group.add(customURLRadio);
		group.setSelected(pcRadio.getModel(), true);

		modelPanel.setBorder(BorderFactory.createTitledBorder("Source model"));
		modelPanel.setBackground(BACKGROUND);

		getContentPane().add(modelPanel, BorderLayout.NORTH);

		JPanel minerPanel = new JPanel(new BorderLayout());
		minerPanel.setBackground(BACKGROUND);
		minerPanel.setBorder(BorderFactory.createTitledBorder("Pattern to search"));
		JPanel comboPanel = new JPanel(new FlowLayout());
		comboPanel.setBackground(BACKGROUND);
		JLabel patternLabel = new JLabel("Pattern: ");
		patternCombo = new JComboBox(getAvailablePatterns());
		patternCombo.addActionListener(this);
		patternCombo.setBackground(BACKGROUND);
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

		JPanel progressPanel = new JPanel(new GridBagLayout());
		progressPanel.setBackground(BACKGROUND);
		prgLabel = new JLabel("                       ");
		prgBar = new JProgressBar();
		prgBar.setStringPainted(true);
		prgBar.setVisible(false);
		con = new GridBagConstraints();
		con.gridx = 0;
		con.anchor = GridBagConstraints.CENTER;
		con.ipady = 12;
		con.ipadx = 10;
		progressPanel.add(prgLabel, con);
		con = new GridBagConstraints();
		con.gridx = 1;
		con.anchor = GridBagConstraints.LINE_END;
		progressPanel.add(prgBar, con);

		minerPanel.add(progressPanel, BorderLayout.SOUTH);

		getContentPane().add(minerPanel, BorderLayout.CENTER);

		JPanel finishPanel = new JPanel(new BorderLayout());

		JPanel lowerPanel = new JPanel(new FlowLayout());
		lowerPanel.setBackground(BACKGROUND);
		outputField = new JTextField(20);
		outputField.setBorder(BorderFactory.createTitledBorder("Output file"));
		outputField.addActionListener(this);
		outputField.addKeyListener(this);
		outputField.setText(((Miner) patternCombo.getSelectedItem()).getName() + ".txt");

		finishPanel.add(outputField, BorderLayout.WEST);

		runButton = new JButton("Run");
		runButton.addActionListener(this);
		finishPanel.add(runButton, BorderLayout.EAST);

		JPanel bufferPanel = new JPanel(new FlowLayout());
		bufferPanel.setMinimumSize(new Dimension(300, 10));
		bufferPanel.setBackground(BACKGROUND);
		bufferPanel.add(new JLabel("    "));
		finishPanel.add(bufferPanel, BorderLayout.CENTER);
		finishPanel.setBackground(BACKGROUND);

		lowerPanel.add(finishPanel);

		getContentPane().add(lowerPanel, BorderLayout.SOUTH);
	}

	/**
	 * Gets the maximum memory heap size for the application. This size can be modified by passing
	 * -Xmx option to the virtual machine, like "java -Xmx5G MyClass.java".
	 * @return maximum memory heap size in megabytes
	 */
	private int getMaxMemory()
	{
		int total = 0;
		for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans())
		{
			if (mpBean.getType() == MemoryType.HEAP)
			{
				total += mpBean.getUsage().getMax() >> 20;
			}
		}
		return total;
	}

	/**
	 * Performs interactive operations.
	 * @param e current event
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == pcRadio ||
			e.getSource() == customFileRadio ||
			e.getSource() == customURLRadio)
		{
			pcCombo.setEnabled(pcRadio.isSelected());
			modelField.setEnabled(customFileRadio.isSelected());
			loadButton.setEnabled(customFileRadio.isSelected());
			urlField.setEnabled(customURLRadio.isSelected());
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

			// Update output file name
			String text = outputField.getText();
			if (text.contains("/")) text = text.substring(0, text.lastIndexOf("/") + 1);
			else text = "";
			text += m.getName() + ".txt";
			outputField.setText(text);
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
		runButton.setEnabled((pcRadio.isSelected() ||
			(customFileRadio.isSelected() && !modelField.getText().trim().isEmpty()) ||
			(customURLRadio.isSelected() && !urlField.getText().trim().isEmpty()))
			&& !outputField.getText().trim().isEmpty());
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
	 * Gets the available pattern miners. First lists the parameter miners, then adds the known
	 * miners in the package.
	 * @return pattern miners
	 */
	private Object[] getAvailablePatterns()
	{
		List<Miner> minerList = new ArrayList<>();
		if (miners != null && miners.length > 0)
		{
			minerList.addAll(Arrays.asList(miners));
		}
		else
		{
			minerList.add(new DirectedRelationMiner());
			minerList.add(new ControlsStateChangeOfMiner());
			minerList.add(new CSCOButIsParticipantMiner());
			minerList.add(new CSCOBothControllerAndParticipantMiner());
			minerList.add(new CSCOThroughControllingSmallMoleculeMiner());
			minerList.add(new CSCOThroughBindingSmallMoleculeMiner());
			minerList.add(new ControlsStateChangeDetailedMiner());
			minerList.add(new ControlsPhosphorylationMiner());
			minerList.add(new ControlsTransportMiner());
			minerList.add(new ControlsExpressionMiner());
			minerList.add(new ControlsExpressionWithConvMiner());
			minerList.add(new CSCOThroughDegradationMiner());
			minerList.add(new ControlsDegradationIndirectMiner());
			minerList.add(new ConsumptionControlledByMiner());
			minerList.add(new ControlsProductionOfMiner());
			minerList.add(new CatalysisPrecedesMiner());
			minerList.add(new ChemicalAffectsThroughBindingMiner());
			minerList.add(new ChemicalAffectsThroughControlMiner());
			minerList.add(new ControlsTransportOfChemicalMiner());
			minerList.add(new InComplexWithMiner());
			minerList.add(new InteractsWithMiner());
			minerList.add(new NeighborOfMiner());
			minerList.add(new ReactsWithMiner());
			minerList.add(new UsedToProduceMiner());
			minerList.add(new RelatedGenesOfInteractionsMiner());
			minerList.add(new UbiquitousIDMiner());
		}

		for (Miner miner : minerList)
		{
			if (miner instanceof MinerAdapter) ((MinerAdapter) miner).setBlacklist(blacklist);
		}

		return minerList.toArray(new Object[minerList.size()]);
	}

	/**
	 * Executes the pattern search in a new thread.
	 */
	private void run()
	{
		Thread t = new Thread(new Runnable(){public void run(){mine();}});
		t.start();
	}

	/**
	 * Executes the pattern search.
	 */
	private void mine()
	{
		Miner miner = (Miner) patternCombo.getSelectedItem();
		if (miner instanceof MinerAdapter)
			((MinerAdapter) miner).setIDFetcher(new CommonIDFetcher());

		// Constructing the pattern before loading any model for a debug friendly code. Otherwise, if
		// loading model takes time and an exception occurs in pattern construction, it is just too
		// much wait for nothing.
				((Miner) patternCombo.getSelectedItem()).getPattern();

		// Prepare progress bar

		ProgressWatcher prg = new ProgressWatcher()
		{
			@Override
			public synchronized  void setTotalTicks(int total)
			{
				prgBar.setMaximum(total);
			}

			@Override
			public synchronized  void tick(int times)
			{
				prgBar.setValue(prgBar.getValue() + times);
			}
		};

		prgBar.setVisible(true);

		// Get the model file

		File modFile;

		if (pcRadio.isSelected())
		{
			if (getMaxMemory() < 4000)
			{
				showMessageDialog(this, "Maximum memory not large enough for handling\n" +
					"Pathway Commons data. But will try anyway.\n" +
					"Please consider running this application with the\n" +
					"virtual machine parameter \"-Xmx5G\".");
			}

			modFile = new File(getPCFilename());
			if (!modFile.exists())
			{
				prgLabel.setText("Downloading model");
				if (!downloadPC(prg))
				{
					eraseProgressBar();
					showMessageDialog(this,
						"Cannot download Pathway Commons data for some reason. Sorry.");
					return;
				}
				assert modFile.exists();
			}
		}
		else if (customFileRadio.isSelected())
		{
			modFile = new File(modelField.getText());
		}
		else if (customURLRadio.isSelected())
		{
			String url = urlField.getText().trim();
			prgLabel.setText("Downloading model");
			if (url.endsWith(".gz")) downloadCompressed(prg, url, "temp.owl", true);
			else if (url.endsWith(".zip")) downloadCompressed(prg, url, "temp.owl", false);
			else downloadPlain(url, "temp.owl");

			modFile = new File("temp.owl");

			if (!modFile.exists())
			{
				showMessageDialog(this,
					"Cannot download the model at the given URL.");
				eraseProgressBar();
				return;
			}
		}
		else
		{
			throw new RuntimeException("Code should not be able to reach here!");
		}

		// Get the output file

		File outFile = new File(outputField.getText());

		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			writer.write("x");
			writer.close();
			outFile.delete();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			eraseProgressBar();
			showMessageDialog(this, "Cannot write to file: " + outFile.getPath());
			return;
		}

		// Load model

		prgLabel.setText("Loading the model");
		prgBar.setIndeterminate(true);
		prgBar.setStringPainted(false);
		SimpleIOHandler io = new SimpleIOHandler();
		Model model;

		try
		{
			model = io.convertFromOWL(new FileInputStream(modFile));
			prgBar.setIndeterminate(false);
			prgBar.setStringPainted(true);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			eraseProgressBar();
			showMessageDialog(this, "File not found: " + modFile.getPath());
			return;
		}

		// Search

		Miner min = (Miner) patternCombo.getSelectedItem();

		Pattern p = min.getPattern();
		prgLabel.setText("Searching the pattern");
		prgBar.setValue(0);
		Map<BioPAXElement,List<Match>> matches = Searcher.search(model, p, prg);

		if (matches.isEmpty())
		{
			prgLabel.setText("No results found!");
		}
		else
		{
			try
			{
				prgLabel.setText("Writing result");
				prgBar.setValue(0);
				prgBar.setStringPainted(false);
				prgBar.setIndeterminate(true);
				FileOutputStream os = new FileOutputStream(outFile);
				min.writeResult(matches, os);
				os.close();
				prgBar.setIndeterminate(false);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				eraseProgressBar();
				showMessageDialog(this, "Error occurred while writing the results");
				return;
			}

			prgLabel.setText("Success!    ");
			System.out.println("Success!");
			this.dispose();
		}
	}

	private void eraseProgressBar()
	{
		prgLabel.setText("             ");
		prgBar.setVisible(false);
	}

	/**
	 * Gets the url for the current selected PC resource.
	 * @return the url
	 */
	private String getPCDataURL()
	{
		return PC_DATA_URL_PREFIX + PC_RES_URL[pcCombo.getSelectedIndex()] + PC_DATA_URL_SUFFIX;
	}

	/**
	 * Gets the url for the current selected PC resource.
	 * @return the url
	 */
	private String getPCFilename()
	{
		return PC_RES_NAMES[pcCombo.getSelectedIndex()].toString() + ".owl";
	}

	/**
	 * Downloads the PC data.
	 * @return true if download successful
	 */
	private boolean downloadPC(ProgressWatcher prg)
	{
		return downloadCompressed(prg, getPCDataURL(), getPCFilename(), true);
	}

	private boolean downloadCompressed(ProgressWatcher prg, String urlString, String filename,
		boolean gz)
	{
		try
		{
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			InputStream in = gz ? new GZIPInputStream(con.getInputStream()) :
				new ZipInputStream(con.getInputStream());

			prg.setTotalTicks(con.getContentLength() * 8);

			// Open the output file
			OutputStream out = new FileOutputStream(filename);
			// Transfer bytes from the compressed file to the output file
			byte[] buf = new byte[1024];

			int lines = 0;
			int len;
			while ((len = in.read(buf)) > 0)
			{
				prg.tick(len);
				out.write(buf, 0, len);
				lines++;
			}

			// Close the file and stream
			in.close();
			out.close();

			return lines > 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Downloads the PC data.
	 * @return true if download successful
	 */
	private static boolean downloadUbiques()
	{
		return downloadPlain(UBIQUE_URL, UBIQUE_FILE);
	}

	private static boolean downloadPlain(String urlString, String file)
	{
		try
		{
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			InputStream in = con.getInputStream();

			// Open the output file
			OutputStream out = new FileOutputStream(file);
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

	/**
	 * Load ubique IDs if exists.
	 */
	static
	{
		File f = new File(UBIQUE_FILE);

		if (!f.exists()) downloadUbiques();
		else if (f.exists()) blacklist = new Blacklist(f.getAbsolutePath());
		else System.out.println("Warning: Cannot load blacklist.");
	}
}
