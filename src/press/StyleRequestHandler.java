package press;

import static press.PluginConfig.contentHostingDomain;
import static press.PluginConfig.htmlCompatible;

public class StyleRequestHandler extends RequestHandler {
    private SourceFileManager srcManager = new StyleFileManager();
    private CompressedFileManager compressManager = new StyleCompressedFileManager();

    @Override
    public String getCompressedUrl(String requestKey) {
        return getCompressedUrl("press.Press.getCompressedCSS", requestKey);
    }

    @Override
    public String getTag(String src, String... args) {
        String media = args.length > 0 && args[0] != null ? " media=\"" + args[0] + "\"" : "";
        return "<link href=\"" + contentHostingDomain + src
                + "\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\"" + media + ">"
                + (htmlCompatible ? "" : "</link>") + "\n";
    }

    @Override
    protected SourceFileManager getSourceManager() {
        return srcManager;
    }

    @Override
    protected CompressedFileManager getCompressedFileManager() {
        return compressManager;
    }

    public static int clearCache() {
        return StyleCompressor.clearCache();
    }

    @Override
    public String getSingleFileCompressionKey(String fileName) {
        return super.getSingleFileCompressionKey(fileName, new StyleFileManager());
    }
}
