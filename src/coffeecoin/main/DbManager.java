package coffeecoin.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Contains methods common to all DBManager classes
 * @author 
 */
public abstract class DbManager {

	public synchronized byte[] getCurrentDb(String filename) throws IOException {
		File db = new File(filename);
		FileInputStream fis = new FileInputStream(db);
		byte[] bFile = new byte[(int) db.length()];
		int offset = 0;
		int numread = 0;
		while(offset < bFile.length && (numread=fis.read(bFile, offset, bFile.length-offset)) >= 0) {
            offset += numread;
		}
        if (offset < bFile.length) {
        	fis.close();
            throw new IOException("Could not completely read file "+db.getName());
        }
		fis.close();
		System.gc();
		return bFile;
	}
	
}
