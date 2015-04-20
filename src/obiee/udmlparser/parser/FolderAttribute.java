package obiee.udmlparser.parser;

import metadata.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Folder Attribute Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class FolderAttribute implements UDMLObject {

	private String		presentationColumnID;
	private String		presentationColumnName;
	private String		presentationColumnMappingID;
	private String		displayName;
	private String		description;
	private String[] 	aliases = null;


	public FolderAttribute (String declare, String presentationColumn, Repository udml) {
		String line;
		String header = declare.trim();
		int indexAS = header.indexOf(" AS ");
		int indexAttribute = header.indexOf(" LOGICAL ATTRIBUTE ");
		presentationColumnID = header.substring(presentationColumn.length(), indexAS).trim().replaceAll("\"", "");
		presentationColumnName = header.substring(indexAS+4, header.indexOf( " LOGICAL ATTRIBUTE ",indexAS)).trim().replaceAll("\"", "");

		if (header.indexOf(" OVERRIDE LOGICAL NAME") == -1) {
			presentationColumnMappingID = header.substring(indexAttribute+19).trim().replaceAll("\"", "");
		}
		else {
			presentationColumnMappingID = header.substring(indexAttribute+19, header.indexOf(" OVERRIDE LOGICAL NAME")).trim().replace("\"", "");
		}

		do {
			line = udml.nextLine().trim().replaceAll("\"", "");

			//ALIASES
			if (line.contains("ALIASES (")) {
				int aliasesBegins = line.indexOf("ALIASES (") + 9;
				int aliasesEnds = line.lastIndexOf(")") - 1;
				aliases = line.substring(aliasesBegins, aliasesEnds).trim().replaceAll("\"", "").split(",");
			}

			//DISPLAY NAME
			if (line.contentEquals("DISPLAY NAME ")) {
				if (line.contains("DISPLAY NAME ")) {
					int displayNameBegins = line.indexOf("DISPLAY NAME ") + 13;
					int displayNameEnds = line.lastIndexOf(" ON") - 1;
					displayName = line.trim().substring(displayNameBegins, displayNameEnds).trim().replaceAll("\"", "");
				}

			}

			//DESCRIPTION
			if (line.contains("DESCRIPTION ") || line.contains("CUSTOM DESCRIPTION ")) {
				int descriptionStarts;
				String descriptionStops;
				if (line.contains("{")) {
					descriptionStarts = line.indexOf("{") + 1;
					descriptionStops = "}";
				}
				else { //CUSTOM DESCRIPTION
					descriptionStarts = line.indexOf("DESCRIPTION ") + 12;
					descriptionStops = " TRUE";
				}
				int length = line.length();
				description = line.substring(descriptionStarts, length).replaceAll(descriptionStops, "").replaceAll("\"", "").trim();
				//LARGE TEXT
				while (!line.contains("PRIVILEGES ") && !line.endsWith(";") && !line.contains(descriptionStops) && udml.hasNextLine()) {
					line = udml.nextLine().trim();
					description += "\n";
					description += line.trim().replaceAll(descriptionStops, "").replaceAll("\"", "");
				}
			}
		} while (!line.contains("PRIVILEGES ") && !line.endsWith(";") && udml.hasNextLine());
	}

	/**
	 * Folder Attribute XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (presentationColumnID == null) {
			presentationColumnID = "";
		}
		Node nPresentationColumnID = xmldoc.createTextNode(presentationColumnID);
		if (presentationColumnName == null) {
			presentationColumnName = "";
		}
		Node nPresentationColumnName = xmldoc.createTextNode(presentationColumnName);
		if (presentationColumnMappingID == null) {
			presentationColumnMappingID = "";
		}
		Node nPresentationColumnMappingID = xmldoc.createTextNode(presentationColumnMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes
		if (displayName == null) {
			displayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(displayName);
		if (description == null) {
			description = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(description);

		Element ePresentationColumn = xmldoc.createElement("PresentationColumn");
		Element ePresentationColumnID = xmldoc.createElement("PresentationColumnID");
		Element ePresentationColumnName = xmldoc.createElement("PresentationColumnName");
		Element ePresentationColumnMappingID = xmldoc.createElement("PresentationColumnMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");

		ePresentationColumnID.appendChild(nPresentationColumnID);
		ePresentationColumnName.appendChild(nPresentationColumnName);
		ePresentationColumnMappingID.appendChild(nPresentationColumnMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationColumn.appendChild(ePresentationColumnID);
		ePresentationColumn.appendChild(ePresentationColumnName);
		ePresentationColumn.appendChild(ePresentationColumnMappingID);
		ePresentationColumn.appendChild(ePresentationDisplayName);
		ePresentationColumn.appendChild(ePresentationDescription);

		Element ePresentationColumnAliasList = xmldoc.createElement("PresentationColumnAliasList");
		Element ePresentationColumnAlias = null;
		Node nCatalogFolderAlias = null;

		if(aliases != null)
			for (String sPresColAlias : aliases) {
				ePresentationColumnAlias = xmldoc.createElement("PresentationColumnAlias");
				if (sPresColAlias == null)
					nCatalogFolderAlias = xmldoc.createTextNode("");
				else
					nCatalogFolderAlias = xmldoc.createTextNode(sPresColAlias);

				ePresentationColumnAlias.appendChild(nCatalogFolderAlias);
				ePresentationColumnAliasList.appendChild(ePresentationColumnAlias);
			}

		ePresentationColumn.appendChild(ePresentationColumnAliasList);
		return ePresentationColumn;
	}
}
/*
 * DECLARE FOLDER ATTRIBUTE <FQ Folder Attribute Name> AS <Folder Attribute Name> LOGICAL ATTRIBUTE  <FQ Logical Column Name>
 *  ALIASES (<Folder Attribute Alias>)
 *  PRIVILEGES ( <...>);
 */
