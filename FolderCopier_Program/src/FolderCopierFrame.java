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
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JScrollPane;

public class FolderCopierFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textFieldSource;
    private JTextField textFieldPrefix;
    private List<JTextField> destinationFields;
    private JPanel destinationPanel;

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
        setTitle("[Abhijay] - Folder Copier");
        destinationFields = new ArrayList<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 600); // Increased height to accommodate more fields
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

        // Create a scroll pane for the destination panel
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 82, 560, 250);
        contentPane.add(scrollPane);

        // Destination panel inside the scroll pane
        destinationPanel = new JPanel();
        destinationPanel.setLayout(null);
        scrollPane.setViewportView(destinationPanel);
        destinationPanel.setPreferredSize(new Dimension(550, 250)); // Initial size, will increase as fields are added

        addDestinationField(); // Initial destination field

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
        txtrPrefix.setText("File Prefix (Optional):");
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

                if (srcPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in the source path.");
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

                File srcDir = new File(srcPath);

                if (!srcDir.exists()) {
                    JOptionPane.showMessageDialog(null, "Source directory does not exist.");
                    return;
                }

                FolderCopier folderCopier = new FolderCopier();
                boolean overwrite = false;
                boolean hasConflictingFiles = false;

                for (String destPath : destPaths) {
                    File destDir = new File(destPath); // Use the entire path as the destination
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
                        File destDir = new File(destPath);
                        folderCopier.copyDir(srcDir, destDir, prefix, overwrite);
                    }
                    JOptionPane.showMessageDialog(null, "Copied successfully.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "An error occurred: " + ex.getMessage());
                }
            }
        });
    }

    private void addDestinationField() {
        int yOffset = destinationFields.size() * 70; // Increased spacing between fields
        destinationPanel.setPreferredSize(new Dimension(550, yOffset + 100));

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
            if (!dest.exists()) {
                dest.mkdirs(); // Create all necessary directories
            }

            if (src.isDirectory()) {
                String[] files = src.list();
                if (files != null) {
                    for (String fileName : files) {
                        File srcFile = new File(src, fileName);
                        File destFile = new File(dest, (prefix != null ? prefix : "") + fileName);

                        if (srcFile.isDirectory()) {
                            copyDir(srcFile, destFile, prefix, overwrite);
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

        public boolean checkForConflictingFiles(File srcDir, File destDir, String prefix) {
            String[] files = srcDir.list();
            if (files != null) {
                for (String fileName : files) {
                    File destFile = new File(destDir, (prefix != null ? prefix : "") + fileName);
                    if (destFile.exists()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
