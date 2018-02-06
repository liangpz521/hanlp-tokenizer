package com.hanlp.tokenizer.plugin;

import com.hanlp.tokenizer.core.MyTokenizer;
import com.hanlp.tokenizer.core.SegmentationType;
import com.hanlp.tokenizer.update.HotUpdate;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

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
public class MyTokenizerFactory extends AbstractTokenizerFactory {

    private SegmentationType segmentationType;

    public MyTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings, SegmentationType segmentationType) {
        super(indexSettings, name, settings);
        this.segmentationType = segmentationType;
        HotUpdate.begin(env);
    }

    @Override
    public Tokenizer create() {
        return new MyTokenizer(segmentationType);
    }
}