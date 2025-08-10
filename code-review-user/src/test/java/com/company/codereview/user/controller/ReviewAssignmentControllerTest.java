package com.company.codereview.user.controller;

import com.company.codereview.user.entity.ReviewAssignment;
import com.company.codereview.user.service.ReviewAssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 评审分配控制器测试
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReviewAssignmentController.class)
class ReviewAssignmentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ReviewAssignmentService assignmentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ReviewAssignment testAssignment;
    
    @BeforeEach
    void setUp() {
        testAssignment = new ReviewAssignment();
        testAssignment.setId(1L);
        testAssignment.setTeamId(1L);
        testAssignment.setReviewerId(1L);
        testAssignment.setRevieweeId(2L);
        testAssignment.setWeekStartDate(LocalDate.now());
        testAssignment.setStatus(ReviewAssignment.AssignmentStatus.ASSIGNED);
        testAssignment.setTotalScore(0.85);
        testAssignment.setIsManualAdjusted(false);
        testAssignment.setCreatedAt(LocalDateTime.now());
        testAssignment.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testGenerateWeeklyAssignments() throws Exception {
        // 准备测试数据
        List<ReviewAssignment> assignments = Arrays.asList(testAssignment);
        when(assignmentService.generateWeeklyAssignments(anyLong(), any(LocalDate.class)))
            .thenReturn(assignments);
        
        // 执行测试
        mockMvc.perform(post("/api/review-assignments/generate")
                .with(csrf())
                .param("teamId", "1")
                .param("weekStart", "2024-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].teamId").value(1))
                .andExpect(jsonPath("$.data[0].reviewerId").value(1))
                .andExpect(jsonPath("$.data[0].revieweeId").value(2));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testPreviewAssignments() throws Exception {
        // 准备测试数据
        List<ReviewAssignment> assignments = Arrays.asList(testAssignment);
        when(assignmentService.previewWeeklyAssignments(anyLong(), any(LocalDate.class)))
            .thenReturn(assignments);
        
        // 执行测试
        mockMvc.perform(post("/api/review-assignments/preview")
                .with(csrf())
                .param("teamId", "1")
                .param("weekStart", "2024-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testGetTeamAssignmentHistory() throws Exception {
        // 准备测试数据
        List<ReviewAssignment> assignments = Arrays.asList(testAssignment);
        when(assignmentService.getTeamAssignmentHistory(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(assignments);
        
        // 执行测试
        mockMvc.perform(get("/api/review-assignments/team/1/history")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-07")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].teamId").value(1));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testGetAssignmentDetail() throws Exception {
        // 准备测试数据
        when(assignmentService.getAssignmentDetail(anyLong())).thenReturn(testAssignment);
        
        // 执行测试
        mockMvc.perform(get("/api/review-assignments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.teamId").value(1));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testAdjustAssignment() throws Exception {
        // 准备测试数据
        ReviewAssignment adjustedAssignment = new ReviewAssignment();
        adjustedAssignment.setId(1L);
        adjustedAssignment.setTeamId(1L);
        adjustedAssignment.setReviewerId(1L);
        adjustedAssignment.setRevieweeId(3L); // 调整后的被评审者
        adjustedAssignment.setIsManualAdjusted(true);
        adjustedAssignment.setRemarks("手动调整");
        
        when(assignmentService.adjustAssignment(anyLong(), anyLong(), anyString()))
            .thenReturn(adjustedAssignment);
        
        // 准备请求数据
        ReviewAssignmentController.AdjustAssignmentRequest request = 
            new ReviewAssignmentController.AdjustAssignmentRequest();
        request.setNewRevieweeId(3L);
        request.setRemarks("手动调整");
        
        // 执行测试
        mockMvc.perform(put("/api/review-assignments/1/adjust")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revieweeId").value(3))
                .andExpect(jsonPath("$.data.isManualAdjusted").value(true));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testUpdateAssignmentStatus() throws Exception {
        // 准备测试数据
        ReviewAssignment updatedAssignment = new ReviewAssignment();
        updatedAssignment.setId(1L);
        updatedAssignment.setStatus(ReviewAssignment.AssignmentStatus.IN_PROGRESS);
        
        when(assignmentService.updateAssignmentStatus(anyLong(), any(ReviewAssignment.AssignmentStatus.class)))
            .thenReturn(updatedAssignment);
        
        // 执行测试
        mockMvc.perform(put("/api/review-assignments/1/status")
                .with(csrf())
                .param("status", "IN_PROGRESS")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testDeleteAssignment() throws Exception {
        // 执行测试
        mockMvc.perform(delete("/api/review-assignments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testValidateAssignments() throws Exception {
        // 准备测试数据
        List<String> conflicts = Arrays.asList("发现重复分配: 1-2");
        when(assignmentService.checkAssignmentConflicts(anyLong(), any(LocalDate.class)))
            .thenReturn(conflicts);
        
        // 执行测试
        mockMvc.perform(post("/api/review-assignments/validate")
                .with(csrf())
                .param("teamId", "1")
                .param("weekStart", "2024-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.errors").isArray())
                .andExpect(jsonPath("$.data.errors[0]").value("发现重复分配: 1-2"));
    }
    
    @Test
    @WithMockUser(roles = "DEVELOPER")
    void testGetMyCurrentAssignments() throws Exception {
        // 准备测试数据
        List<ReviewAssignment> assignments = Arrays.asList(testAssignment);
        when(assignmentService.getCurrentUserAssignments(anyLong(), any(LocalDate.class)))
            .thenReturn(assignments);
        
        // 执行测试
        mockMvc.perform(get("/api/review-assignments/my-current")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @WithMockUser(roles = "TEAM_LEADER")
    void testCheckAssignmentConflicts() throws Exception {
        // 准备测试数据
        List<String> conflicts = Arrays.asList("发现自己评审自己: 用户1");
        when(assignmentService.checkAssignmentConflicts(anyLong(), any(LocalDate.class)))
            .thenReturn(conflicts);
        
        // 执行测试
        mockMvc.perform(get("/api/review-assignments/conflicts")
                .param("teamId", "1")
                .param("weekStart", "2024-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("发现自己评审自己: 用户1"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testGenerateWeeklyAssignments_AccessDenied() throws Exception {
        // 执行测试 - 普通用户应该被拒绝访问
        mockMvc.perform(post("/api/review-assignments/generate")
                .with(csrf())
                .param("teamId", "1")
                .param("weekStart", "2024-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}