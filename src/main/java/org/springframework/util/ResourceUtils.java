package org.springframework.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ResourceUtils {

    /**
     * 根据URL来获取File，最后还是会转换成URI来获取路径
     */
    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        //断言不能为空
        Assert.notNull(resourceUrl, "Resource URL must not be null");
        //URL协议必须要为file开头，不是file开头就判定不存在
        if(!"file".equals(resourceUrl.getProtocol())){
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
                    + "because it does not reside in the filesystem: " + resourceUrl);
        }else{
            //[scheme:]scheme-specific-part[#fragment]对于文件来说，应该是本地文件的绝对路径吧
            try {
                return new File(toURI(resourceUrl).getSchemeSpecificPart());
            } catch (URISyntaxException e) {
                return new File(resourceUrl.getFile());
            }
        }
    }

    public static File getFile(URI resourceUri, String description) throws FileNotFoundException{
        Assert.notNull(resourceUri, "Resource URI must not be null");
        if (!"file".equals(resourceUri.getScheme())) {
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
                    + "because it does not reside in the file system: " + resourceUri);
        } else {
            return new File(resourceUri.getSchemeSpecificPart());
        }
    }

    /**
     * 说明判断是否是文件URL，file，vfsfile，vfs开头
     */
    public static boolean isFileURL(URL url){
        String protocol = url.getProtocol();
        return "file".equals(protocol) || "vfsfile".equals(protocol) || "vfs".equals(protocol);
    }

    /**
     * 判断是否为储存java源码的压缩包文件
     * path里面包含!/，协议为：code-source...
     */
    public static boolean isJarURL(URL url){
        String protocol = url.getProtocol();
        return "jar".equals(protocol) || "zip".equals(protocol) || "vfszip".equals(protocol) || "wsjar".equals(protocol) || "code-source".equals(protocol) && url.getPath().contains("!/");
    }

    /**
     * 提取URL中的表示文件的URL
     * 1. jar的URL：jar:<url>!/{entry}
     * 2. 如果包含!/则需要特殊处理解析；
     */
    public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException{
        //获取表示文件的URL，getFile后会去除原本的协议
        //如:jar:file:D:// ---> file:D://
        //如：file:D:// ---> D://
        String urlFile = jarUrl.getFile();
        int separatorIndex = urlFile.indexOf("!/"); //jar协议的URL中一定会包含这个
        if(separatorIndex != -1){
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException e) { //协议异常，比如说：jar:D://这种
                if(!jarFile.startsWith("/")){
                    jarFile = "/" + jarFile;
                }
                return new URL("file:" + jarFile); //开头补上file:
            }
        }else {
            return jarUrl; //这个作用是，如果不包含!/这估计也不是特殊协议，直接返回？
        }
    }

    public static URI toURI(URL url)throws URISyntaxException{
        return toURI(url.toString());
    }

    public static URI toURI(String location) throws URISyntaxException{
        //格式化空格
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    public static void main(String[] args) throws Exception{
        //test extractJarFileURL
        URL url = new URL("jar:file:D://maven-compiler-plugin-2.5.1.jar!/org.apache.maven.plugin.AbstractCompilerMojo.class");
        URL actualUrl = extractJarFileURL(url);
        System.out.println(url.getProtocol()); //jar
        System.out.println(url.getFile()); //file:D://maven-compiler-plugin-2.5.1.jar!/org.apache.maven.plugin.AbstractCompilerMojo.class
        System.out.println(actualUrl.getProtocol()); //file
    }
}
