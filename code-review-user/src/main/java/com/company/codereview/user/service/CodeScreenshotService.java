package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.user.entity.CodeScreenshot;
import com.company.codereview.user.repository.CodeScreenshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 代码截图服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeScreenshotService {
    
    private final CodeScreenshotRepository screenshotRepository;
    
    /**
     * 添加截图
     */
    @Transactional
    public CodeScreenshot addScreenshot(CodeScreenshot screenshot) {
        log.info("添加代码截图: reviewRecordId={}, fileName={}", 
            screenshot.getReviewRecordId(), screenshot.getFileName());
        
        // 设置排序顺序
        if (screenshot.getSortOrder() == null) {
            int maxOrder = getMaxSortOrder(screenshot.getReviewRecordId());
            screenshot.setSortOrder(maxOrder + 1);
        }
        
        screenshotRepository.insert(screenshot);
        
        log.info("代码截图添加成功: id={}", screenshot.getId());
        return screenshot;
    }
    
    /**
     * 批量添加截图
     */
    @Transactional
    public List<CodeScreenshot> batchAddScreenshots(List<CodeScreenshot> screenshots) {
        if (screenshots.isEmpty()) {
            return screenshots;
        }
        
        Long reviewRecordId = screenshots.get(0).getReviewRecordId();
        log.info("批量添加代码截图: reviewRecordId={}, count={}", reviewRecordId, screenshots.size());
        
        // 设置排序顺序
        int maxOrder = getMaxSortOrder(reviewRecordId);
        for (int i = 0; i < screenshots.size(); i++) {
            CodeScreenshot screenshot = screenshots.get(i);
            if (screenshot.getSortOrder() == null) {
                screenshot.setSortOrder(maxOrder + i + 1);
            }
        }
        
        // 批量插入
        screenshotRepository.batchInsert(screenshots);
        
        log.info("批量添加代码截图成功: count={}", screenshots.size());
        return screenshots;
    }
    
    /**
     * 更新截图
     */
    @Transactional
    public CodeScreenshot updateScreenshot(CodeScreenshot screenshot) {
        log.info("更新代码截图: id={}", screenshot.getId());
        
        screenshotRepository.updateById(screenshot);
        
        log.info("代码截图更新成功: id={}", screenshot.getId());
        return screenshot;
    }
    
    /**
     * 删除截图
     */
    @Transactional
    public void deleteScreenshot(Long screenshotId) {
        log.info("删除代码截图: id={}", screenshotId);
        
        screenshotRepository.deleteById(screenshotId);
        
        log.info("代码截图删除成功: id={}", screenshotId);
    }
    
    /**
     * 批量删除截图
     */
    @Transactional
    public void batchDeleteScreenshots(List<Long> screenshotIds) {
        if (screenshotIds.isEmpty()) {
            return;
        }
        
        log.info("批量删除代码截图: ids={}", screenshotIds);
        
        screenshotRepository.batchDeleteByIds(screenshotIds);
        
        log.info("批量删除代码截图成功: count={}", screenshotIds.size());
    }
    
    /**
     * 根据评审记录ID删除截图
     */
    @Transactional
    public void deleteScreenshotsByReviewRecordId(Long reviewRecordId) {
        log.info("删除评审记录的所有截图: reviewRecordId={}", reviewRecordId);
        
        int deletedCount = screenshotRepository.deleteByReviewRecordId(reviewRecordId);
        
        log.info("删除评审记录截图成功: reviewRecordId={}, deletedCount={}", reviewRecordId, deletedCount);
    }
    
    /**
     * 根据ID获取截图
     */
    public CodeScreenshot getScreenshotById(Long screenshotId) {
        log.debug("获取代码截图: id={}", screenshotId);
        
        CodeScreenshot screenshot = screenshotRepository.selectById(screenshotId);
        
        if (screenshot == null) {
            throw new RuntimeException("截图不存在: id=" + screenshotId);
        }
        
        return screenshot;
    }
    
    /**
     * 根据评审记录ID获取截图列表
     */
    public List<CodeScreenshot> getScreenshotsByReviewRecordId(Long reviewRecordId) {
        log.debug("获取评审记录的截图列表: reviewRecordId={}", reviewRecordId);
        
        return screenshotRepository.selectByReviewRecordId(reviewRecordId);
    }
    
    /**
     * 更新截图排序
     */
    @Transactional
    public void updateScreenshotOrder(Long screenshotId, Integer sortOrder) {
        log.info("更新截图排序: id={}, sortOrder={}", screenshotId, sortOrder);
        
        screenshotRepository.updateSortOrder(screenshotId, sortOrder);
        
        log.info("截图排序更新成功: id={}", screenshotId);
    }
    
    /**
     * 批量更新截图排序
     */
    @Transactional
    public void batchUpdateScreenshotOrder(List<Long> screenshotIds) {
        log.info("批量更新截图排序: ids={}", screenshotIds);
        
        for (int i = 0; i < screenshotIds.size(); i++) {
            screenshotRepository.updateSortOrder(screenshotIds.get(i), i + 1);
        }
        
        log.info("批量更新截图排序成功: count={}", screenshotIds.size());
    }
    
    /**
     * 获取评审记录的最大排序号
     */
    private int getMaxSortOrder(Long reviewRecordId) {
        QueryWrapper<CodeScreenshot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("review_record_id", reviewRecordId)
                   .orderByDesc("sort_order")
                   .last("LIMIT 1");
        
        CodeScreenshot lastScreenshot = screenshotRepository.selectOne(queryWrapper);
        
        return lastScreenshot != null && lastScreenshot.getSortOrder() != null ? 
               lastScreenshot.getSortOrder() : 0;
    }
    
    /**
     * 统计评审记录的截图数量
     */
    public long countScreenshotsByReviewRecordId(Long reviewRecordId) {
        QueryWrapper<CodeScreenshot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("review_record_id", reviewRecordId);
        
        return screenshotRepository.selectCount(queryWrapper);
    }
    
    /**
     * 检查截图是否属于指定的评审记录
     */
    public boolean isScreenshotBelongsToReviewRecord(Long screenshotId, Long reviewRecordId) {
        CodeScreenshot screenshot = screenshotRepository.selectById(screenshotId);
        return screenshot != null && reviewRecordId.equals(screenshot.getReviewRecordId());
    }
    
    /**
     * 重新排序截图
     */
    @Transactional
    public void reorderScreenshots(Long reviewRecordId, List<Long> screenshotIds) {
        log.info("重新排序截图: reviewRecordId={}, count={}", reviewRecordId, screenshotIds.size());
        
        for (int i = 0; i < screenshotIds.size(); i++) {
            CodeScreenshot screenshot = new CodeScreenshot();
            screenshot.setId(screenshotIds.get(i));
            screenshot.setSortOrder(i + 1);
            screenshotRepository.updateById(screenshot);
        }
        
        log.info("截图重新排序完成: reviewRecordId={}", reviewRecordId);
    }
    
    /**
     * 获取截图数量
     */
    public Long getScreenshotCount(Long reviewRecordId) {
        return countScreenshotsByReviewRecordId(reviewRecordId);
    }
}