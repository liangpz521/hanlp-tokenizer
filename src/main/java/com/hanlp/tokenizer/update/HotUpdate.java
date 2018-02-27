package com.hanlp.tokenizer.update;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hanlp.tokenizer.dictionary.SynonymDictionary;
import com.hanlp.tokenizer.plugin.MyAnalysisPlugin;
import com.hanlp.tokenizer.util.Address;
import com.hanlp.tokenizer.util.RequestSender;
import org.apache.http.HttpEntity;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.env.Environment;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hanlp.tokenizer.util.IOUtils.close;
import static com.hanlp.tokenizer.util.RequestMethod.GET;
import static com.hanlp.tokenizer.util.RequestMethod.HEAD;
import static org.apache.http.util.TextUtils.isEmpty;

/**
 * <p></p>
 * <p>
 * <PRE>
 * <BR>	修改记录
 * <BR>-----------------------------------------------
 * <BR>	修改日期			修改人			修改内容
 * </PRE>
 *
 * @author youshipeng
 * @version 1.0
 * @since 1.0
 */
public class HotUpdate {
    public static Path HANLP_TOKENIZER_ROOT = null;

    private Logger logger = Loggers.getLogger("HanLP-HotUpdate");
    private final static String FILE_NAME = "hanlp-hot-update.cfg.xml";
    private final static String HANLP_PROPERTIES_NAME = "hanlp.properties";
    private final static String REMOTE_EXT_DICT = "remote_ext_dict";
    private final static String REMOTE_EXT_STOP = "remote_ext_stopwords";
    private final static String REMOTE_EXT_SYNONYMS = "remote_ext_synonyms";
    private final static String REMOTE_EXT_CUSTOM = "remote_ext_custom";

    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    private Properties properties;

    private static HotUpdate hotUpdate = null;

    public synchronized static void begin(Environment env) {
        if (hotUpdate == null) {
            hotUpdate = new HotUpdate(env);
        }
    }

    private HotUpdate(Environment env) {
        try {
            properties = new Properties();
            logger.info(System.getProperty("user.dir"));

            HANLP_TOKENIZER_ROOT = env.pluginsFile().resolve(MyAnalysisPlugin.PLUGIN_NAME);
            if (HANLP_TOKENIZER_ROOT == null) {
                throw new RuntimeException("hanlp tokenizer root path not found.");
            }
            Path configFile = HANLP_TOKENIZER_ROOT.resolve(FILE_NAME);
            logger.info(configFile.toString());

            properties.loadFromXML(new FileInputStream(configFile.toFile()));
            String usedLocation = getProperty(REMOTE_EXT_DICT);
            String stopLocation = getProperty(REMOTE_EXT_STOP);
            String synonymLocation = getProperty(REMOTE_EXT_SYNONYMS);
            String fileName = null;
            String extCustomWordDictCfg = getProperty(REMOTE_EXT_CUSTOM);

            if (extCustomWordDictCfg != null) {
                String[] filePaths = extCustomWordDictCfg.split(";");
                for (String filePath : filePaths) {
                    if (filePath != null && !"".equals(filePath.trim())) {
//                        Path file = PathUtils.get(filePath.trim());
//                        extStopWordDictFiles.add(file.toString());
                        char prefix = filePath.charAt(0);
                        if(prefix != '/'){
                            fileName = HANLP_TOKENIZER_ROOT.toString()+"/"+filePath;
                        }else{
                            fileName = filePath;
                        }
                        logger.info(fileName);

                        //读取文件进行分词
                        BufferedReader reader = new BufferedReader(new FileReader(fileName));//构造一个BufferedReader类来读取文件
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] message = line.split(",");
                            if (message.length == 2) {
                                CustomDictionary.add(message[0], message[1]);
                            } else {
                                //用于判断是否加入了词性
                                String[] msg = line.split(" ");
                                if(msg.length >1){
//                                    System.out.println(Arrays.toString(msg));
                                    CustomDictionary.add(msg[0], msg[1]+' '+msg[2]);
                                }else {

                                    CustomDictionary.add(line);
                                }
//                                logger.info("名字："+line);
                            }
                        }
                    }
                }
            }

            // used words thread
            if (!isEmpty(usedLocation)) {
                pool.scheduleAtFixedRate(new Monitor(usedLocation.trim(), WordType.USED) {
                    @Override
                    void processResponse(BufferedReader reader) throws IOException {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] message = line.split(",");
                            if (message.length == 2) {
                                CustomDictionary.add(message[0], message[1]);
                            } else {
                                CustomDictionary.add(line);
                            }
                            logger.info(line);
                        }
                    }
                }, 10, 60, TimeUnit.SECONDS);
            }
            // stop words thread
            if (!isEmpty(stopLocation)) {
                pool.scheduleAtFixedRate(new Monitor(stopLocation.trim(), WordType.STOP) {
                    @Override
                    void processResponse(BufferedReader reader) throws IOException {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            CustomDictionary.remove(line);
                            logger.info(line);
                        }
                    }
                }, 10, 60, TimeUnit.SECONDS);
            }
            // synonymous thread
            if (!isEmpty(synonymLocation)) {
                pool.scheduleAtFixedRate(new Monitor(synonymLocation.trim(), WordType.SYNONYM) {
                    @Override
                    void processResponse(BufferedReader reader) throws IOException {
                        String line;
                        TreeMap<String, Set<String>> treeMap = new TreeMap<>();
                        while ((line = reader.readLine()) != null) {
                            Set<String> words = Arrays.stream(line.split(" ")).collect(Collectors.toSet());
                            words.forEach(word -> {
                                if (treeMap.containsKey(word))  {
                                    treeMap.get(word).addAll(new HashSet<>(words));
                                } else {
                                    treeMap.put(word, new HashSet<>(words));
                                }
                            });
                            logger.info(line);
                        }
                        SynonymDictionary.reloadCustomerSynonyms(treeMap);
                        logger.info("synonymous sync success.");
                    }
                }, 10, 60, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            throw new RuntimeException("hanlp-hot-update.cfg.xml read fail.", e);
        }
    }

    private String getProperty(String key) {
        if (properties != null) {
            return properties.getProperty(key);
        }
        return null;
    }

    private abstract class Monitor implements Runnable {

        private Address address;
        private Address tryAddress;
        private WordType wordType;

        private String lastModified;
        private String eTags;

        Monitor(String location, WordType wordType) {
            this.address = new HotUpdateAddress(location, "", GET);
            this.tryAddress = new HotUpdateAddress(location, "", HEAD);
            this.wordType = wordType;
        }

        abstract void processResponse(BufferedReader reader) throws IOException;

        @Override
        public void run() {
            new RequestSender<Void>() {
                @Override
                public Address getAddress() {
                    return tryAddress;
                }

                @Override
                public HashMap<String, String> getHeaders() {
                    return new HashMap<String, String>() {{
                        if (lastModified != null) {
                            put("If-Modified-Since", lastModified);
                        }
                        if (eTags != null) {
                            put("If-None-Match", eTags);
                        }
                    }};
                }

                @Override
                public Void readResult(int statusCode, HashMap<String, String> headers, HttpEntity httpEntity) {
                    if (statusCode == 200) {
                        String currentLastModified = headers.get("Last-Modified");
                        String currentETags = headers.get("ETag");
                        if ((!isEmpty(currentLastModified) && !currentLastModified.equals(lastModified))
                                || (!isEmpty(currentETags) && !currentETags.equals(eTags))) {
                            logger.info(address.getDomain() + " update dictionary.");
                            lastModified = currentLastModified;
                            eTags = currentETags;
                            updateDictionary();
                        } else {
                            logger.info(address.getDomain() + " result no changed and skip current request.");
                        }
                    } else if (statusCode == 304) {
                        logger.info(address.getDomain() + " result no changed and skip current request.");
                    } else {
                        logger.info(address.getDomain() + " status code[" + statusCode + "].");
                    }
                    return null;
                }
            }.send();
        }

        private void updateDictionary() {
            logger.info("-----------updateDictionary: begin--------------");
            new RequestSender<Void>() {
                @Override
                public Address getAddress() {
                    return address;
                }

                @Override
                public Void readResult(int statusCode, HashMap<String, String> headers, HttpEntity httpEntity) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "utf-8"));
                        processResponse(reader);
                    } catch (IOException e) {
                        throw new RuntimeException("hanlp-hot-update.cfg.xml read fail.", e);
                    } finally {
                        close(reader);
                    }
                    return null;
                }
            }.send();
        }
    }

    private enum WordType {
        USED, STOP, SYNONYM
    }
}