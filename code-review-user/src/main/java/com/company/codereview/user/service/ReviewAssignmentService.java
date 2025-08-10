package com.company.codereview.user.service;

import com.company.codereview.user.algorithm.HungarianAlgorithm;
import com.company.codereview.user.dto.MatchingMatrix;
import com.company.codereview.user.entity.ReviewAssignment;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.ReviewAssignmentRepository;
import com.company.codereview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评审分配服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAssignmentService {
    
    private final ReviewAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final HungarianAlgorithm hungarianAlgorithm;
    
    // 配置参数
    @Value("${review.assignment.avoidance-weeks:4}")
    private int avoidanceWeeks;
    
    @Value("${review.assignment.max-assignments-per-week:3}")
    private int maxAssignmentsPerWeek;
    
    @Value("${review.assignment.skill-match-weight:0.4}")
    private double skillMatchWeight;
    
    @Value("${review.assignment.load-balance-weight:0.3}")
    private double loadBalanceWeight;
    
    @Value("${review.assignment.diversity-weight:0.3}")
    private double diversityWeight;
    
    /**
     * 智能分配算法核心逻辑
     * 1. 获取团队活跃成员列表
     * 2. 计算成员间的匹配度矩阵
     * 3. 应用避重约束（4周内不重复）
     * 4. 使用匈牙利算法求解最优分配
     * 5. 应用负载均衡调整
     */
    @Transactional
    public List<ReviewAssignment> generateWeeklyAssignments(Long teamId, LocalDate weekStart) {
        log.info("开始为团队 {} 生成 {} 周的评审分配", teamId, weekStart);
        
        // 1. 获取团队活跃成员列表
        List<User> activeMembers = getActiveTeamMembers(teamId);
        if (activeMembers.size() < 2) {
            log.warn("团队 {} 活跃成员不足2人，无法进行分配", teamId);
            return new ArrayList<>();
        }
        
        // 2. 计算成员间的匹配度矩阵
        MatchingMatrix matrix = calculateMatchingMatrix(activeMembers, weekStart);
        
        // 3. 使用匈牙利算法求解最优分配
        int[] assignmentArray = HungarianAlgorithm.solve(matrix.getMatrix());
        List<HungarianAlgorithm.Assignment> optimalAssignments = new ArrayList<>();
        
        for (int i = 0; i < assignmentArray.length; i++) {
            int j = assignmentArray[i];
            if (j != -1) { // 有效的分配
                double cost = matrix.getMatrix()[i][j];
                optimalAssignments.add(new HungarianAlgorithm.Assignment(i, j, cost));
            }
        }
        
        // 4. 转换为ReviewAssignment实体并应用负载均衡调整
        List<ReviewAssignment> assignments = convertToReviewAssignments(
            optimalAssignments, matrix, teamId, weekStart);
        
        // 5. 保存分配结果
        if (!assignments.isEmpty()) {
            assignmentRepository.insertBatch(assignments);
            log.info("成功为团队 {} 生成 {} 个评审分配", teamId, assignments.size());
        }
        
        return assignments;
    }
    
    /**
     * 获取团队活跃成员列表
     */
    private List<User> getActiveTeamMembers(Long teamId) {
        return userRepository.findByTeamId(teamId).stream()
            .filter(User::getActive)
            .filter(user -> !user.getDeleted())
            .collect(Collectors.toList());
    }
    
    /**
     * 计算匹配度矩阵
     */
    private MatchingMatrix calculateMatchingMatrix(List<User> members, LocalDate weekStart) {
        List<Long> userIds = members.stream().map(User::getId).collect(Collectors.toList());
        MatchingMatrix matrix = new MatchingMatrix(userIds);
        
        // 为每对用户计算匹配度分数
        for (int i = 0; i < members.size(); i++) {
            for (int j = 0; j < members.size(); j++) {
                if (i != j) { // 不能自己评审自己
                    User reviewer = members.get(i);
                    User reviewee = members.get(j);
                    
                    double score = calculatePairScore(reviewer, reviewee, weekStart);
                    // 匈牙利算法求最小值，所以使用负分数
                    matrix.setScore(reviewer.getId(), reviewee.getId(), -score);
                } else {
                    // 自己评审自己的分数设为很大的正数（避免被选中）
                    matrix.setScore(members.get(i).getId(), members.get(j).getId(), Double.MAX_VALUE);
                }
            }
        }
        
        return matrix;
    }
    
    /**
     * 计算两个用户之间的匹配度分数
     */
    private double calculatePairScore(User reviewer, User reviewee, LocalDate weekStart) {
        // 1. 技能匹配度计算
        double skillMatchScore = calculateSkillMatchScore(reviewer, reviewee);
        
        // 2. 历史避重检查
        double avoidanceScore = calculateAvoidanceScore(reviewer.getId(), reviewee.getId(), weekStart);
        
        // 3. 工作负载评估
        double loadBalanceScore = calculateLoadBalanceScore(reviewer.getId(), reviewee.getId(), weekStart);
        
        // 4. 多样性评估
        double diversityScore = calculateDiversityScore(reviewer, reviewee);
        
        // 加权计算总分
        double totalScore = skillMatchScore * skillMatchWeight +
                           avoidanceScore * (1 - skillMatchWeight - loadBalanceWeight - diversityWeight) +
                           loadBalanceScore * loadBalanceWeight +
                           diversityScore * diversityWeight;
        
        log.debug("用户 {} -> {} 的匹配分数: 技能={}, 避重={}, 负载={}, 多样性={}, 总分={}",
            reviewer.getId(), reviewee.getId(), skillMatchScore, avoidanceScore, 
            loadBalanceScore, diversityScore, totalScore);
        
        return totalScore;
    }
    
    /**
     * 计算技能匹配度分数
     */
    private double calculateSkillMatchScore(User reviewer, User reviewee) {
        String reviewerSkills = reviewer.getSkills();
        String revieweeSkills = reviewee.getSkills();
        
        if (reviewerSkills == null || revieweeSkills == null) {
            return 0.5; // 默认中等匹配度
        }
        
        Set<String> reviewerSkillSet = Arrays.stream(reviewerSkills.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
        
        Set<String> revieweeSkillSet = Arrays.stream(revieweeSkills.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
        
        if (reviewerSkillSet.isEmpty() || revieweeSkillSet.isEmpty()) {
            return 0.5;
        }
        
        // 计算技能交集
        Set<String> intersection = new HashSet<>(reviewerSkillSet);
        intersection.retainAll(revieweeSkillSet);
        
        // 计算技能并集
        Set<String> union = new HashSet<>(reviewerSkillSet);
        union.addAll(revieweeSkillSet);
        
        // Jaccard相似度
        double similarity = (double) intersection.size() / union.size();
        
        // 转换为0-1分数，相似度越高分数越高
        return similarity;
    }
    
    /**
     * 计算避重分数（避免近期重复分配）
     */
    private double calculateAvoidanceScore(Long reviewerId, Long revieweeId, LocalDate weekStart) {
        LocalDate sinceDate = weekStart.minusWeeks(avoidanceWeeks);
        
        List<ReviewAssignment> recentAssignments = assignmentRepository.findPairAssignmentsSince(
            null, reviewerId, revieweeId, sinceDate);
        
        if (recentAssignments.isEmpty()) {
            return 1.0; // 没有近期分配，分数最高
        }
        
        // 根据最近分配的时间计算分数
        LocalDate lastAssignmentDate = recentAssignments.stream()
            .map(ReviewAssignment::getWeekStartDate)
            .max(LocalDate::compareTo)
            .orElse(sinceDate);
        
        long weeksSinceLastAssignment = java.time.temporal.ChronoUnit.WEEKS.between(lastAssignmentDate, weekStart);
        
        // 分数随时间线性增长
        return Math.min(1.0, (double) weeksSinceLastAssignment / avoidanceWeeks);
    }
    
    /**
     * 计算负载均衡分数
     */
    private double calculateLoadBalanceScore(Long reviewerId, Long revieweeId, LocalDate weekStart) {
        LocalDate startDate = weekStart.minusWeeks(4); // 考虑最近4周的负载
        LocalDate endDate = weekStart.plusWeeks(1);
        
        int reviewerLoad = assignmentRepository.countUserAssignments(reviewerId, startDate, endDate);
        int revieweeLoad = assignmentRepository.countUserAssignments(revieweeId, startDate, endDate);
        
        // 负载越低，分数越高
        double reviewerScore = Math.max(0, 1.0 - (double) reviewerLoad / maxAssignmentsPerWeek);
        double revieweeScore = Math.max(0, 1.0 - (double) revieweeLoad / maxAssignmentsPerWeek);
        
        // 取两者的平均值
        return (reviewerScore + revieweeScore) / 2.0;
    }
    
    /**
     * 计算多样性分数
     */
    private double calculateDiversityScore(User reviewer, User reviewee) {
        double score = 0.0;
        
        // 不同级别的用户互相评审可以增加多样性
        if (!Objects.equals(reviewer.getLevel(), reviewee.getLevel())) {
            score += 0.3;
        }
        
        // 不同角色的用户互相评审可以增加多样性
        if (!Objects.equals(reviewer.getRole(), reviewee.getRole())) {
            score += 0.2;
        }
        
        // 经验丰富的用户评审新人可以增加多样性
        if (isExperiencedUser(reviewer) && isNewUser(reviewee)) {
            score += 0.5;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * 判断是否为经验丰富的用户
     */
    private boolean isExperiencedUser(User user) {
        // 简单判断：创建时间超过6个月
        return user.getCreatedAt().isBefore(LocalDate.now().minusMonths(6).atStartOfDay());
    }
    
    /**
     * 判断是否为新用户
     */
    private boolean isNewUser(User user) {
        // 简单判断：创建时间不超过3个月
        return user.getCreatedAt().isAfter(LocalDate.now().minusMonths(3).atStartOfDay());
    }
    
    /**
     * 转换为ReviewAssignment实体
     */
    private List<ReviewAssignment> convertToReviewAssignments(
            List<HungarianAlgorithm.Assignment> optimalAssignments,
            MatchingMatrix matrix, Long teamId, LocalDate weekStart) {
        
        List<ReviewAssignment> assignments = new ArrayList<>();
        
        for (HungarianAlgorithm.Assignment assignment : optimalAssignments) {
            Long reviewerId = matrix.getUserId(assignment.getReviewerIndex());
            Long revieweeId = matrix.getUserId(assignment.getRevieweeIndex());
            
            if (reviewerId != null && revieweeId != null && !reviewerId.equals(revieweeId)) {
                ReviewAssignment reviewAssignment = new ReviewAssignment();
                reviewAssignment.setTeamId(teamId);
                reviewAssignment.setReviewerId(reviewerId);
                reviewAssignment.setRevieweeId(revieweeId);
                reviewAssignment.setWeekStartDate(weekStart);
                reviewAssignment.setStatus(ReviewAssignment.AssignmentStatus.ASSIGNED);
                reviewAssignment.setTotalScore(-assignment.getCost()); // 转回正分数
                reviewAssignment.setIsManualAdjusted(false);
                
                assignments.add(reviewAssignment);
            }
        }
        
        return assignments;
    }
    
    /**
     * 查询团队的分配历史
     */
    public List<ReviewAssignment> getTeamAssignmentHistory(Long teamId, LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.findByTeamIdAndDateRange(teamId, startDate, endDate);
    }
    
    /**
     * 查询用户的分配历史
     */
    public List<ReviewAssignment> getUserAssignmentHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * 手动调整分配
     */
    @Transactional
    public ReviewAssignment adjustAssignment(Long assignmentId, Long newRevieweeId, String remarks) {
        ReviewAssignment assignment = assignmentRepository.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("分配记录不存在");
        }
        
        assignment.setRevieweeId(newRevieweeId);
        assignment.setIsManualAdjusted(true);
        assignment.setRemarks(remarks);
        
        assignmentRepository.updateById(assignment);
        
        log.info("手动调整分配: {} -> {}, 备注: {}", assignmentId, newRevieweeId, remarks);
        
        return assignment;
    }
    
    /**
     * 删除分配
     */
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        ReviewAssignment assignment = assignmentRepository.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("分配记录不存在");
        }
        
        if (assignment.getStatus() == ReviewAssignment.AssignmentStatus.COMPLETED) {
            throw new RuntimeException("已完成的分配不能删除");
        }
        
        assignmentRepository.deleteById(assignmentId);
        log.info("删除分配: {}", assignmentId);
    }
    
    /**
     * 获取所有团队的分配历史
     */
    public List<ReviewAssignment> getAllAssignmentHistory(LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * 获取当前用户的分配
     */
    public List<ReviewAssignment> getCurrentUserAssignments(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return assignmentRepository.findByUserIdAndDateRange(userId, weekStart, weekEnd);
    }
    
    /**
     * 批量删除分配
     */
    @Transactional
    public void batchDeleteAssignments(List<Long> assignmentIds) {
        for (Long assignmentId : assignmentIds) {
            try {
                deleteAssignment(assignmentId);
            } catch (Exception e) {
                log.warn("删除分配失败: assignmentId={}, error={}", assignmentId, e.getMessage());
            }
        }
    }
    
    /**
     * 更新分配状态
     */
    @Transactional
    public ReviewAssignment updateAssignmentStatus(Long assignmentId, ReviewAssignment.AssignmentStatus status) {
        ReviewAssignment assignment = assignmentRepository.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("分配记录不存在");
        }
        
        assignment.setStatus(status);
        assignmentRepository.updateById(assignment);
        
        log.info("更新分配状态: {} -> {}", assignmentId, status);
        return assignment;
    }
    
    /**
     * 获取分配详情
     */
    public ReviewAssignment getAssignmentDetail(Long assignmentId) {
        ReviewAssignment assignment = assignmentRepository.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("分配记录不存在");
        }
        return assignment;
    }
    
    /**
     * 检查分配冲突
     */
    public List<String> checkAssignmentConflicts(Long teamId, LocalDate weekStart) {
        List<String> conflicts = new ArrayList<>();
        List<ReviewAssignment> assignments = getTeamAssignmentHistory(teamId, weekStart, weekStart.plusDays(6));
        
        // 检查重复分配
        Set<String> pairSet = new HashSet<>();
        for (ReviewAssignment assignment : assignments) {
            String pair = assignment.getReviewerId() + "-" + assignment.getRevieweeId();
            if (pairSet.contains(pair)) {
                conflicts.add("发现重复分配: 评审者" + assignment.getReviewerId() + " -> 被评审者" + assignment.getRevieweeId());
            }
            pairSet.add(pair);
        }
        
        // 检查自己评审自己
        for (ReviewAssignment assignment : assignments) {
            if (assignment.getReviewerId().equals(assignment.getRevieweeId())) {
                conflicts.add("发现自己评审自己: 用户" + assignment.getReviewerId());
            }
        }
        
        // 检查负载超限
        Map<Long, Integer> userLoadMap = new HashMap<>();
        for (ReviewAssignment assignment : assignments) {
            userLoadMap.merge(assignment.getReviewerId(), 1, Integer::sum);
            userLoadMap.merge(assignment.getRevieweeId(), 1, Integer::sum);
        }
        
        for (Map.Entry<Long, Integer> entry : userLoadMap.entrySet()) {
            if (entry.getValue() > maxAssignmentsPerWeek) {
                conflicts.add("用户" + entry.getKey() + "的分配数量(" + entry.getValue() + ")超过限制(" + maxAssignmentsPerWeek + ")");
            }
        }
        
        return conflicts;
    }
    
    /**
     * 获取用户的分配统计
     */
    public Map<String, Object> getUserAssignmentStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ReviewAssignment> assignments = getUserAssignmentHistory(userId, startDate, endDate);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAssignments", assignments.size());
        statistics.put("asReviewerCount", assignments.stream()
            .mapToInt(a -> a.getReviewerId().equals(userId) ? 1 : 0).sum());
        statistics.put("asRevieweeCount", assignments.stream()
            .mapToInt(a -> a.getRevieweeId().equals(userId) ? 1 : 0).sum());
        statistics.put("completedCount", assignments.stream()
            .mapToInt(a -> a.getStatus() == ReviewAssignment.AssignmentStatus.COMPLETED ? 1 : 0).sum());
        statistics.put("inProgressCount", assignments.stream()
            .mapToInt(a -> a.getStatus() == ReviewAssignment.AssignmentStatus.IN_PROGRESS ? 1 : 0).sum());
        statistics.put("averageScore", assignments.stream()
            .filter(a -> a.getTotalScore() != null)
            .mapToDouble(ReviewAssignment::getTotalScore)
            .average().orElse(0.0));
        
        return statistics;
    }
    
    /**
     * 预览分配结果（不保存到数据库）
     */
    public List<ReviewAssignment> previewWeeklyAssignments(Long teamId, LocalDate weekStart) {
        log.info("预览团队 {} 在 {} 周的评审分配", teamId, weekStart);
        
        // 1. 获取团队活跃成员列表
        List<User> activeMembers = getActiveTeamMembers(teamId);
        if (activeMembers.size() < 2) {
            log.warn("团队 {} 活跃成员不足2人，无法进行分配", teamId);
            return new ArrayList<>();
        }
        
        // 2. 计算成员间的匹配度矩阵
        MatchingMatrix matrix = calculateMatchingMatrix(activeMembers, weekStart);
        
        // 3. 使用匈牙利算法求解最优分配
        int[] assignmentArray = hungarianAlgorithm.solve(matrix.getMatrix());
        List<HungarianAlgorithm.Assignment> optimalAssignments = new ArrayList<>();
        for (int i = 0; i < assignmentArray.length; i++) {
            if (assignmentArray[i] != -1) {
                optimalAssignments.add(new HungarianAlgorithm.Assignment(i, assignmentArray[i], matrix.getMatrix()[i][assignmentArray[i]]));
            }
        }
        
        // 4. 转换为ReviewAssignment实体（但不保存）
        List<ReviewAssignment> assignments = convertToReviewAssignments(
            optimalAssignments, matrix, teamId, weekStart);
        
        log.info("预览完成，团队 {} 将生成 {} 个评审分配", teamId, assignments.size());
        return assignments;
    }
}