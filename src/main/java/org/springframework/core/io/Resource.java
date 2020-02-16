package org.springframework.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * https://blog.csdn.net/u010086122/article/details/81607127
 */
public interface Resource extends InputStreamSource{
    //该资源是否存在
    boolean exists();

    //是否可读
    boolean isReadable();

    //是否已经打开输入流，若返回true，则只能读取一次后关闭
    boolean isOpen();

    /**
     * https://www.cnblogs.com/1540340840qls/p/9426764.html
     * https://www.cnblogs.com/shoshana-kong/p/10939188.html
     * https://blog.csdn.net/koflance/article/details/79635240
     *
     * URI和URL关系：
     * 1. URL是URI的一种，是特殊的URI;
     * 2. URI又可以分为URN(name)和URL(location)，以及其他；
     * (1) URN好比身份证(标识一个人)，URL好比住址(让你知道怎么找到这个人)
     * (2) 例如：isbn:0-395-36341-1是RUN，一个国际标准书号，可以唯一确定哪本书；
     * http://blog.csdn.net/koflance是个URL，通过这个网址可以告诉CDN找到我的博客所在地，并且还告诉用HTTP协议访问
     * 3. URL正在被弃用？
     *
     * URI格式：
     *   URI=[scheme:]<scheme-specific-part>[#fragment]
     *   <scheme-specfic-part>=[//authority]<path>[:query]
     *   authority=[userinfo@]<host>[:port]
     * 例如：http://qinlin@localhost:8080/oi/oi?user='aieg'#iewio
     * 1. authority --- qinlin@localhost:8080
     * 2. path --- /oi/oi
     * 3. query --- user='aieg'
     *
     * 问题：
     * 1. 这几篇文章里面有提到个东西，让URI成为URL的是访问机制？那为什么URI格式里面也会有scheme
     * 还是说正因为scheme可选，才会出现URL这个分类出现，我觉得可能是这样。
     */
    URL getURL() throws IOException;

    URI getURI() throws IOException;

    //如果当前Resource代表的底层资源能由java.io.File代表，则返回该File，否则抛出IO异常
    File getFile() throws IOException;

    long contentLength() throws IOException;

    //最后修改时间
    long lastModified() throws IOException;

    //根据资源的相对路径创建新资源
    Resource createRelative(String relativePath) throws IOException;

    //返回文件路径，网络资源返回""
    String getFilename();

    //返回当前Resource代表的底层资源的描述符，实际文件名或实际URL地址
    String getDescription();
}
