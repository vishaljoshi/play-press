package press;

import play.mvc.Router;
import play.vfs.VirtualFile;
import press.io.PressFileGlobber;

/**
 * Manages the state of a single request to render the page
 */
public class RequestManager {
    public static final boolean RQ_TYPE_SCRIPT = true;
    public static final boolean RQ_TYPE_STYLE = !RQ_TYPE_SCRIPT;

    private boolean errorOccurred = false;
    private RequestHandler scriptRequestHandler = new ScriptRequestHandler();
    private RequestHandler styleRequestHandler = new StyleRequestHandler();

    private RequestHandler getRequestHandler(boolean rqType) {
        return rqType == RQ_TYPE_SCRIPT ? scriptRequestHandler : styleRequestHandler;
    }

    public String addSingleFile(boolean rqType, String fileName, String ... args) {
        RequestHandler handler = getRequestHandler(rqType);
        VirtualFile file = handler.checkFileExists(fileName);

        String src = null;
        if (performCompression()) {
            src = handler.getCompressedUrl(handler.getSingleFileCompressionKey(fileName));
        } else {
            src = Router.reverse(file);
        }

        return handler.getTag(src, args);
    }

    public String addMultiFile(boolean rqType, String src, boolean packFile, String ... args) {
        RequestHandler handler = getRequestHandler(rqType);
        String baseUrl = handler.getSrcDir();
        String result = "";
        for (String fileName : PressFileGlobber.getResolvedFiles(src, baseUrl)) {
            VirtualFile file = handler.checkFileExists(fileName);
            handler.checkForDuplicates(fileName);

            if (performCompression()) {
                handler.add(fileName, packFile);
            } else {
                result += handler.getTag(Router.reverse(file), args);
            }
        }

        return result;
    }

    public String compressedTag(boolean rqType, String key, String ... args) {
        RequestHandler handler = getRequestHandler(rqType);
        if (performCompression()) {
          String requestKey;
          if (key != null) {
            handler.getSourceManager().requestKey = key;
            requestKey = key;
          }
          else requestKey =  handler.closeRequest();
          return handler.getTag(handler.getCompressedUrl(requestKey), args);
        }
        return "";
    }

    public void saveFileList() {
        if (!performCompression()) {
            return;
        }

        scriptRequestHandler.saveFileList();
        styleRequestHandler.saveFileList();
    }

    public void errorOccurred() {
        errorOccurred = true;
    }

    private boolean performCompression() {
        return PluginConfig.enabled && !errorOccurred;
    }

    public static void clearCache() {
        ScriptRequestHandler.clearCache();
        StyleRequestHandler.clearCache();
    }
}
