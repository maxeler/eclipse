package com.maxeler.eclipse.installer;
import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MaxelerECJInstaller {

	private JFrame installer;
	private JTextField eclipsePath;
	private static File eclipseFolder;
	private static File pluginsFolder;
	private static File installFolder = new File("plugins");
	private static File configFile;
	private static boolean isInstalled;
	private static LinkedList<String> configFileLines;
	private JButton install;
	private JButton uninstall;
	private static Map<String, String> plugins = new HashMap<String, String>(2);
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args.length > 0)
            noGuiInstall(args);
        else
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MaxelerECJInstaller window = new MaxelerECJInstaller();
					window.installer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MaxelerECJInstaller() {
		initialize();
	}

	static interface InternalLogger {
		public void info(String msg);

		public void error(String msg);

		public void clear();
	}

	private static InternalLogger log = null;

	static class GUILogger implements InternalLogger {

		public GUILogger(JTextArea logArea) {
			this.logArea = logArea;
		}

		private JTextArea logArea;

		@Override
		public void info(String msg) {
			logArea.setForeground(Color.BLACK);
			logArea.append(msg);
		}

		@Override
		public void error(String msg) {
			logArea.setForeground(Color.RED);
			logArea.append(msg);
		}

		@Override
		public void clear() {
			logArea.setText("");
		}
	}

	static class ConsoleLogger implements InternalLogger {
		@Override
		public void info(String msg) {
			System.out.println(msg);
		}

		@Override
		public void error(String msg) {
			System.err.println("[Error]: " + msg);
		}

		@Override
		public void clear() {
		}
	}

    static class TreeCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        public Path jdt_core_file;
        public Path jdt_ui_file;

        TreeCopier(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            Path file = source.relativize(filePath);
            if (file.getFileName().toString().startsWith("org.eclipse.jdt.core")) {
                jdt_core_file = file;
                if (!copyFile(filePath, target.resolve(file)))
                    return FileVisitResult.TERMINATE;
            }
            if (file.getFileName().toString().startsWith("org.eclipse.jdt.ui")) {
                jdt_ui_file = file;
                if (!copyFile(filePath, target.resolve(file)))
                    return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                log.error("Cycle detected: " + file);
            } else {
                log.error("Error with " + file + "!\n");
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
    
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		installer = new JFrame();
		installer.setResizable(false);
		installer.setTitle("MAXJ Eclipse PlugIn Installer");
		installer.setBackground(SystemColor.window);
		installer.getContentPane().setBackground(SystemColor.window);
		installer.setBounds(100, 100, 456, 339);
		installer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		installer.getContentPane().setLayout(null);

		JTextArea installLog = new JTextArea();
		installLog.setLineWrap(true);
		installLog.setEditable(false);
		installLog.setBounds(22, 104, 420, 144);
		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(22, 104, 420, 144);
		installer.getContentPane().add(scroll);
		scroll.setViewportView(installLog);
		
		log = new GUILogger(installLog);

		JButton chooseEclipsePath = new JButton("Choose Eclipse Path");
		chooseEclipsePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser eclipsePathChooser = new JFileChooser(
						new File("."));
				eclipsePathChooser.setDialogTitle("Select Eclipse folder");
				eclipsePathChooser
						.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(eclipsePathChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					pluginsFolder = null;
					eclipseFolder = eclipsePathChooser.getSelectedFile();
					eclipsePath.setText(eclipseFolder.toString());
					log.clear();
					if(!verifyEclipseFolder(eclipseFolder)){
						log.error("Selected folder is not an eclipse directory!\n");
					}
					else{
						if(loadConfigFile()){
							if(isInstalled){
								install.setEnabled(false);
								uninstall.setEnabled(true);
							}
							else{
								install.setEnabled(true);
								uninstall.setEnabled(false);
							}
						}
						else{
							log.error("Couldn't read bundles.info file!\n");
						}
					}
				}
			}
		});
		chooseEclipsePath.setBounds(241, 67, 201, 25);
		installer.getContentPane().add(chooseEclipsePath);

		eclipsePath = new JTextField();
		eclipsePath.setEditable(false);
		eclipsePath.setBounds(101, 41, 341, 19);
		installer.getContentPane().add(eclipsePath);
		eclipsePath.setColumns(10);

		JTextArea eclipsePathLabel = new JTextArea();
		eclipsePathLabel.setEditable(false);
		eclipsePathLabel.setBackground(SystemColor.window);
		eclipsePathLabel.setText("Eclipse path:");
		eclipsePathLabel.setBounds(12, 43, 154, 25);
		installer.getContentPane().add(eclipsePathLabel);

		final JButton finish = new JButton("Finish");
		finish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				installer.dispose();
			}
		});
		finish.setEnabled(true);
		finish.setBounds(325, 260, 117, 25);
		installer.getContentPane().add(finish);

		install = new JButton("Install");
		install.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				finish.setEnabled(false);
		        int installCode = installPlugins();
		        isInstalled = installCode == 0 ? true : false;
		        if(isInstalled){
		        	install.setEnabled(false);
		        	uninstall.setEnabled(true);
		        }
		        finish.setEnabled(true);
			}
		});
		install.setEnabled(false);
		install.setBounds(171, 260, 117, 25);
		installer.getContentPane().add(install);

		uninstall = new JButton("Uninstall");
		uninstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish.setEnabled(false);
				
		        isInstalled = uninstallPlugins() == 0 ? false : true;
		        if(!isInstalled){
		        	uninstall.setEnabled(false);
		        	install.setEnabled(true);
		        }
		        finish.setEnabled(true);
		        
			}
		});
		uninstall.setEnabled(false);
		uninstall.setBounds(22, 260, 117, 25);
		installer.getContentPane().add(uninstall);

	}

	private static boolean verifyEclipseFolder(File selectedFile) {
		if (!selectedFile.isDirectory()) {
			return false;
		}
		return selectedFile.listFiles(new java.io.FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (!pathname.isDirectory()) {
					return false;
				}
				if (pathname.getName().equals("plugins")) {
					pluginsFolder = pathname;
					return true;
				} else if (pathname.getName().equals("configuration")) {
					return pathname.listFiles(new java.io.FileFilter() {
						@Override
						public boolean accept(File pathname) {
							if (pathname.getName().equals(
									"org.eclipse.equinox.simpleconfigurator")) {
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
	
	private static boolean loadConfigFile() {
        configFile = new File(FileSystems.getDefault().getPath(eclipseFolder.getPath(),
                "configuration",
                "org.eclipse.equinox.simpleconfigurator",
                "bundles.info").toString());
        BufferedReader br = null;
        configFileLines = new LinkedList<>();
        try {
            br = new BufferedReader(new FileReader(configFile));
            String fileLine = null;
            while ((fileLine = br.readLine()) != null) {
                configFileLines.add(fileLine);
                if (fileLine.startsWith("org.eclipse.jdt.core,"))
                    isInstalled = fileLine.contains("MAXELER");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
	
	private static boolean copyFile(Path source, Path target) {
        CopyOption[] options = new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING };
        try {
            Files.copy(source, target, options);
            return true;
        } catch (IOException x) {
            log.error("Unable to copy: " +    source + " to " + target + "\n");
        }
        return false;
    }
	
	private static void addConfigLine(Map<String, String> versions, String pluginFile) {
        String s1[] = pluginFile.split("_");
        String pluginName = s1[0].split("-")[0];
        String bundleVersion = s1[1].replace(".jar", "");
        versions.put(pluginName,
            pluginName + "," + bundleVersion + ",plugins/" + pluginFile + ",4,false");
    }
	
    private static int writeOutConfig() {
        int retVal = 0;
        BufferedWriter bw = null;
        String process = isInstalled ? "Uninstall" : "Install";

        try {
            bw = new BufferedWriter(new FileWriter(configFile));
            for (String line : configFileLines) {
                String newLine = line;
                for (Map.Entry<String, String> plugin : plugins.entrySet())
                {
                    if (line.startsWith(plugin.getKey() + ","))
                    {
                        plugins.remove(plugin.getKey());
                        newLine = plugin.getValue();
                        log.info(process + "ing " + plugin.getKey() + "\n");
                        break;
                    }
                }
                bw.write(newLine);
                bw.write("\n");
            }
            log.info(process + "ation finished!");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error during plugin " + process.toLowerCase() + "ation!\n");
            retVal = -1;
        } finally {
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                retVal = -1;
            }
        }
        return retVal;
    }
    
    public static int noGuiInstall(String[] args) {
        boolean doInstall = true;
        log = new ConsoleLogger();
        eclipseFolder = new File(args[0]);
        if (args.length > 1) {
            if (!args[1].equals("--revert")) {
                log.error("Too many arguments.");
                return -1;
            }
            doInstall = false;
        }
        if (!verifyEclipseFolder(eclipseFolder)) {
            log.error("Not a valid Eclipse folder: " + eclipseFolder);
            return -1;
        }
        if (!loadConfigFile()) {
            log.error("Couldn't load bundles.info!");
            return -1;
        }
        if (doInstall) {
            if (!isInstalled)
                return installPlugins();
            log.info("Nothing to do, plugins already installed.");
            return -1;
        }

        if (isInstalled)
            return uninstallPlugins();
        log.info("Nothing to do, plugins not installed.");
        return -1;
    }
    
    private static int installPlugins(){
    	Path source = FileSystems.getDefault().getPath(installFolder.getPath());
        Path target = FileSystems.getDefault().getPath(eclipseFolder.getPath(), "plugins");
        log.info("Copying plugins in folder:\n" +
                pluginsFolder.getPath() + "\n");
        TreeCopier tc = new TreeCopier(source, target);
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Could not copy plugin files!\n");
        }
        if (tc.jdt_core_file == null || tc.jdt_ui_file == null) {
            return -1;
        }
        addConfigLine(plugins, tc.jdt_core_file.getFileName().toString());
        addConfigLine(plugins, tc.jdt_ui_file.getFileName().toString());
        return writeOutConfig();
    }
    
    private static int uninstallPlugins(){
    	pluginsFolder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith("org.eclipse.jdt.core_") ||
                        pathname.getName().startsWith("org.eclipse.jdt.ui_")) {
                    addConfigLine(plugins, pathname.getName());
                    return true;
                }
                return false;
            }
        });
        if (plugins.size() < 2) {
            log.error("Original plugins not found.\n");
            return -1;
        }
        if (plugins.size() > 2) {
            log.error("Too many plugins found.\n");
            return -1;
        }
        return writeOutConfig();
    }
}
