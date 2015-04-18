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
	private String			presentationDisplayName;
	private String			presentationDescription;
	private Vector <String>	folderAttributeIDs = null;
	private String[]		presentationTableAliases = null;

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
			} while (line.charAt(line.length()-1) != ')');
		}

		while (!line.contains(";") && udml.hasNextLine()) {
			line = udml.nextLine().trim().replaceAll("\"", "");
			//ALIASES
			if (line.contains("ALIASES (")) {
				presentationTableAliases = line.substring(line.indexOf("ALIASES (")+9, line.lastIndexOf(")")).trim().replaceAll("\"", "").split(",");
			}

			if (line.contains("DISPLAY NAME ")) {
				presentationDisplayName = line.trim().substring(line.indexOf("DISPLAY NAME ")+13, line.lastIndexOf(" ON")).trim().replaceAll("\"", "");
			}

			if (line.contains("DESCRIPTION ")) {
				//some UDML versions no longer use brackets...
				int descriptionStarts;
				String descriptionStops;
				if (line.contains("{")) {
					descriptionStarts = line.indexOf("{")+1;
					descriptionStops = "}";
				}
				else {
					descriptionStarts = line.indexOf("DESCRIPTION ") + 12;
					descriptionStops = " TRUE";
				}
				int length = line.length();
				presentationDescription = line.substring( descriptionStarts, length).replaceAll(descriptionStops, "").replaceAll("\"", "").trim();
				//LARGE TEXT
				while (!line.contains(descriptionStops) && udml.hasNextLine()) {
					line = udml.nextLine().trim();
					presentationDescription += "\n";
					presentationDescription += line.trim().replaceAll(descriptionStops, "").replaceAll("\"", "");
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
		if (presentationDisplayName == null) {
			presentationDisplayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(presentationDisplayName);
		if (presentationDescription == null) {
			presentationDescription = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(presentationDescription);

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

		if(presentationTableAliases != null)
			for (int i=0; i< presentationTableAliases.length; i++) {
				ePresentationTableAlias = xmldoc.createElement("PresentationTableAlias");
				if (presentationTableAliases[i] == null) {
					nCatalogFolderAlias = xmldoc.createTextNode("");
				} else {
					nCatalogFolderAlias = xmldoc.createTextNode(presentationTableAliases[i]);
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
