package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSource {
    //定位并打开资源，返回资源对应的输入流，使用完后要关闭
    InputStream getInputStream() throws IOException;
}
