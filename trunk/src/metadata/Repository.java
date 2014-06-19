/**
 * 
 */
package metadata;

import java.io.File;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author danielgalassi@gmail.com
 *
 */
public class Repository {

	private static final Logger logger = LogManager.getLogger(Repository.class.getName());

	private Scanner udml = null;

	public Repository(String input) {
		try {
			File udmlExtract = new File(input);
			udml = new Scanner(udmlExtract, "UTF-8");
		} catch (Exception e) {
			logger.fatal("A repository could not be created. {} thrown while creating a representation of a repository file", e.getClass().getCanonicalName());
		}
	}
	
	public String nextLine() {
		return udml.nextLine();
	}
	
	public boolean isValid() {
		boolean isUDML = false;
		if (!(udml==null) && hasNextLine()) {
			isUDML = nextLine().startsWith("DECLARE ");
		}
		logger.info("File contains UDML code = {}", isUDML);
		return isUDML;
	}

	public boolean hasNextLine() {
		return udml.hasNextLine();
	}

	public void close() {
		if (!(udml == null)) {
			udml.close();
		}
	}
}
