package mfi.photos.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mfi.photos.util.GalleryViewCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ContextListener {

	@Autowired
	private Processor processor;

	private static Logger logger = LoggerFactory.getLogger(ContextListener.class);

	@PostConstruct
	public void contextInitialized() {
		logger.info("Context initializing...");
		GalleryViewCache.getInstance().refresh(processor.lookupJsonDir(), new GsonBuilder().create());
		logger.info("Context initialized.");
	}

	@PreDestroy
	public void contextDestroyed() {
		logger.info("Context destroyed.");
	}

}
