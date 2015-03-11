/**
 * 
 */
package obiee.udmlparser.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles argument processing for command line requests
 * @author danielgalassi@gmail.com
 *
 */
public class RequestProcessor {

	private static final Logger logger = LogManager.getLogger(RequestProcessor.class.getName());

	/** Command line arguments used by the parser application*/
	private Options options = new Options();
	/** Command line representation for Apache Commons CLI */
	private CommandLine cli = null;

	/**
	 * Processes all arguments entered in the command line interface
	 * @param args command line arguments
	 * @throws Exception when command line arguments cannot be successfully parsed or represented
	 * @see <code>ParseException</code>
	 */
	public RequestProcessor (String[] args) throws Exception {
		createOptions();
		try {
			parseCommandLine(args);
			validatingOptions();
		} catch (Exception e) {
			logger.error("{} thrown while processing command line arguments ({})", e.getClass().getCanonicalName(), e.getMessage());
			throw new Exception("Command line request processing terminated");
		}
	}

	/**
	 * Displays help information
	 */
	private void displayUsage() {
		org.apache.commons.cli.HelpFormatter help = new HelpFormatter();
		help.printHelp("udmlparser", options);
	}

	/**
	 * Verifies that dependencies are met
	 * @throws Exception when argument dependencies are broken
	 * @see <code>MissingOptionException</code>
	 */
	private void validatingOptions() throws Exception {
		if (cli.hasOption("udmltgt") && !cli.hasOption("udmlxsl") && !cli.hasOption("cmd")) {
			throw new MissingOptionException("Transformation requested without XSL stylesheet");
		}
		if (cli.hasOption("help")) {
			displayUsage();
			throw new Exception("Arguments (if any) after 'help' not used");
		}
	}

	/**
	 * Creates an representation of the command line arguments
	 * @param args command line arguments
	 * @throws Exception when command line arguments are missing or incorrectly entered
	 * @see <code>ParseException</code> 
	 */
	private void parseCommandLine(String[] args) throws Exception {

		if (args.length == 0) {
			logger.fatal("No arguments found");
			throw new ParseException("Invalid request, no arguments found");
		}
		CommandLineParser parser = new GnuParser();
		try {
			cli = parser.parse(options, args);
		} catch (ParseException e) {
			logger.fatal("{} thrown while parsing command line arguments ({})", e.getClass().getCanonicalName(), e.getMessage());
		}
	}

	/**
	 * Adds all options evaluated by <code>MetadataExtract</code>
	 */
	private void createOptions() {
		options.addOption("u", "udml", true, "a UDML file");
		options.addOption("x", "rpdxml", false, "the resulting XML file, if left unspecified a random name will be set");
		options.addOption("s", "udmlxsl", true, "XSL stylesheet for (optional) transformation");
		options.addOption("t", "udmltgt", true, "the file resulting from the transformation, if left unspecified a random name will be set");
		options.addOption("c", "cmd", true, "invokes a bundled utility such as the Bus Matrix Generator");
		options.addOption("h", "help", false, "displays valid command line arguments");
	}

	/**
	 * Generates a request containing all (valid) retrieved arguments
	 * @return a representation of a list of command line arguments
	 */
	public Request getRequest() {
		Request request = new Request();
		if (cli.hasOption("cmd")) {
			logger.info("Bundled App invoked");
			request.setArg("matrixMode", cli.getOptionValue("cmd"));
		}
		if (cli.hasOption("udml")) {
			request.setArg("udml", cli.getOptionValue("udml"));
			if (!cli.hasOption("rpdxml")) {
				request.setArg("rpdxml", System.currentTimeMillis() + ".xml");
			}
		}
		if (cli.hasOption("rpdxml")) {
			request.setArg("rpdxml", cli.getOptionValue("rpdxml"));
		}
		if (cli.hasOption("udmlxsl")) {
			request.setArg("stylesheet", cli.getOptionValue("udmlxsl"));
			request.invokeTransformation(true);
		}
		if (cli.hasOption("udmltgt")) {
			request.setArg("target", cli.getOptionValue("udmltgt"));
		}
		return request;
	}
}
