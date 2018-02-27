package com.hanlp.tokenizer.core;

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
public enum  SegmentationType {
    hanlp("hanlp2"),//标准分词
    standard("hanlp-standard"),//标准分词
    index("hanlp-index"),//索引分词
    search("hanlp-search"),//NLP分词
    synonym("hanlp-synonym"),//同义词分词 与标准分词一样
    nlp("hanlp-nlp"),//NLP分词
    nshort("hanlp-nshort"),//N-最短路径分词
    dijkstra("hanlp-dijkstra"),//最短路分词
//    crf("hanlp-crf"),//CRF分词
    speed("hanlp-speed");//极速词典分词
    private String title;

    SegmentationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}