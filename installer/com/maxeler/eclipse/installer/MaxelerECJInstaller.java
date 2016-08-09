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
import java.net.URISyntaxException;
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
    private static JButton openButton, installButton;
    private static JFileChooser fc;
    private static File eclipseFolder = null;
    private static File configFolder = null;
    private static File pluginsFolder = null;
    private static File installFolder = new File("plugins");
    private static File configFile = null;
    private static List<String> configFileLines = null;
    private static Map<String, String> plugins = new HashMap<String, String>(2);
    private static boolean isInstalled = false;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        if (args.length > 0)
            System.exit(noGuiInstall(args));
        else
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

    static interface InternalLogger {
        public void info(String msg);
        public void error(String msg);
        public void clear();
    }

    private static InternalLogger log = null;

    static class GuiLogger implements InternalLogger {
        public JTextArea logger = new JTextArea();
        private static Color errorColor = new Color(255, 0, 0);
        @Override
        public void info(String msg) {
            logger.append(msg);
        }
        @Override
        public void error(String msg) {
            logger.append(msg);
            logger.setForeground(errorColor);
        }
        @Override
        public void clear() {
            logger.setText("");
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

    private static int noGuiInstall(String[] args) {
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
            log.info("Nothing to do, plugin already installed.");
            return -1;
        }

        if (isInstalled)
            return uninstallPlugins();
        log.info("Nothing to do, plugin not installed.");
        return -1;
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Maxeler: MaxJ Plugin Installer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.getContentPane().add(new MaxelerECJInstaller());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public MaxelerECJInstaller() {
        initializeGUI();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initializeGUI() {
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
        installButton.setVisible(false);
        installButton.addActionListener(this);

        GuiLogger guiLogger = new GuiLogger();
        guiLogger.logger.setEditable(false);
        guiLogger.logger.setMargin(new Insets(5, 5, 5, 5));
        guiLogger.logger.setPreferredSize(new Dimension(600, 300));
        JScrollPane logScrollPane = new JScrollPane(guiLogger.logger);
        add(logScrollPane, new GridBagConstraints(0, 1, 2, 1,
                1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        log = guiLogger;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Handle open button action.
        log.clear();
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(MaxelerECJInstaller.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                configFolder = null; pluginsFolder = null;
                if (verifyEclipseFolder(fc.getSelectedFile())) {
                    eclipseFolder = fc.getSelectedFile();
                    installButton.setVisible(true);
                    if (!loadConfigFile()) {
                        log.error("Couldn't read bundles.info file!\n");
                        return;
                    }
                    if (isInstalled)
                        installButton.setText("Uninstall");
                    else
                        installButton.setText("Install");
                    log.info("Selected: " + fc.getSelectedFile() + ".\n");
                } else {
                    installButton.setVisible(false);
                    log.error("Not a valid Eclipse folder: " + fc.getSelectedFile() + ".\n");
                }
            }
        } else if (e.getSource() == installButton) {
            if (eclipseFolder == null) {
                return;
            }
            if (isInstalled) {
                uninstallPlugins();
            } else {
                installPlugins();
            }
            installButton.setVisible(false);
        }
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
        CopyOption[] options = new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING };
        try {
            Files.copy(source, target, options);
            return true;
        } catch (IOException x) {
            log.error("Unable to copy: " +    source + " to " + target + "\n");
        }
        return false;
    }

    static class TreeCopier implements FileVisitor<Path> {
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
            if (file.startsWith("org.eclipse.jdt.core")) {
                jdt_core_file = file;
                if (!copyFile(filePath, target.resolve(file)))
                    return TERMINATE;
            }
            if (file.startsWith("org.eclipse.jdt.core")) {
                jdt_ui_file = file;
                if (!copyFile(filePath, target.resolve(file)))
                    return TERMINATE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                log.error("Cycle detected: " + file);
            } else {
                log.error("Error with " + file + "!\n");
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }
    }

    private static int installPlugins() {
        Path source = FileSystems.getDefault().getPath(installFolder.getPath());
        Path target = FileSystems.getDefault().getPath(eclipseFolder.getPath(), "plugins");
        log.info("Installing in folders:\n\t" +
                pluginsFolder.getPath() + "\n\t" +
                configFolder.getPath() + "\n");

        TreeCopier tc = new TreeCopier(source, target);
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

        try {
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Could not copy plugin files!\n");
            return -1;
        }
        if (tc.jdt_core_file == null || tc.jdt_ui_file == null) {
            return -1;
        }
        addConfigLine(plugins, tc.jdt_core_file.getFileName().toString());
        addConfigLine(plugins, tc.jdt_ui_file.getFileName().toString());

        return writeOutConfig();
    }

    private static int uninstallPlugins() {
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
            log.error("Original plugins not found.");
            return -1;
        }
        if (plugins.size() > 2) {
            log.error("Too many plugins found.");
            return -1;
        }
        return writeOutConfig();
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

    private static void addConfigLine(Map<String, String> versions, String pluginFile) {
        String s1[] = pluginFile.split("_");
        String pluginName = s1[0].split("-")[0];
        String bundleVersion = s1[1].replace(".jar", "");
        versions.put(pluginName,
            pluginName + "," + bundleVersion + ",plugins/" + pluginFile + ",4,false");
    }

    private static boolean verifyEclipseFolder(File selectedFile) {
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
                    MaxelerECJInstaller.pluginsFolder = pathname;
                    return true;
                } else if (pathname.getName().equals("configuration")) {
                    return pathname.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.getName().equals("org.eclipse.equinox.simpleconfigurator")) {
                                MaxelerECJInstaller.configFolder = pathname;
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

