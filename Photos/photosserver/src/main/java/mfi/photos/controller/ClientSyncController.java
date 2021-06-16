package mfi.photos.controller;

import mfi.photos.server.Processor;
import mfi.photos.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class ClientSyncController {

    private static final String UTF_8 = "UTF-8";

    @Autowired
    private Processor processor;

    @Autowired
    private RequestUtil requestUtil;

    @RequestMapping("/PhotosAddGalleryServlet")
    public void response(HttpServletResponse response,
                         @RequestParam(name = "saveGallery", required = false) String saveGallery,
                         @RequestParam(name = "renameGallery", required = false) String renameGallery,
                         @RequestParam(name = "saveImage", required = false) String saveImage,
                         @RequestParam(name = "checksum", required = false) String checksum,
                         @RequestParam(name = "readlist", required = false) String readlist,
                         @RequestParam(name = "readalbum", required = false) String readalbum,
                         @RequestParam(name = "album_key", required = false) String albumKey,
                         @RequestParam(name = "cleanup", required = false) String cleanup,
                         @RequestParam(name = "cleanupListHash", required = false) String cleanupListHash,
                         @RequestParam(name = "galleryName", required = false) String galleryName,
                         @RequestParam(name = "imageName", required = false) String imageName,
                         @RequestParam(name = "keyOld", required = false) String keyOld,
                         @RequestParam(name = "append", required = false) String append
                        ) throws IOException {

        requestUtil.assertLoggedInUser();

        response.setCharacterEncoding(UTF_8);
        response.addHeader("Cache-Control", "no-cache");

        PrintWriter out = response.getWriter();

        // TODO: write separate methods for each request
        if (saveGallery != null) {
            processor.saveNewGallery(saveGallery);
        } else if (renameGallery != null) {
            processor.renameGallery(keyOld, renameGallery);
        } else if (saveImage != null) {
            processor.saveNewImage(galleryName, imageName, saveImage, append);
        } else if (checksum != null) {
            out.print(processor.checksumFromImage(galleryName, imageName));
        } else if (readlist != null) {
            // -> GalleryList
            out.print(processor.listJson(null, 0L, true));
        } else if (readalbum != null) {
            // -> GalleryView
            out.println(processor.galleryJson(albumKey));
        } else if (cleanup != null) {
            processor.cleanUp(cleanup, cleanupListHash);
        }

        response.setStatus(200);

        out.flush();
        out.close();
    }

}
