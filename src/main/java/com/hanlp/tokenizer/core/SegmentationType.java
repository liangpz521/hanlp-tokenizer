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
    index("hanlp_index"),
    search("hanlp_search"),
    synonym("hanlp_synonym");

    private String title;

    SegmentationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}