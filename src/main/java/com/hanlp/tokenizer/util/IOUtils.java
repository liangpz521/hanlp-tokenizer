package com.hanlp.tokenizer.util;

import java.io.*;

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
public final class IOUtils {

    private IOUtils() {}

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("stream close exception.", e);
        }
    }

    public static void read(String path, ReaderHelper helper) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                helper.process(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("read file exception path[" + path + "].", e);
        } finally {
            close(reader);
        }
    }
}