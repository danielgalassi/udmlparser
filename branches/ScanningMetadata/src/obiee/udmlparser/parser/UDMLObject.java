/**
 * 
 */
package obiee.udmlparser.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author danielgalassi
 *
 */
public interface UDMLObject {

	public Element serialize(Document doc);
}
