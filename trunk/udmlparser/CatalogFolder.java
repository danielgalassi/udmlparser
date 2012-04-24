package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Catalog Folder Parser Class
 * @author danielgalassi@gmail.com
 *
 */
/*@XStreamAlias("PresentationCatalog")
*/
public class CatalogFolder {

//	@XStreamAlias("PresentationCatalogID")
	private String			sCatFolderID;
//	@XStreamAlias("PresentationCatalogName")
	private String			sCatFolderName;
//	@XStreamAlias("PresentationCatalogAlias")
	private String[]		saCatFolderAliases = null;
//	@XStreamAlias("PresentationCatalogMappingID")
	private String			sCatFolderMappingID;
//	@XStreamAlias("PresentationTableIDList")
	private Vector <String>	vEntityFolderID = null;

	public CatalogFolder(String sDeclareStmt,
						 String sCatFolder,
						 BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		//finds custom icons in Subject Areas
		int iICONIDX = sTrimmedDS.indexOf(" ICON INDEX ");
		sCatFolderID = sTrimmedDS.substring(sCatFolder.length(), iIndexAS).
												trim().replaceAll("\"", "");
		if (iICONIDX != -1)
			sCatFolderName = sTrimmedDS.substring(iIndexAS+4, iICONIDX).
										trim().replaceAll("\"", "");
		else
			sCatFolderName = sTrimmedDS.substring(iIndexAS+4).
										trim().replaceAll("\"", "");
		try {
			//SUBJECT AREA
			line = brUDML.readLine().trim().replaceAll("\"", "");

			if (line.indexOf("SUBJECT AREA ") != -1)
				sCatFolderMappingID = line.substring(line.
												indexOf("SUBJECT AREA ")+13).
												trim().replaceAll("\"", "");

			//ENTITY FOLDERS LIST
			line = brUDML.readLine().trim().replaceAll("\"", "");
			if (line.indexOf("ENTITY FOLDERS (") != -1) {
				vEntityFolderID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					vEntityFolderID.add(line.substring(0,line.length()-1));
				} while (line.charAt(line.length()-1) != ')');
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES,
			//RECOVERING ALIASES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
				if (line.indexOf("ALIASES (") != -1)
					saCatFolderAliases = line.substring(line.
							indexOf("ALIASES (")+9, line.lastIndexOf(")")).
							trim().replaceAll("\"", "").split(",");
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
		Node nPresentationCatalogID = xmldoc.createTextNode(sCatFolderID);
		Node nPresentationCatalogName = xmldoc.createTextNode(sCatFolderName);
		Node nCatalogFolderMappingID = xmldoc.createTextNode(sCatFolderMappingID);

		Element ePresentationCatalog = xmldoc.createElement("PresentationCatalog");
		Element ePresentationCatalogID = xmldoc.createElement("PresentationCatalogID");
		Element ePresentationCatalogName = xmldoc.createElement("PresentationCatalogName");
		Element eCatalogFolderMappingID = xmldoc.createElement("PresentationCatalogMappingID");

		ePresentationCatalogID.appendChild(nPresentationCatalogID);
		ePresentationCatalogName.appendChild(nPresentationCatalogName);
		eCatalogFolderMappingID.appendChild(nCatalogFolderMappingID);

		ePresentationCatalog.appendChild(ePresentationCatalogID);
		ePresentationCatalog.appendChild(ePresentationCatalogName);
		ePresentationCatalog.appendChild(eCatalogFolderMappingID);

		Element eCatalogFolderAliasList = xmldoc.createElement("PresentationCatalogAliasList");
		Element eCatalogFolderAlias = null;
		Node nCatalogFolderAlias = null;

		if(saCatFolderAliases != null)
			for (int i=0; i< saCatFolderAliases.length; i++) {
				eCatalogFolderAlias = xmldoc.createElement("PresentationCatalogAlias");
				nCatalogFolderAlias = xmldoc.createTextNode(saCatFolderAliases[i]);
				eCatalogFolderAlias.appendChild(nCatalogFolderAlias);
				eCatalogFolderAliasList.appendChild(eCatalogFolderAlias);
			}

		ePresentationCatalog.appendChild(eCatalogFolderAliasList);

		Element ePresentationFolderList = xmldoc.createElement("PresentationTableIDList");
		Element ePresentationFolder = null;
		Node nPresentationFolder = null;

		if(vEntityFolderID != null)
			for (int i=0; i< vEntityFolderID.size(); i++) {
				ePresentationFolder = xmldoc.createElement("PresentationTableID");
				nPresentationFolder = xmldoc.createTextNode(vEntityFolderID.get(i));
				ePresentationFolder.appendChild(nPresentationFolder);
				ePresentationFolderList.appendChild(ePresentationFolder);
			}

		ePresentationCatalog.appendChild(ePresentationFolderList);
		return ePresentationCatalog;
	}

//	public void toXML () {
//		XStream x = new XStream();
//		x.autodetectAnnotations(true);
//		System.out.println(x.toXML(this));
//	}
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
