package com.darkecage.dcaicodegenerator.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 游标分页查询结果
 *
 * @author kaiqi.hu
 */
@Data
public class ScrollResult<T> implements Serializable {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 下一页游标（最后一条记录的自增 id），hasMore 为 false 时为 null
     */
    private Long nextCursor;

    /**
     * 是否还有更多数据
     */
    private boolean hasMore;

    private static final long serialVersionUID = 1L;

    public static <T> ScrollResult<T> of(List<T> records, Long nextCursor, boolean hasMore) {
        ScrollResult<T> result = new ScrollResult<>();
        result.setRecords(records);
        result.setNextCursor(nextCursor);
        result.setHasMore(hasMore);
        return result;
    }
}
