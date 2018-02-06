package com.hanlp.tokenizer.update;


import com.hanlp.tokenizer.util.Address;
import com.hanlp.tokenizer.util.RequestMethod;

/**
 * <p></p>
 *
 * <PRE>
 * <BR>	修改记录
 * <BR>-----------------------------------------------
 * <BR>	修改日期			修改人			修改内容
 * </PRE>
 *
 * @author youshipeng
 * @since 1.0
 * @version 1.0
 */
public class HotUpdateAddress implements Address {
    private String domain;
    private String mapping;
    private RequestMethod method;

    public HotUpdateAddress(String domain, String mapping, RequestMethod method) {
        this.domain = domain;
        this.mapping = mapping;
        this.method = method;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getMapping() {
        return mapping;
    }

    @Override
    public RequestMethod getMethod() {
        return method;
    }
}