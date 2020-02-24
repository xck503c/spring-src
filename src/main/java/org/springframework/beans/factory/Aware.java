package org.springframework.beans.factory;

/**
 * 标记接口，感知接口？
 * 1. 实现该接口的bean表明：spring容器可以通过回调方式将框架中的特定对象通知给该bean
 * 2. 他下面有各种扩展接口，具体是要通知说明由它们确定
 * 3. 正常只接收单个参数的void方法
 */
public interface Aware {
}
