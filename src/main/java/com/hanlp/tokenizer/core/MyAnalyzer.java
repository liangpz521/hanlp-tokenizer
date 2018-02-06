package com.hanlp.tokenizer.core;

import com.hanlp.tokenizer.NatureAttribute;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

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
 *@version 1.0
 */
public class MyAnalyzer extends Analyzer {

    private SegmentationType segmentationType;

    public MyAnalyzer(SegmentationType segmentationType) {
        this.segmentationType = segmentationType;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new MyTokenizer(segmentationType);
        return new TokenStreamComponents(tokenizer);
    }

    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new MyAnalyzer(SegmentationType.synonym);
        TokenStream tokenStream = analyzer.tokenStream("text", "你好，上外日本文化经济学院的陆晚霞教授正在教授泛读课程");
        tokenStream.reset();
        while(tokenStream.incrementToken()){
            CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
            PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
            NatureAttribute natureAttribute = tokenStream.getAttribute(NatureAttribute.class);
            TypeAttribute typeAttribute = tokenStream.getAttribute(TypeAttribute.class);
            System.out.println(charTermAttribute.toString() + " : " + typeAttribute.type() + " : " + natureAttribute.nature() + " ("+offsetAttribute.startOffset()+" - "+offsetAttribute.endOffset()+") "+positionIncrementAttribute.getPositionIncrement());
        }
        tokenStream.close();
    }
}