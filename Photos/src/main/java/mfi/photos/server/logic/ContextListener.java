package mfi.photos.server.logic;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mfi.photos.util.CookieMap;
import mfi.photos.util.GalleryViewCache;

public class ContextListener implements ServletContextListener {

	private static Logger logger = LoggerFactory.getLogger(ContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		logger.info("Context initializing...");

		Processor processor = new Processor();
		CookieMap.getInstance().loadFrom(processor.getApplicationProperties());
		Gson gson = new GsonBuilder().create();
		String jsonDir = processor.lookupJsonDir(processor.getApplicationProperties());
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		logger.info("Context initialized.");
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		logger.info("Context destroying...");
		try {
			Processor processor = new Processor();
			CookieMap.getInstance().saveTo(processor.getApplicationProperties());
		} catch (Exception e) {
			System.out.println(e);
		}

		logger.info("Context destroyed.");
	}

}
