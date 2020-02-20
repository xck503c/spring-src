package org.springframework.core.io;

import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * URL，路径类型的资源
 */
public class UrlResource extends AbstractFileResolvingResource{

    private final URI uri;
    private final URL url;
    private final URL cleanedUrl;

    public UrlResource(URI uri) throws MalformedURLException {
        Assert.notNull(uri, "URI must no be null");
        this.uri = uri;
        this.url = uri.toURL();
        this.cleanedUrl = this.getCleanedUrl(this.url, uri.toString());
    }

    public UrlResource(URL url) {
        Assert.notNull(url, "URL must not be null");
        this.url = url;
        this.cleanedUrl = this.getCleanedUrl(this.url, url.toString());
        this.uri = null;
    }

    public UrlResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path must not be null");
        this.uri = null;
        this.url = new URL(path);
        this.cleanedUrl = this.getCleanedUrl(this.url, path);
    }

    public UrlResource(String protocol, String location) throws MalformedURLException {
        this(protocol, location, (String)null);
    }

    //uri协议
    public UrlResource(String protocol, String location, String fragment) throws MalformedURLException {
        try {
            this.uri = new URI(protocol, location, fragment);
            this.url = this.uri.toURL();
            this.cleanedUrl = this.getCleanedUrl(this.url, this.uri.toString());
        } catch (URISyntaxException e) {
            MalformedURLException exToThrow = new MalformedURLException(e.getMessage());
            exToThrow.initCause(e);
            throw exToThrow;
        }
    }

    /**
     * 转换originalPath，清理
     */
    private URL getCleanedUrl(URL originalUrl, String originalPath){
        try {
            return new URL(StringUtils.cleanPath(originalPath));
        } catch (MalformedURLException var4) {
            return originalUrl;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection con = url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        }
        catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    @Override
    public URL getURL() {
        return this.url;
    }

    @Override
    public URI getURI() throws IOException {
        if (this.uri != null) {
            return this.uri;
        }
        else {
            return super.getURI();
        }
    }

    @Override
    public File getFile() throws IOException {
        return this.uri != null ? super.getFile(this.uri) : super.getFile();
    }

    @Override
    public Resource createRelative(String relativePath) throws MalformedURLException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        return new UrlResource(new URL(this.url, relativePath));
    }

    @Override
    public String getFilename() {
        return (new File(this.url.getFile())).getName();
    }

    public String getDescription() {
        return "URL [" + this.url + "]";
    }

    public boolean equals(Object obj) {
        return obj == this || obj instanceof UrlResource && this.cleanedUrl.equals(((UrlResource)obj).cleanedUrl);
    }

    public int hashCode() {
        return this.cleanedUrl.hashCode();
    }

    public static void main(String[] args) throws Exception{
        URL url = new URL("file:/D:/test.jar");
        System.out.println(new URL(url, "yese").toString()); //file:/D:/yese
    }
}
