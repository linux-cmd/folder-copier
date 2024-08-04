import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class FolderCopierFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textFieldSource;
    private JTextField textFieldPrefix;
    private List<JTextField> destinationFields;
    private JPanel destinationPanel;

    // Preference key to remember user choice
    private static final String PREF_KEY_DO_NOT_ASK_AGAIN = "doNotAskAgain";
    private Preferences prefs;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FolderCopierFrame frame = new FolderCopierFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public FolderCopierFrame() {
        prefs = Preferences.userNodeForPackage(FolderCopierFrame.class);
        destinationFields = new ArrayList<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 550);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JTextArea txtrSource = new JTextArea();
        txtrSource.setBackground(new Color(240, 240, 240));
        txtrSource.setBounds(10, 11, 414, 30);
        txtrSource.setRows(1);
        txtrSource.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 14));
        txtrSource.setText("Source Folder Path:");
        contentPane.add(txtrSource);

        textFieldSource = new JTextField();
        textFieldSource.setBounds(10, 42, 300, 30);
        contentPane.add(textFieldSource);
        textFieldSource.setColumns(10);

        JButton btnBrowseSource = new JButton("Browse");
        btnBrowseSource.setBounds(320, 42, 100, 30);
        contentPane.add(btnBrowseSource);

        btnBrowseSource.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textFieldSource.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        destinationPanel = new JPanel();
        destinationPanel.setBounds(10, 82, 560, 250);
        destinationPanel.setLayout(null);
        contentPane.add(destinationPanel);

        addDestinationField();

        JButton btnAddDestination = new JButton("Add Destination");
        btnAddDestination.setBounds(10, 334, 150, 30);
        contentPane.add(btnAddDestination);

        btnAddDestination.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addDestinationField();
            }
        });

        JTextArea txtrPrefix = new JTextArea();
        txtrPrefix.setBackground(new Color(240, 240, 240));
        txtrPrefix.setBounds(10, 374, 414, 30);
        txtrPrefix.setRows(1);
        txtrPrefix.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 14));
        txtrPrefix.setText("File Prefix:");
        contentPane.add(txtrPrefix);

        textFieldPrefix = new JTextField();
        textFieldPrefix.setBounds(10, 405, 300, 30);
        contentPane.add(textFieldPrefix);
        textFieldPrefix.setColumns(10);

        JButton btnStartCopy = new JButton("Start Copy");
        btnStartCopy.setBounds(10, 446, 150, 30);
        contentPane.add(btnStartCopy);

        btnStartCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String srcPath = textFieldSource.getText();
                String prefix = textFieldPrefix.getText();

                if (srcPath.isEmpty() || prefix.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                    return;
                }

                // Collect all destination paths
                List<String> destPaths = new ArrayList<>();
                for (JTextField destField : destinationFields) {
                    String destPath = destField.getText();
                    if (!destPath.isEmpty()) {
                        destPaths.add(destPath);
                    }
                }

                if (destPaths.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please provide at least one destination path.");
                    return;
                }

                // Prompt for the new folder name for each destination
                String newFolderName = JOptionPane.showInputDialog("Enter the name for the new folder:");
                if (newFolderName == null || newFolderName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "New folder name cannot be empty.");
                    return;
                }

                File srcDir = new File(srcPath);

                if (!srcDir.exists()) {
                    JOptionPane.showMessageDialog(null, "Source directory does not exist.");
                    return;
                }

                FolderCopier folderCopier = new FolderCopier();
                boolean overwrite = false;
                boolean hasConflictingFiles = false;

                for (String destPath : destPaths) {
                    File destDir = new File(destPath, newFolderName); // Create new folder in destination
                    hasConflictingFiles |= folderCopier.checkForConflictingFiles(srcDir, destDir, prefix);
                }

                if (hasConflictingFiles) {
                    int result = JOptionPane.showConfirmDialog(null, 
                        "Some files have the same name and will be overwritten. Continue with operation?", 
                        "Warning", 
                        JOptionPane.YES_NO_OPTION);

                    if (result == JOptionPane.YES_OPTION) {
                        overwrite = true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Operation canceled.");
                        return;
                    }
                }

                try {
                    for (String destPath : destPaths) {
                        File destDir = new File(destPath, newFolderName);
                        folderCopier.copyDir(srcDir, destDir, prefix, overwrite);
                    }
                    JOptionPane.showMessageDialog(null, "Copied successfully.");

                    if (!prefs.getBoolean(PREF_KEY_DO_NOT_ASK_AGAIN, false)) {
                        JPanel panel = new JPanel();
                        JCheckBox checkBox = new JCheckBox("Do not ask again");
                        panel.add(checkBox);

                        int deleteOption = JOptionPane.showConfirmDialog(null, 
                            new Object[] { 
                                "Would you like to delete the copied folder now?", 
                                checkBox 
                            }, 
                            "Delete Copied Folder", 
                            JOptionPane.YES_NO_OPTION);

                        if (checkBox.isSelected()) {
                            prefs.putBoolean(PREF_KEY_DO_NOT_ASK_AGAIN, true);
                        }

                        if (deleteOption == JOptionPane.YES_OPTION) {
                            for (String destPath : destPaths) {
                                File destDir = new File(destPath, newFolderName);
                                boolean isDeleted = folderCopier.deleteDir(destDir);
                                if (isDeleted) {
                                    JOptionPane.showMessageDialog(null, "Copied folder deleted successfully.");
                                } else {
                                    JOptionPane.showMessageDialog(null, "Failed to delete the copied folder.");
                                }
                            }
                        }
                    } else {
                        int deleteOption = JOptionPane.showConfirmDialog(null, 
                            "Would you like to delete the copied folder now?", 
                            "Delete Copied Folder", 
                            JOptionPane.YES_NO_OPTION);

                        if (deleteOption == JOptionPane.YES_OPTION) {
                            for (String destPath : destPaths) {
                                File destDir = new File(destPath, newFolderName);
                                boolean isDeleted = folderCopier.deleteDir(destDir);
                                if (isDeleted) {
                                    JOptionPane.showMessageDialog(null, "Copied folder deleted successfully.");
                                } else {
                                    JOptionPane.showMessageDialog(null, "Failed to delete the copied folder.");
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "An error occurred: " + ex.getMessage());
                }
            }
        });
    }

    private void addDestinationField() {
        int yOffset = destinationFields.size() * 70; // Increased space between destination bars

        JTextArea txtrDestination = new JTextArea();
        txtrDestination.setBackground(new Color(240, 240, 240));
        txtrDestination.setBounds(10, yOffset, 414, 30);
        txtrDestination.setRows(1);
        txtrDestination.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 14));
        txtrDestination.setText("Destination Folder Path:");
        destinationPanel.add(txtrDestination);

        JTextField textFieldDestination = new JTextField();
        textFieldDestination.setBounds(10, yOffset + 30, 300, 30);
        destinationPanel.add(textFieldDestination);
        destinationFields.add(textFieldDestination);

        JButton btnBrowseDestination = new JButton("Browse");
        btnBrowseDestination.setBounds(320, yOffset + 30, 100, 30);
        destinationPanel.add(btnBrowseDestination);

        btnBrowseDestination.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textFieldDestination.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        destinationPanel.revalidate();
        destinationPanel.repaint();
    }

    class FolderCopier {

        public void copyDir(File src, File dest, String prefix, boolean overwrite) throws IOException {
            if (src.isDirectory()) {
                if (!dest.exists()) {
                    dest.mkdir();
                }

                String[] files = src.list();
                if (files != null) {
                    for (String fileName : files) {
                        File srcFile = new File(src, fileName);
                        File destFile = new File(dest, prefix + fileName);

                        if (srcFile.isDirectory()) {
                            copyDir(srcFile, new File(dest, fileName), prefix, overwrite);
                        } else {
                            if (destFile.exists() && !overwrite) {
                                continue; // Skip existing files if not overwriting
                            }
                            fileCopy(srcFile, destFile);
                        }
                    }
                }
            } else {
                fileCopy(src, dest);
            }
        }

        private void fileCopy(File src, File dest) throws IOException {
            try (InputStream in = new FileInputStream(src);
                 OutputStream out = new FileOutputStream(dest)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }

        public boolean deleteDir(File dir) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteDir(file);
                    }
                }
            }
            return dir.delete();
        }

        public boolean checkForConflictingFiles(File srcDir, File destDir, String prefix) {
            String[] files = srcDir.list();
            if (files != null) {
                for (String fileName : files) {
                    File destFile = new File(destDir, prefix + fileName);
                    if (destFile.exists()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
