package com.hanlp.tokenizer.core;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.hanlp.tokenizer.NatureAttribute;
import com.hanlp.tokenizer.dictionary.SynonymDictionary;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.SpecialPermission;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Other.DoubleArrayTrieSegment;
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
public class MyTokenizer extends Tokenizer {

    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    private final NatureAttribute natureAttribute = addAttribute(NatureAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    private static Nature auxiliary;

    static {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                auxiliary = Nature.create("auxiliary");
                return null;
            }
        });
    }

    private Segment NLPSegment = HanLP.newSegment();
    private Segment indexSegment = IndexTokenizer.SEGMENT;
    private Segment nshortSegment = new NShortSegment();
    private Segment dijkstraSegment = new DijkstraSegment();
//    private Segment crfSegment = new CRFSegment();
    private Segment speedSegment = new DoubleArrayTrieSegment();


    private SegmentationType segmentationType;
    private BufferedReader reader = null;
    private final Queue<Term> terms = new LinkedTransferQueue<>();
    private final Queue<String> tokens = new LinkedTransferQueue<>();

    private Term current;

    public MyTokenizer(SegmentationType segmentationType) {
        this.segmentationType = segmentationType;
    }

    @Override
    public boolean incrementToken() throws IOException {
        String token = getToken();
        if (token != null) {
            charTermAttribute.setEmpty().append(token);
            return true;
        }
        return false;
    }

    private Segment getSegment() {
        Segment segment;
        if (segmentationType == SegmentationType.hanlp || segmentationType == SegmentationType.standard) {
            segment = NLPSegment;
            segment.enableIndexMode(false);
        }else if (segmentationType == SegmentationType.index || segmentationType == SegmentationType.synonym) {
            segment = indexSegment;
//            segment.enableIndexMode(true);
//                    //.enableOffset(true)
//                    .enableJapaneseNameRecognize(true) // 日本人名识别
//                    .enableNumberQuantifierRecognize(true) //数量词识别
//                    .enableOrganizationRecognize(true) //机构名识别
//                    .enableNameRecognize(true)
//                    .enablePlaceRecognize(true)
//                    .enableTranslatedNameRecognize(true) //音译人名识别
////                    .enableOrganizationRecognize(true)
//                    .enableNumberQuantifierRecognize(true) //数量词识别
//                    .enablePartOfSpeechTagging(true);//词性标注

        } else if(segmentationType == SegmentationType.search || segmentationType == SegmentationType.nlp) {
            segment = NLPSegment;
            segment.enableNameRecognize(true).enableTranslatedNameRecognize(true)
                    .enableJapaneseNameRecognize(true).enablePlaceRecognize(true)
                    .enableOrganizationRecognize(true).enablePartOfSpeechTagging(true);
        } else if(segmentationType == SegmentationType.nshort ) {
            segment = nshortSegment;
            segment.enableCustomDictionary(false)
                    .enablePlaceRecognize(true)
                    .enableOrganizationRecognize(true);
        } else if(segmentationType == SegmentationType.dijkstra ) {
            segment = dijkstraSegment;
            segment.enableCustomDictionary(false)
                    .enablePlaceRecognize(true).enableOrganizationRecognize(true);
        }
        else{
            segment = speedSegment;
            segment.enableCustomDictionary(true).enablePartOfSpeechTagging(true);
        }
        return segment;
    }

    private Segment getSegment2() {
        Segment segment;
        if (segmentationType == SegmentationType.index || segmentationType == SegmentationType.synonym) {
            segment = indexSegment;
        } else {
            segment = NLPSegment;
        }
        segment.enablePartOfSpeechTagging(true) //词性标注
                .enableOffset(true) //计算偏移量
                .enableNameRecognize(true) // 中文人名识别
                .enableJapaneseNameRecognize(true) // 日本人名识别
                .enableNumberQuantifierRecognize(true) //数量词识别
                .enableOrganizationRecognize(true) //机构名识别
                .enablePlaceRecognize(true)
                .enableTranslatedNameRecognize(true); //音译人名识别
        return segment;
    }

    private List<Term> seg(String text) {
        return getSegment().seg(text);
    }

    private Term getTerm() throws IOException {
        Term term = terms.poll();
        if (term == null) {
            if (reader == null) {
                reader = new BufferedReader(input);
            }
            int length;
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[200];
            while (-1 != (length = reader.read(buffer, 0, buffer.length))) {
                sb.append(buffer, 0, length);
            }
            terms.addAll(seg(sb.toString()));
            term = terms.poll();
        }
        return term;
    }

    private String getToken() throws IOException {
        String token = tokens.poll();
        if(token == null) {
            Term term = getTerm();
            if (term != null) {
                int positionIncrement = 1;
                offsetAttribute.setOffset(term.offset, term.offset + term.word.length());
                positionIncrementAttribute.setPositionIncrement(positionIncrement);
                natureAttribute.setNature(term.nature);
                typeAttribute.setType(term.nature.name());

                if (segmentationType == SegmentationType.synonym) {
                    SynonymDictionary.get(term.word).forEach(tokens::offer);
                } else {
                    tokens.offer(term.word);
                }

                token = tokens.poll();
            }
        }
        return token;
    }

    private void decomposition(Term term) {
        if (term.length() == 1 || term.nature == auxiliary) {
            return;
        }
        for (int i = 0 ; i < term.length(); i++) {
            tokens.offer(term.word.substring(i, i + 1));
        }
    }

    private String getIndexToken() throws IOException {
        String token = tokens.poll();
        if (token == null) {
            Term term = getTerm();
            if (term != null) {
                current = term;
                int positionIncrement = 1;
                offsetAttribute.setOffset(term.offset, term.offset + term.word.length());
                positionIncrementAttribute.setPositionIncrement(positionIncrement);
                natureAttribute.setNature(term.nature);
                typeAttribute.setType(term.nature.name());

                tokens.offer(term.word);
                decomposition(term);
                token = tokens.poll();
            }
        } else {
            offsetAttribute.setOffset(current.offset + current.length() - tokens.size() - 1, current.offset + current.length() - tokens.size());
            positionIncrementAttribute.setPositionIncrement(1);
            natureAttribute.setNature(auxiliary);
            typeAttribute.setType(auxiliary.name());
        }
        return token;
    }
}