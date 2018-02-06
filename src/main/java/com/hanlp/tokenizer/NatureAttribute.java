package com.hanlp.tokenizer;

import com.hankcs.hanlp.corpus.tag.Nature;
import org.apache.lucene.util.Attribute;

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
public interface NatureAttribute extends Attribute {
    Nature nature();

    void setNature(Nature nature);
}