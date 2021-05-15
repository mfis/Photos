package mfi.photos.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

public class IOUtil {

	public static String readContentFromFileInClasspath(String name) {

		try {
			URL url = new IOUtil().getClass().getClassLoader().getResource(name);
			URLConnection resConn = url.openConnection();
			resConn.setUseCaches(false);
			InputStream in = resConn.getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(in, writer, "UTF-8");
			return writer.toString();

		} catch (IOException e) {
			throw new IllegalStateException("Error loading Resource " + name + ": ", e);
		}
	}

}
