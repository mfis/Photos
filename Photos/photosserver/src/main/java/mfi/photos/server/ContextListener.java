package mfi.photos.server;

import com.google.gson.GsonBuilder;
import mfi.photos.util.GalleryViewCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

@Component
public class ContextListener {

	@Autowired
	private Processor processor;

	@PostConstruct
	public void contextInitialized() {
		Assert.isTrue(StringUtils.endsWith(processor.lookupPhotosDir(), "/"), "photosDir has to end with '/'");
		Assert.isTrue(StringUtils.endsWith(processor.lookupListDir(), "/"), "jsonDir has to end with '/'");
		Assert.isTrue(StringUtils.isNotBlank(processor.lookupLinkToLawSite()), "lawlink has to be configured");
		GalleryViewCache.getInstance().refresh(processor.lookupListDir(), new GsonBuilder().create());
	}
}
