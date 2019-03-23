package mfi.photos.server.logic;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mfi.photos.util.CookieMap;
import mfi.photos.util.GalleryViewCache;

@Component
public class ContextListener {

	private static Logger logger = LoggerFactory.getLogger(ContextListener.class);

	@PostConstruct
	public void contextInitialized() {

		logger.info("Context initializing...");

		Processor processor = new Processor();
		CookieMap.getInstance().loadFrom(processor.getApplicationProperties());
		Gson gson = new GsonBuilder().create();
		String jsonDir = processor.lookupJsonDir(processor.getApplicationProperties());
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		logger.info("Context initialized.");
	}

	@PreDestroy
	public void contextDestroyed() {

		logger.info("Context destroying...");
		try {
			Processor processor = new Processor();
			CookieMap.getInstance().saveTo(processor.getApplicationProperties());
		} catch (Exception e) {
			logger.error("exception destroying context", e);
		}

		logger.info("Context destroyed.");
	}

}
