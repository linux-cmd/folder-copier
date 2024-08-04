

import java.io.File;
import java.nio.file.Files;

public class FileCopier {

	public static void main(String[] args) {
		
		
			File originalFile = new File("data1.txt");
			File newFile = new File("Backup.txt");
		
			try
			{
				Files.copy(originalFile.toPath(),newFile.toPath());
			}
			catch(Exception e)
			{
				System.out.println("Error");	
			}
		
		
	}

}
