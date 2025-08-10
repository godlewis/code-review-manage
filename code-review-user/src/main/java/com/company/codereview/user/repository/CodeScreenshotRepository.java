package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.CodeScreenshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代码截图数据访问层
 */
@Mapper
public interface CodeScreenshotRepository extends BaseMapper<CodeScreenshot> {
    
    /**
     * 根据评审记录ID查询截图列表
     */
    List<CodeScreenshot> selectByReviewRecordId(@Param("reviewRecordId") Long reviewRecordId);
    
    /**
     * 根据评审记录ID删除截图
     */
    int deleteByReviewRecordId(@Param("reviewRecordId") Long reviewRecordId);
    
    /**
     * 批量插入截图
     */
    int batchInsert(@Param("screenshots") List<CodeScreenshot> screenshots);
    
    /**
     * 批量删除截图
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 更新截图排序
     */
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
}