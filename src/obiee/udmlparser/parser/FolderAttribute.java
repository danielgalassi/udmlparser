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
	private String		presentationDispayName;
	private String		presentationDescription;
	private String[] 	presentationColumnAliases = null;


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

		//ALIASES
		do {
			boolean keywordFound = false;
			line = udml.nextLine().trim().replaceAll("\"", "");
			if(line.indexOf("ALIASES (") != -1) {
				presentationColumnAliases = line.substring(
						line.indexOf("ALIASES (")+9, 
						line.lastIndexOf(")")).trim().replaceAll("\"", "").split(",");
				keywordFound = true;
			}

			//DISPLAY NAME
			if (line.indexOf("DISPLAY NAME ") != -1 && !keywordFound) {
				presentationDispayName = line.trim().substring(
						line.indexOf("DISPLAY NAME ")+13,
						line.lastIndexOf(" ON")).trim().replaceAll("\"", "");
				keywordFound = true;
			}

			//DESCRIPTION
			if (line.indexOf("DESCRIPTION") != -1 && !keywordFound) {
				presentationDescription = line.trim().substring(
						line.indexOf("{")+1,
						line.length()).trim().replaceAll("}", "");
				//LARGE TEXT
				while (line.indexOf("}") == -1){
					line = udml.nextLine().trim();
					presentationDescription += "\n";
					presentationDescription += line.trim().replaceAll("}", "");
					keywordFound = true;
				}
			}

		} while (line.indexOf("PRIVILEGES") == -1 && line.indexOf(";") == -1);
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
		if (presentationDispayName == null) {
			presentationDispayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(presentationDispayName);
		if (presentationDescription == null) {
			presentationDescription = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(presentationDescription);

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

		if(presentationColumnAliases != null)
			for (String sPresColAlias : presentationColumnAliases) {
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
