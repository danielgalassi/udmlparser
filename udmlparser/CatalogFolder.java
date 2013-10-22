package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Catalog Folder Parser Class
 * @author danielgalassi@gmail.com
 *
 */
public class CatalogFolder {

	private String			catalogFolderID;
	private String			catalogFolderName;
	private String[]		catalogFolderAliases = null;
	private String			catalogFolderMappingID;
	private Vector <String>	entityFolderIDs = null;

	private String			presentationDispayName;
	private String			presentationDescription;

	public CatalogFolder(String declare, String catalogFolder, BufferedReader udml) {
		String line;
		String sTrimmedDS = declare.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		//finds custom icons in Subject Areas
		int iICONIDX = sTrimmedDS.indexOf(" ICON INDEX ");
		catalogFolderID = sTrimmedDS.substring(catalogFolder.length(), iIndexAS).trim().replaceAll("\"", "");
		if (iICONIDX != -1) {
			catalogFolderName = sTrimmedDS.substring(iIndexAS+4, iICONIDX).trim().replaceAll("\"", "");
		}
		else {
			catalogFolderName = sTrimmedDS.substring(iIndexAS+4).trim().replaceAll("\"", "");
		}
		try {
			//SUBJECT AREA
			line = udml.readLine().trim().replaceAll("\"", "");

			if (line.indexOf("SUBJECT AREA ") != -1)
				catalogFolderMappingID = line.substring(line.
						indexOf("SUBJECT AREA ")+13).
						trim().replaceAll("\"", "");

			//ENTITY FOLDERS LIST
			line = udml.readLine().trim().replaceAll("\"", "");
			if (line.indexOf("ENTITY FOLDERS (") != -1) {
				entityFolderIDs = new Vector<String>();
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
					entityFolderIDs.add(line.substring(0,line.length()-1));
				} while (line.charAt(line.length()-1) != ')');
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES,
			//RECOVERING ALIASES
			do {
				line = udml.readLine().trim().replaceAll("\"", "");
				if (line.indexOf("ALIASES (") != -1)
					catalogFolderAliases = line.substring(line.
							indexOf("ALIASES (")+9, line.lastIndexOf(")")).
							trim().replaceAll("\"", "").split(",");

				//DISPLAY NAME
				if (line.indexOf("DISPLAY NAME ") != -1){
					int i1 = line.indexOf("DISPLAY NAME ")+13;
					int i2 = line.lastIndexOf(" ON");
					presentationDispayName = line.trim().substring(
							i1,
							i2).
							trim().replaceAll("\"", "");
				}

				//DESCRIPTION
				if (line.indexOf("DESCRIPTION ") != -1){
					presentationDescription = line.trim().substring(
							line.indexOf("{")+1,
							line.length()).
							trim().replaceAll("}", "");
					//LARGE TEXT
					while (line.indexOf("}") == -1){
						line = udml.readLine().trim();
						presentationDescription += "\n";
						presentationDescription += line.trim().replaceAll("}", "");
					}
				}
			} while (line.indexOf(";") == -1);

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
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

		if (presentationDispayName == null)
			presentationDispayName = "";
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(presentationDispayName);

		if (presentationDescription == null)
			presentationDescription = "";
		Node nPresentationColumnDescription = xmldoc.createTextNode(presentationDescription);

		Element ePresentationCatalog = xmldoc.createElement("PresentationCatalog");
		Element ePresentationCatalogID = xmldoc.createElement("PresentationCatalogID");
		Element ePresentationCatalogName = xmldoc.createElement("PresentationCatalogName");
		Element eCatalogFolderMappingID = xmldoc.createElement("PresentationCatalogMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");

		ePresentationCatalogID.appendChild(nPresentationCatalogID);
		ePresentationCatalogName.appendChild(nPresentationCatalogName);
		eCatalogFolderMappingID.appendChild(nCatalogFolderMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationCatalog.appendChild(ePresentationCatalogID);
		ePresentationCatalog.appendChild(ePresentationCatalogName);
		ePresentationCatalog.appendChild(eCatalogFolderMappingID);
		ePresentationCatalog.appendChild(ePresentationDisplayName);
		ePresentationCatalog.appendChild(ePresentationDescription);

		Element eCatalogFolderAliasList = xmldoc.createElement("PresentationCatalogAliasList");
		Element eCatalogFolderAlias = null;
		Node nCatalogFolderAlias = null;

		if(catalogFolderAliases != null)
			for (String sCatFolderAlias : catalogFolderAliases) {
				eCatalogFolderAlias = xmldoc.createElement("PresentationCatalogAlias");
				if (sCatFolderAlias == null)
					nCatalogFolderAlias = xmldoc.createTextNode("");
				else
					nCatalogFolderAlias = xmldoc.createTextNode(sCatFolderAlias);

				eCatalogFolderAlias.appendChild(nCatalogFolderAlias);
				eCatalogFolderAliasList.appendChild(eCatalogFolderAlias);
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
