package com.hanlp.tokenizer.plugin;

import com.hanlp.tokenizer.core.SegmentationType;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

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
public class MyAnalysisPlugin extends Plugin implements AnalysisPlugin {

    public static final String PLUGIN_NAME = "hanlp-analyzer";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        return new HashMap<String, AnalysisModule.AnalysisProvider<TokenizerFactory>>() {{
            for (SegmentationType segmentationType : SegmentationType.values()) {
                put(segmentationType.getTitle(), (indexSettings, environment, s, settings) ->
                        new MyTokenizerFactory(indexSettings, environment, s, settings, segmentationType));
            }
        }};
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return new HashMap<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>>() {{
            for (SegmentationType segmentationType : SegmentationType.values()) {
                put(segmentationType.getTitle(), (indexSettings, environment, s, settings) ->
                        new MyAnalyzerProvider(indexSettings, environment, s, settings, segmentationType));
            }
        }};
    }
}