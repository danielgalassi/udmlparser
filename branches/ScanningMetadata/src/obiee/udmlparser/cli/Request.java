/**
 * 
 */
package obiee.udmlparser.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author danielgalassi@gmail.com
 *
 */
public class Request {

	private static final Logger logger = LogManager.getLogger(Request.class.getName());

	private Options options = new Options();
	private CommandLine cli = null;

	public Request (String[] args) throws Exception {

		createOptions();
		parseCommandLine(args);
		try {
		validatingOptions();
		} catch (Exception e) {
			logger.fatal("{} thrown while processing command line arguments ({})", e.getClass().getCanonicalName(), e.getMessage());
			org.apache.commons.cli.HelpFormatter help = new HelpFormatter();
			help.printHelp("udmlparser", options);
			throw new ParseException("Invalid request");
		}
		retrieveOptions();
	}

	private void retrieveOptions() {
		if (cli.hasOption("cmd")) {
			logger.info("Bundled App invoked");
		}

		if (cli.hasOption("batch")) {
			logger.info("Batch option invoked");
			System.out.println("batch!!");
			String batch = cli.getOptionValue("b");
			System.out.println(batch.equals(""));
		}
	}

	private void validatingOptions() throws Exception {
		if (cli.hasOption("udmltgt") && !cli.hasOption("udmlxsl")) {
			throw new MissingOptionException("Transformation requested without XSL stylesheet");
		}
		if (cli.hasOption("batch") && cli.getOptionValue("batch").equals("")) {
			throw new MissingArgumentException("Batch processing requested without providing the required configuration file");
		}
	}

	private void parseCommandLine(String[] args) {
		CommandLineParser parser = new GnuParser();
		try {
			cli = parser.parse(options, args);
		} catch (ParseException e) {
			logger.fatal("{} thrown while parsing command line options ({})", e.getClass().getCanonicalName(), e.getMessage());
		}
	}

	private void createOptions() {
		options.addOption("u", "udml", true, "a UDML file");
		options.addOption("x", "rpdxml", false, "the resulting XML file");
		options.addOption("s", "udmlxsl", true, "XSL stylesheet for (optional) transformation");
		options.addOption("t", "udmltgt", false, "the file resulting from the transformation");
		options.addOption("b", "batch", true, "batch processing configuration file, in case you need to parse multiple UDML files using a single command call");
		options.addOption("c", "cmd", true, "invokes a bundled utility such as the Bus Matrix Generator");
		options.addOption("?", "help", false, "display usage options");
		options.addOption("h", "help", false, "display usage options");
	}
}
