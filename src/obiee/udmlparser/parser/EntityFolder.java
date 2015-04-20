package obiee.udmlparser.parser;

import java.util.Vector;

import metadata.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Entity Folder Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class EntityFolder implements UDMLObject {

	private String			presentationTableID;
	private String			presentationTableName;
	private String			presentationTableMappingID;
	private String			displayName;
	private String			description;
	private Vector <String>	folderAttributeIDs = null;
	private String[]		aliases = null;

	public EntityFolder (String declare, String entityFolder, Repository udml) {
		String line;
		String header = declare.trim();
		int indexAS = header.indexOf(" AS ");
		presentationTableID = header.substring(entityFolder.length(), indexAS).trim().replaceAll("\"", "");
		if (header.indexOf(" ENTITY ") != -1 && header.indexOf(" ENTITY ") != header.lastIndexOf(" ENTITY ")) {
			presentationTableName = header.substring(indexAS + 4, header.indexOf(" ENTITY ", indexAS)).trim().replaceAll("\"", "");
			presentationTableMappingID = header.substring(header.indexOf(" ENTITY ", indexAS + 4) + 8).trim().replaceAll("\"", "");
		}
		else {
			presentationTableName = header.substring(indexAS + 4).trim().replaceAll("\"", "");
			presentationTableMappingID = "";
		}

		//FOLDER ATTRIBUTES LIST
		line = udml.nextLine().trim().replaceAll("\"", "");
		if (line.contains("FOLDER ATTRIBUTES ")) {
			folderAttributeIDs = new Vector<String>();
			do {
				line = udml.nextLine().trim().replaceAll("\"", "");
				folderAttributeIDs.add(line.substring(0,line.length()-1));
			} while (!line.endsWith(")"));
		}

		while (!line.contains("PRIVILEGES ") && !line.endsWith(";") && udml.hasNextLine()) {
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
				while (!(line.contains("PRIVILEGES ") && line.endsWith(";")) && !line.contains(descriptionStops) && udml.hasNextLine()) {
					line = udml.nextLine().trim();

					description += "\n";
					description += line.trim().replaceAll(descriptionStops, "").replaceAll("\"", "");
				}
			}

		}
	}

	/**
	 * Entity Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (presentationTableID == null) {
			presentationTableID = "";
		}
		Node nPresentationTableID = xmldoc.createTextNode(presentationTableID);

		if (presentationTableName == null) {
			presentationTableName = "";
		}
		Node nPresentationTableName = xmldoc.createTextNode(presentationTableName);

		if (presentationTableMappingID == null) {
			presentationTableMappingID = "";
		}
		Node nPresentationTableMappingID = xmldoc.createTextNode(presentationTableMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes
		if (displayName == null) {
			displayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(displayName);
		if (description == null) {
			description = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(description);

		Element ePresentationTable = xmldoc.createElement("PresentationTable");
		Element ePresentationTableID = xmldoc.createElement("PresentationTableID");
		Element ePresentationTableName = xmldoc.createElement("PresentationTableName");
		Element ePresentationTableMappingID = xmldoc.createElement("PresentationTableMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");

		ePresentationTableID.appendChild(nPresentationTableID);
		ePresentationTableName.appendChild(nPresentationTableName);
		ePresentationTableMappingID.appendChild(nPresentationTableMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationTable.appendChild(ePresentationTableID);
		ePresentationTable.appendChild(ePresentationTableName);
		ePresentationTable.appendChild(ePresentationTableMappingID);
		ePresentationTable.appendChild(ePresentationDisplayName);
		ePresentationTable.appendChild(ePresentationDescription);

		Element ePresentationTableAliasList = xmldoc.createElement("PresentationTableAliasList");
		Element ePresentationTableAlias = null;
		Node nCatalogFolderAlias = null;

		if(aliases != null)
			for (int i=0; i< aliases.length; i++) {
				ePresentationTableAlias = xmldoc.createElement("PresentationTableAlias");
				if (aliases[i] == null) {
					nCatalogFolderAlias = xmldoc.createTextNode("");
				} else {
					nCatalogFolderAlias = xmldoc.createTextNode(aliases[i]);
				}
				ePresentationTableAlias.appendChild(nCatalogFolderAlias);
				ePresentationTableAliasList.appendChild(ePresentationTableAlias);
			}

		ePresentationTable.appendChild(ePresentationTableAliasList);

		Element ePresentationAttributeIDList = xmldoc.createElement("PresentationAttributeIDList");
		Element ePresentationAttributeID = null;
		Node nPresentationAttributeID = null;

		if(folderAttributeIDs != null)
			for (String sFolderAttribID : folderAttributeIDs) {
				ePresentationAttributeID = xmldoc.createElement("PresentationAttributeID");
				if (sFolderAttribID == null)
					nPresentationAttributeID = xmldoc.createTextNode("");
				else
					nPresentationAttributeID = xmldoc.createTextNode(sFolderAttribID);

				ePresentationAttributeID.appendChild(nPresentationAttributeID);
				ePresentationAttributeIDList.appendChild(ePresentationAttributeID);
			}

		ePresentationTable.appendChild(ePresentationAttributeIDList);
		return ePresentationTable;
	}
}
/*
 * DECLARE ENTITY FOLDER <FQ Entity Folder Name> AS <Entity Folder Name> ENTITY  <FQ Logical Table Name>
 *  FOLDER ATTRIBUTES  (
 *  <FQ Entity Attribute Name>,
 *  <FQ Entity Attribute Name> )
 *  PRIVILEGES (<...>);
 */
