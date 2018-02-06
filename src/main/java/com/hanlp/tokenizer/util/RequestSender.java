package com.hanlp.tokenizer.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

import static com.hanlp.tokenizer.util.IOUtils.close;

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
public abstract class RequestSender<T> {

    private Logger logger = Loggers.getLogger("HanLP-RequestSender");

    public abstract Address getAddress();

    public abstract T readResult(int statusCode, HashMap<String, String> httpHeads, HttpEntity httpEntity);

    public HashMap<String, String> getParams() {
        return null;
    }

    public HashMap<String, String> getHeaders() {
        return null;
    }

    public boolean checkParams() {
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public T send() {
        CloseableHttpResponse response = null;
        try {
            if (!checkParams()) {
                throw new RuntimeException("params missing or wrong -> params[" + getParams() + "].");
            }

            Address address = getAddress();
            String url = address.getDomain() + address.getMapping();

            Class<T> resultType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

            response = NetUtils.sendHttpRequest(url, getParams(), getHeaders(), address.getMethod());

            return response == null ? null :
                    readResult(response.getStatusLine().getStatusCode(), getAllHeaders(response), response.getEntity());
        } finally {
            close(response);
        }
    }

    private HashMap<String, String> getAllHeaders(final CloseableHttpResponse response) {
        return new HashMap<String, String>() {{
            for (Header header : response.getAllHeaders()) {
                put(header.getName(), header.getValue());
            }
        }};
    }
}