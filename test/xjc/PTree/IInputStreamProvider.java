/**
 * 
 */
package xjc.PTree;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Admin
 *
 */
public interface IInputStreamProvider {

	public InputStream getInputStream() throws IOException;

	public void start(InputStream is) throws IOException;
}
