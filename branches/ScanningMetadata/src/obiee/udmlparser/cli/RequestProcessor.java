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
 * @author danielgalassi@gmail.com
 *
 */
public class RequestProcessor {

	private static final Logger logger = LogManager.getLogger(RequestProcessor.class.getName());

	private Options options = new Options();
	private CommandLine cli = null;

	public RequestProcessor (String[] args) throws Exception {
		createOptions();
		parseCommandLine(args);
		try {
			validatingOptions();
		} catch (Exception e) {
			logger.fatal("{} thrown while processing command line arguments ({})", e.getClass().getCanonicalName(), e.getMessage());
			displayUsage();
			throw new ParseException("Invalid request");
		}
	}

	private void displayUsage() {
		org.apache.commons.cli.HelpFormatter help = new HelpFormatter();
		help.printHelp("udmlparser", options);
	}

	private void validatingOptions() throws Exception {
		if (cli.hasOption("udmltgt") && !cli.hasOption("udmlxsl")) {
			throw new MissingOptionException("Transformation requested without XSL stylesheet");
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
		options.addOption("c", "cmd", true, "invokes a bundled utility such as the Bus Matrix Generator");
		options.addOption("?", "help", false, "display usage options");
		options.addOption("h", "help", false, "display usage options");
	}

	public Request getRequest() {
		Request request = new Request();
		if (cli.hasOption("cmd")) {
			logger.info("Bundled App invoked");
			request.invokeBusMatrix(cli.getOptionValue("cmd"));
		}
		if (cli.hasOption("udml")) {
			request.setArg("udml", cli.getOptionValue("udml"));
		}
		if (cli.hasOption("rpdxml")) {
			request.setArg("rpdxml", cli.getOptionValue("rpdxml"));
		}
		if (cli.hasOption("udmlxsl")) {
			request.setArg("stylesheet", cli.getOptionValue("udmlxsl"));
		}
		if (cli.hasOption("udmltgt")) {
			request.setArg("target", cli.getOptionValue("udmltgt"));
		}
		return request;
	}
}
