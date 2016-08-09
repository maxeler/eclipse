package com.maxeler.eclipse.installer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class MaxelerECJInstaller extends JPanel implements ActionListener {
	
	private JButton openButton, installButton;
	private JFileChooser fc;
	private JTextArea log;
	private File eclipseFolder = null;
	private File configFolder = null;
	private File pluginsFolder = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.put("swing.boldMetal", Boolean.FALSE);
					createAndShowGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.getContentPane().add(new MaxelerECJInstaller());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
	/**
	 * Create the application.
	 */
	public MaxelerECJInstaller() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		fc = new JFileChooser();
	    fc.setCurrentDirectory(new java.io.File("."));
	    fc.setDialogTitle("Select Eclipse folder");
	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		setLayout(new GridBagLayout());

		openButton = new JButton("Select Eclipse folder...");
		add(openButton, new GridBagConstraints(0, 0, 1, 1,
		        0.0, 0.0,
		        GridBagConstraints.WEST, GridBagConstraints.NONE,
		        new Insets(0, 0, 0, 0), 0, 0));
		openButton.addActionListener(this);

		installButton = new JButton("Install");
		add(installButton, new GridBagConstraints(1, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		installButton.setEnabled(false);
		installButton.addActionListener(this);

		log = new JTextArea();
		log.setEditable(false);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setPreferredSize(new Dimension(600, 300));
		JScrollPane logScrollPane = new JScrollPane(log);
		add(logScrollPane, new GridBagConstraints(0, 1, 2, 1,
		        1.0, 1.0,
		        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		        new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(MaxelerECJInstaller.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                log.setText("");
                configFolder = null; pluginsFolder = null;
                if (verifyEclipseFolder(fc.getSelectedFile())) {
                	this.eclipseFolder = fc.getSelectedFile();
                	this.installButton.setEnabled(true);
                	log.append("Selected: " + fc.getSelectedFile() + ".\n");
                	readConfig();
                } else {
                	this.installButton.setEnabled(false);
                	log.append("Not a valid Eclipse folder: " + fc.getSelectedFile() + ".\n");
	            	log.setForeground(new Color(255, 0, 0));
                }
            }
            log.setCaretPosition(log.getDocument().getLength());
        } else if (e.getSource() == installButton) {
        	if (this.eclipseFolder != null) {
        		this.installEclipse();
        	}
        }
	}

 
    private void readConfig() {
				
	}

	boolean copyFile(Path source, Path target) {
        CopyOption[] options = new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING };
        try {
            Files.copy(source, target, options);
            return true;
        } catch (IOException x) {
        	log.append("Unable to copy: " +	source + " to " + target + "\n");
            System.err.format("Unable to copy: \n%s:\n%s%n", source, x);
        }
        return false;
    }
    
    class TreeCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        public Path jdt_core_file = null;
        public Path jdt_ui_file = null;
 
        TreeCopier(Path source, Path target) {
            this.source = source;
            this.target = target;
        }
 
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
        	Path file = source.relativize(filePath);
            if (!copyFile(filePath, target.resolve(file)))
            	return TERMINATE;
            if (file.toString().startsWith("org.eclipse.jdt.core"))
        		jdt_core_file = file;
        	else
        		jdt_ui_file = file;
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                System.err.println("cycle detected: " + file);
            } else {
            	log.append("Unable to copy: " + file + "!\n");
                System.err.format("Unable to copy: %s: %s%n", file, exc);
            }
            return CONTINUE;
        }

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return CONTINUE;
		}
    }
    
	private void installEclipse() {
		Path source = FileSystems.getDefault().getPath("/home/djovanovic/workspace/github/maxeler/eclipse/maxeler-eclipse-plugin-v1.0", "plugins");
		Path target = FileSystems.getDefault().getPath(this.eclipseFolder.getPath(), "plugins");
		log.append("Installing in folders:\n\t" +
				pluginsFolder.getPath() + "\n\t" +
				configFolder.getPath() + "\n");

		TreeCopier tc = new TreeCopier(source, target);
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		
		try {
			Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
		} catch (IOException e) {
			e.printStackTrace();
			log.append("Error: Couldn't copy plugin files!\n");
        	log.setForeground(new Color(255, 0, 0));
        	return;
		}
		
		Map<String, String> versions = new HashMap<String, String>(2);
		addConfigLine(versions, tc.jdt_core_file);
		addConfigLine(versions, tc.jdt_ui_file);

		File config = new File(FileSystems.getDefault().getPath(this.eclipseFolder.getPath(),
				"configuration",
				"org.eclipse.equinox.simpleconfigurator",
				"bundles.info").toString());
		LinkedList<String> fileLines = new LinkedList<String>();
		String fileLine;

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(config));
	        while( (fileLine=br.readLine()) != null )
	        { 
	        	String newLine = fileLine;
	            for (Map.Entry<String, String> plugin : versions.entrySet())
	            {
	            	if (fileLine.startsWith(plugin.getKey() + ","))
	            	{
	            		versions.remove(plugin.getKey());
		            	newLine = plugin.getValue();
		            	log.append("Installing " + plugin.getValue() + "\n");
	            		break;
	            	}
	            }
	            fileLines.add(newLine);
	        }
	        bw = new BufferedWriter(new FileWriter(config));
	        for (String line : fileLines) {
	        	bw.write(line);
	        	bw.write("\n");
	        }
			log.append("Installation finished!");
		} catch (IOException e) {
			e.printStackTrace();
			log.append("Error during plugin installation!\n");
        	log.setForeground(new Color(255, 0, 0));
		} finally {			
			try {
				if (br != null)
					br.close();
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addConfigLine(Map<String, String> versions, Path pluginFile) {
		String s1[] = pluginFile.toString().split("_");
		String pluginName = s1[0].split("-")[0]; 
		String bundleVersion = s1[1].replace(".jar", "");
		versions.put(pluginName,
			pluginName + "," + bundleVersion + ",plugins/" + pluginFile + ",4,false");
	}

	private boolean verifyEclipseFolder(File selectedFile) {		
		if (!selectedFile.isDirectory()) {
			return false;
		}
		return selectedFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (!pathname.isDirectory()) {
					return false;
				}
				if (pathname.getName().equals("plugins")) {
					MaxelerECJInstaller.this.pluginsFolder = pathname;
					return true;
				} else if (pathname.getName().equals("configuration")) {
					return pathname.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							if (pathname.getName().equals("org.eclipse.equinox.simpleconfigurator")) {
								MaxelerECJInstaller.this.configFolder = pathname;
								return true;
							}
							return false;
						}
					}).length == 1;
				}
				return false;
			}
		}).length == 2;
	}
}
