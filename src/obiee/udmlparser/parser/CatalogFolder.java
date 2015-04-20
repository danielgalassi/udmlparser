package obiee.udmlparser.parser;

import java.util.Vector;

import metadata.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Catalog Folder Parser Class
 * @author danielgalassi@gmail.com
 *
 */
public class CatalogFolder implements UDMLObject {

	private String			catalogFolderID;
	private String			catalogFolderName;
	private String[]		aliases = null;
	private String			catalogFolderMappingID;
	private Vector <String>	entityFolderIDs = null;
	private String			displayName;
	private String			description;
	private String			implicitFactColumn;

	public CatalogFolder(String declare, String catalogFolder, Repository udml) {
		String line;
		String trimmedHeader = declare.trim();
		int asMarker = trimmedHeader.indexOf(" AS ");
		//finds custom icons in Subject Areas
		int iconMarker = trimmedHeader.indexOf(" ICON INDEX ");
		catalogFolderID = trimmedHeader.substring(catalogFolder.length(), asMarker).trim().replaceAll("\"", "");
		if (iconMarker != -1) {
			catalogFolderName = trimmedHeader.substring(asMarker+4, iconMarker).trim().replaceAll("\"", "");
		}
		else {
			catalogFolderName = trimmedHeader.substring(asMarker+4).trim().replaceAll("\"", "");
		}
		//SUBJECT AREA
		line = udml.nextLine().trim().replaceAll("\"", "");

		int subjectAreaMarker = line.indexOf("SUBJECT AREA ");
		if (subjectAreaMarker != -1) {
			catalogFolderMappingID = line.substring(subjectAreaMarker + 13).trim().replaceAll("\"", "");
		}

		//ENTITY FOLDERS LIST
		line = udml.nextLine().trim().replaceAll("\"", "");
		if (line.indexOf("ENTITY FOLDERS (") != -1) {
			entityFolderIDs = new Vector<String>();
			do {
				line = udml.nextLine().trim().replaceAll("\"", "");
				entityFolderIDs.add(line.substring(0,line.length()-1));
			} while (!line.endsWith(")"));
		}

		//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES,
		//RECOVERING ALIASES
		while (!line.contains("PRIVILEGES ") && !line.endsWith(";") && udml.hasNextLine()) {
			line = udml.nextLine().trim().replaceAll("\"", "");

			//DEFAULT FACT COLUMN
			if (line.contains("DEFAULT FACT COLUMN ")) {
				int implicitFactColumnBegins = line.indexOf("DEFAULT FACT COLUMN ") + 20;
				implicitFactColumn = line.substring(implicitFactColumnBegins, line.length()).trim().replaceAll("\"", "");
			}

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
	 * Catalog Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (catalogFolderID == null)
			catalogFolderID = "";
		Node nPresentationCatalogID = xmldoc.createTextNode(catalogFolderID);

		if (catalogFolderName == null)
			catalogFolderName = "";
		Node nPresentationCatalogName = xmldoc.createTextNode(catalogFolderName);

		if (catalogFolderMappingID == null)
			catalogFolderMappingID = "";
		Node nCatalogFolderMappingID = xmldoc.createTextNode(catalogFolderMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes

		if (displayName == null)
			displayName = "";
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(displayName);

		if (implicitFactColumn == null)
			implicitFactColumn = "";
		Node nImplicitFactColumn = xmldoc.createTextNode(implicitFactColumn);

		if (description == null)
			description = "";
		Node nPresentationColumnDescription = xmldoc.createTextNode(description);

		Element ePresentationCatalog = xmldoc.createElement("PresentationCatalog");
		Element ePresentationCatalogID = xmldoc.createElement("PresentationCatalogID");
		Element ePresentationCatalogName = xmldoc.createElement("PresentationCatalogName");
		Element eCatalogFolderMappingID = xmldoc.createElement("PresentationCatalogMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element eImplicitFactColumn = xmldoc.createElement("ImplicitFactColumn");
		Element ePresentationDescription = xmldoc.createElement("description");

		ePresentationCatalogID.appendChild(nPresentationCatalogID);
		ePresentationCatalogName.appendChild(nPresentationCatalogName);
		eCatalogFolderMappingID.appendChild(nCatalogFolderMappingID);
		eImplicitFactColumn.appendChild(nImplicitFactColumn);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationCatalog.appendChild(ePresentationCatalogID);
		ePresentationCatalog.appendChild(ePresentationCatalogName);
		ePresentationCatalog.appendChild(eCatalogFolderMappingID);
		ePresentationCatalog.appendChild(ePresentationDisplayName);
		ePresentationCatalog.appendChild(eImplicitFactColumn);
		ePresentationCatalog.appendChild(ePresentationDescription);

		Element eCatalogFolderAliasList = xmldoc.createElement("PresentationCatalogAliasList");
		Element eCatalogFolderAlias = null;
		Node nCatalogFolderAlias = null;

		if(aliases != null) {
			for (String sCatFolderAlias : aliases) {
				eCatalogFolderAlias = xmldoc.createElement("PresentationCatalogAlias");
				if (sCatFolderAlias == null)
					nCatalogFolderAlias = xmldoc.createTextNode("");
				else
					nCatalogFolderAlias = xmldoc.createTextNode(sCatFolderAlias);

				eCatalogFolderAlias.appendChild(nCatalogFolderAlias);
				eCatalogFolderAliasList.appendChild(eCatalogFolderAlias);
			}
		}

		ePresentationCatalog.appendChild(eCatalogFolderAliasList);

		Element ePresentationFolderList = xmldoc.createElement("PresentationTableIDList");
		Element ePresentationFolder = null;
		Node nPresentationFolder = null;

		if(entityFolderIDs != null)
			for (String entityFolderID : entityFolderIDs) {
				ePresentationFolder = xmldoc.createElement("PresentationTableID");
				if (entityFolderID == null)
					nPresentationFolder = xmldoc.createTextNode("");
				else
					nPresentationFolder = xmldoc.createTextNode(entityFolderID);

				ePresentationFolder.appendChild(nPresentationFolder);
				ePresentationFolderList.appendChild(ePresentationFolder);
			}

		ePresentationCatalog.appendChild(ePresentationFolderList);
		return ePresentationCatalog;
	}

}
/*
 * DECLARE CATALOG FOLDER <FQ Catalog Name> AS <Catalog Name>
 * SUBJECT AREA <FQ Subject Area Name>
 * ENTITY FOLDERS (
 * <FQ Entity Folder Name>,
 * <FQ Entity Folder Name>)
 * ALIASES (<Catalog Folder Alias>)
 * DESCRIPTION {<...>}
 * PRIVILEGES (<...>);
 */
