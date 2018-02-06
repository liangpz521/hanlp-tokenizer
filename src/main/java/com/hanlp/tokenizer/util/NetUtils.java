package com.hanlp.tokenizer.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by youshipeng on 2016/10/21.
 */
public final class NetUtils {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static Logger logger = Loggers.getLogger("HanLP-NetUtils");

    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * 发送Http请求
     *
     * @param url
     * @param params
     * @param requestMethod
     * @return
     */
    static CloseableHttpResponse sendHttpRequest(String url, Map<String, String> params, Map<String, String> headers, RequestMethod requestMethod) {
        try {
            HttpRequestBase httpRequest = buildHttpRequest(url, params, requestMethod);
            setHeaders(httpRequest, headers);
            return httpClient.execute(httpRequest);
        } catch (IOException e) {
            logger.info("send request fail url[" + url + "]");
            return null;
        }
    }

    /**
     * 创建http连接
     *
     * @param url
     * @param params
     * @return
     */
    private static HttpRequestBase buildHttpRequest(String url, Map<String, String> params, RequestMethod requestMethod) {
        String assembleParams = assemble(params);
        String realUrlStr;
        switch (requestMethod) {
            case GET:
                realUrlStr = url + "?" + assembleParams;
                return new HttpGet(realUrlStr);
            case POST:
                realUrlStr = url;
                HttpPost httpPost = new HttpPost(realUrlStr);
                httpPost.setEntity(buildParams(params));
                return httpPost;
            case HEAD:
                realUrlStr = url + "?" + assembleParams;
                return new HttpHead(realUrlStr);
            default:
                throw new RuntimeException("no support request method [" + requestMethod.name() + "] now.");
        }
    }

    private static void setHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty())
            headers.forEach(httpRequest::setHeader);
    }

    /**
     * 返回  "&key=value&key=value..."
     *
     * @param params
     * @return
     */
    private static String assemble(Map<String, String> params) {
        StringBuilder sb = new StringBuilder("");
        if (params == null || params.isEmpty()) {
            return sb.toString();
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private static HttpEntity buildParams(Map<String, String> params) {
        try {
            List<NameValuePair> formParams = new ArrayList<NameValuePair>() {{
                params.forEach((key, value) -> add(new BasicNameValuePair(key, value)));
            }};

            return new UrlEncodedFormEntity(formParams, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("build params exception, params[" + params.toString() + "]", e);
        }
    }

}
