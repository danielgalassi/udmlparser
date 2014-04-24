/**
 * 
 */
package obiee.udmlparser.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * Requests represent a specific call from a command line interface.
 * @author danielgalassi@gmail.com
 *
 */
public class Request {

	/** A set of (key,value) pairs representing all (no flags) attributes*/
	private Map<String, String> request = new HashMap<String, String>();
	private boolean isBusMatrixInvoked = false;
	private boolean isTransformationInvoked = false;

	public void invokeTransformation(boolean invoked) {
		isTransformationInvoked = invoked;
	}

	public boolean isTransformationInvoked() {
		return isTransformationInvoked;
	}

	public void invokeBusMatrix(String value) {
		isBusMatrixInvoked = (value.equals("busmatrix"));
	}

	public boolean isBusMatrixInvoked() {
		return isBusMatrixInvoked;
	}

	public void setArg(String key, String value) {
		request.put(key, value);
	}

	public String getArg(String key) {
		String value = "";
		if (request.containsKey(key)) {
			value = request.get(key);
		}
		return value;
	}
}