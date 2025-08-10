package com.company.codereview.user.service;

import com.company.codereview.user.algorithm.HungarianAlgorithm;
import com.company.codereview.user.dto.MatchingMatrix;
import com.company.codereview.user.entity.ReviewAssignment;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.ReviewAssignmentRepository;
import com.company.codereview.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评审分配服务测试
 */
@ExtendWith(MockitoExtension.class)
class ReviewAssignmentServiceTest {
    
    @Mock
    private ReviewAssignmentRepository assignmentRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private HungarianAlgorithm hungarianAlgorithm;
    
    @InjectMocks
    private ReviewAssignmentService reviewAssignmentService;
    
    @BeforeEach
    void setUp() {
        // 设置配置参数
        ReflectionTestUtils.setField(reviewAssignmentService, "avoidanceWeeks", 4);
        ReflectionTestUtils.setField(reviewAssignmentService, "maxAssignmentsPerWeek", 3);
        ReflectionTestUtils.setField(reviewAssignmentService, "skillMatchWeight", 0.4);
        ReflectionTestUtils.setField(reviewAssignmentService, "loadBalanceWeight", 0.3);
        ReflectionTestUtils.setField(reviewAssignmentService, "diversityWeight", 0.3);
    }
    
    @Test
    void testGenerateWeeklyAssignments_Success() {
        // 准备测试数据
        Long teamId = 1L;
        LocalDate weekStart = LocalDate.now();
        
        List<User> activeMembers = createTestUsers();
        when(userRepository.findByTeamId(teamId)).thenReturn(activeMembers);
        
        // Mock匈牙利算法结果
        List<HungarianAlgorithm.Assignment> mockAssignments = Arrays.asList(
            createMockAssignment(0, 1, -0.8),
            createMockAssignment(1, 0, -0.7)
        );
        when(hungarianAlgorithm.solve(any(double[][].class))).thenReturn(mockAssignments);
        
        when(assignmentRepository.insertBatch(anyList())).thenReturn(2);
        
        // 执行测试
        List<ReviewAssignment> result = reviewAssignmentService.generateWeeklyAssignments(teamId, weekStart);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证第一个分配
        ReviewAssignment assignment1 = result.get(0);
        assertEquals(teamId, assignment1.getTeamId());
        assertEquals(1L, assignment1.getReviewerId());
        assertEquals(2L, assignment1.getRevieweeId());
        assertEquals(weekStart, assignment1.getWeekStartDate());
        assertEquals(ReviewAssignment.AssignmentStatus.ASSIGNED, assignment1.getStatus());
        assertEquals(0.8, assignment1.getTotalScore(), 0.01);
        assertFalse(assignment1.getIsManualAdjusted());
        
        // 验证方法调用
        verify(userRepository).findByTeamId(teamId);
        verify(hungarianAlgorithm).solve(any(double[][].class));
        verify(assignmentRepository).insertBatch(anyList());
    }
    
    @Test
    void testGenerateWeeklyAssignments_InsufficientMembers() {
        // 准备测试数据 - 只有一个成员
        Long teamId = 1L;
        LocalDate weekStart = LocalDate.now();
        
        List<User> activeMembers = Arrays.asList(createTestUser(1L, "user1"));
        when(userRepository.findByTeamId(teamId)).thenReturn(activeMembers);
        
        // 执行测试
        List<ReviewAssignment> result = reviewAssignmentService.generateWeeklyAssignments(teamId, weekStart);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证没有调用匈牙利算法和数据库插入
        verify(hungarianAlgorithm, never()).solve(any(double[][].class));
        verify(assignmentRepository, never()).insertBatch(anyList());
    }
    
    @Test
    void testAdjustAssignment_Success() {
        // 准备测试数据
        Long assignmentId = 1L;
        Long newRevieweeId = 3L;
        String remarks = "手动调整测试";
        
        ReviewAssignment existingAssignment = new ReviewAssignment();
        existingAssignment.setId(assignmentId);
        existingAssignment.setReviewerId(1L);
        existingAssignment.setRevieweeId(2L);
        existingAssignment.setIsManualAdjusted(false);
        
        when(assignmentRepository.selectById(assignmentId)).thenReturn(existingAssignment);
        when(assignmentRepository.updateById(any(ReviewAssignment.class))).thenReturn(1);
        
        // 执行测试
        ReviewAssignment result = reviewAssignmentService.adjustAssignment(assignmentId, newRevieweeId, remarks);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(newRevieweeId, result.getRevieweeId());
        assertTrue(result.getIsManualAdjusted());
        assertEquals(remarks, result.getRemarks());
        
        // 验证方法调用
        verify(assignmentRepository).selectById(assignmentId);
        verify(assignmentRepository).updateById(any(ReviewAssignment.class));
    }
    
    @Test
    void testAdjustAssignment_NotFound() {
        // 准备测试数据
        Long assignmentId = 1L;
        Long newRevieweeId = 3L;
        String remarks = "手动调整测试";
        
        when(assignmentRepository.selectById(assignmentId)).thenReturn(null);
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewAssignmentService.adjustAssignment(assignmentId, newRevieweeId, remarks);
        });
        
        assertEquals("分配记录不存在", exception.getMessage());
        
        // 验证没有调用更新方法
        verify(assignmentRepository, never()).updateById(any(ReviewAssignment.class));
    }
    
    /**
     * 创建测试用户列表
     */
    private List<User> createTestUsers() {
        return Arrays.asList(
            createTestUser(1L, "user1"),
            createTestUser(2L, "user2"),
            createTestUser(3L, "user3")
        );
    }
    
    /**
     * 创建测试用户
     */
    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRealName("测试用户" + id);
        user.setActive(true);
        user.setDeleted(false);
        user.setSkills("Java,Spring,MySQL");
        user.setCreatedAt(LocalDateTime.now().minusMonths(6));
        return user;
    }
    
    /**
     * 创建Mock分配结果
     */
    private HungarianAlgorithm.Assignment createMockAssignment(int reviewerIndex, int revieweeIndex, double cost) {
        HungarianAlgorithm.Assignment assignment = new HungarianAlgorithm.Assignment();
        assignment.setReviewerIndex(reviewerIndex);
        assignment.setRevieweeIndex(revieweeIndex);
        assignment.setCost(cost);
        return assignment;
    }
}