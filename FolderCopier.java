import java.io.File;
import java.io.FileInputStream;
	import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class FolderCopier {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("What is the path of the folder that you would wish to copy?");
        String folderPath = scanner.nextLine();

        System.out.println("What do you want the name of the copied folder to be?");
        String folderName = scanner.nextLine();

        System.out.println("What is the path of the folder that you would like to copy in? (Leave blank if same path as copied folder)");
        String copyPath = scanner.nextLine();

        System.out.println("Enter the prefix you want for the copied files:");
        String prefix = scanner.nextLine();

        File srcDir = new File(folderPath);
        File destDir;
        
        if (copyPath.isBlank()) {
            destDir = new File(srcDir.getParent() + "/" + folderName);
        } else {
            destDir = new File(copyPath + "/" + folderName);
        }

        if (!srcDir.exists()) {
            System.out.println("Source directory does not exist.");
        } else {
            boolean overwrite = false;
            boolean hasConflictingFiles = false;

            if (destDir.exists()) {
                hasConflictingFiles = checkForConflictingFiles(srcDir, destDir, prefix);

                if (hasConflictingFiles) {
                    System.out.println("Some files have the same name and will be overwritten. Continue with operation? Y/N");
                    String response = scanner.nextLine().toLowerCase();

                    if (response.equals("y")) {
                        overwrite = true;
                    } else {
                        System.out.println("Operation canceled.");
                        scanner.close();
                        return;
                    }
                }
            }

            try {
                FolderCopier folderCopier = new FolderCopier();
                folderCopier.copyDir(srcDir, destDir, prefix, overwrite);
                System.out.println("Copied successfully");

                System.out.println("Would you like to delete the copied folder now? Y/N");
                String deleteFolder = scanner.nextLine().toLowerCase();

                if (deleteFolder.equals("y")) {
                    boolean isDeleted = folderCopier.deleteDir(destDir);
                    if (isDeleted) {
                        System.out.println("Copied folder deleted successfully.");
                    } else {
                        System.out.println("Failed to delete the copied folder. (Folder might already be deleted)");
                    }
                } else {
                    System.out.println("Program finished.");
                }
            } catch (IOException e) {
                System.err.println("An error occurred during the copying process: " + e.getMessage());
            } finally {
                scanner.close();
            }
        }
    }

    public void copyDir(File src, File dest, String prefix, boolean overwrite) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
                System.out.println("Directory copied from " + src + " to " + dest);
            }

            String[] files = src.list();
            if (files != null) {
                for (String fileName : files) {
                    File srcFile = new File(src, fileName);
                    File destFile;
                    if (srcFile.isDirectory()) {
                        destFile = new File(dest, fileName);
                    } else {
                        destFile = new File(dest, prefix + fileName);

                        if (destFile.exists() && !overwrite) {
                            System.out.println("File " + destFile.getName() + " already exists. Skipping.");
                            continue;
                        }
                    }
                    copyDir(srcFile, destFile, prefix, overwrite);
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
        System.out.println("File copied from " + src + " to " + dest);
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

    private static boolean checkForConflictingFiles(File srcDir, File destDir, String prefix) {
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
