import request from '@/utils/request'

export const issueApi = {
  // 问题管理
  createIssue(data) {
    return request({
      url: '/api/issues',
      method: 'post',
      data
    })
  },

  updateIssue(id, data) {
    return request({
      url: `/api/issues/${id}`,
      method: 'put',
      data
    })
  },

  deleteIssue(id) {
    return request({
      url: `/api/issues/${id}`,
      method: 'delete'
    })
  },

  getIssueById(id) {
    return request({
      url: `/api/issues/${id}`,
      method: 'get'
    })
  },

  getIssueWithFixRecords(id) {
    return request({
      url: `/api/issues/${id}/with-fix-records`,
      method: 'get'
    })
  },

  getIssuesByReviewRecord(reviewRecordId) {
    return request({
      url: `/api/issues/by-review-record/${reviewRecordId}`,
      method: 'get'
    })
  },

  getIssuesAssignedToUser(userId, params = {}) {
    return request({
      url: `/api/issues/assigned-to-user/${userId}`,
      method: 'get',
      params
    })
  },

  getIssuesByTeam(teamId, params = {}) {
    return request({
      url: `/api/issues/by-team/${teamId}`,
      method: 'get',
      params
    })
  },

  getIssuesByTypeAndSeverity(params) {
    return request({
      url: '/api/issues/by-type-and-severity',
      method: 'get',
      params
    })
  },

  getPendingFixIssues(userId) {
    return request({
      url: `/api/issues/pending-fix/${userId}`,
      method: 'get'
    })
  },

  updateIssueStatus(id, status) {
    return request({
      url: `/api/issues/${id}/status`,
      method: 'put',
      params: { status }
    })
  },

  batchUpdateStatus(ids, status, updatedBy) {
    return request({
      url: '/api/issues/batch-update-status',
      method: 'put',
      params: { ids: ids.join(','), status, updatedBy }
    })
  },

  closeIssue(id, reason) {
    return request({
      url: `/api/issues/${id}/close`,
      method: 'put',
      params: { reason }
    })
  },

  reopenIssue(id, reason) {
    return request({
      url: `/api/issues/${id}/reopen`,
      method: 'put',
      params: { reason }
    })
  },

  // 统计分析
  getTeamIssueStatistics(teamId, params = {}) {
    return request({
      url: `/api/issues/statistics/team/${teamId}`,
      method: 'get',
      params
    })
  },

  getUserIssueStatistics(userId, params = {}) {
    return request({
      url: `/api/issues/statistics/user/${userId}`,
      method: 'get',
      params
    })
  },

  getFrequentIssues(teamId, params = {}) {
    return request({
      url: `/api/issues/frequent/${teamId}`,
      method: 'get',
      params
    })
  },

  countIssuesByType(teamId, params = {}) {
    return request({
      url: `/api/issues/count-by-type/${teamId}`,
      method: 'get',
      params
    })
  },

  countIssuesBySeverity(teamId, params = {}) {
    return request({
      url: `/api/issues/count-by-severity/${teamId}`,
      method: 'get',
      params
    })
  },

  // 智能分类和模板
  getClassificationSuggestion(title, description) {
    return request({
      url: '/api/issues/classification-suggestion',
      method: 'post',
      params: { title, description }
    })
  },

  getIssueTemplates() {
    return request({
      url: '/api/issues/templates',
      method: 'get'
    })
  },

  getIssueTemplatesByType(issueType) {
    return request({
      url: '/api/issues/templates/by-type',
      method: 'get',
      params: { issueType }
    })
  },

  createIssueFromTemplate(templateId, reviewRecordId, parameters) {
    return request({
      url: '/api/issues/create-from-template',
      method: 'post',
      params: { templateId, reviewRecordId },
      data: parameters
    })
  },

  // 整改记录管理
  createFixRecord(data) {
    return request({
      url: '/api/fix-records',
      method: 'post',
      data
    })
  },

  submitFixRecord(issueId, data) {
    return request({
      url: `/api/fix-records/submit/${issueId}`,
      method: 'post',
      data
    })
  },

  updateFixRecord(id, data) {
    return request({
      url: `/api/fix-records/${id}`,
      method: 'put',
      data
    })
  },

  deleteFixRecord(id) {
    return request({
      url: `/api/fix-records/${id}`,
      method: 'delete'
    })
  },

  getFixRecordById(id) {
    return request({
      url: `/api/fix-records/${id}`,
      method: 'get'
    })
  },

  getFixRecordsByIssue(issueId) {
    return request({
      url: `/api/fix-records/by-issue/${issueId}`,
      method: 'get'
    })
  },

  getFixRecordsByFixer(fixerId, params = {}) {
    return request({
      url: `/api/fix-records/by-fixer/${fixerId}`,
      method: 'get',
      params
    })
  },

  getPendingVerificationRecords(verifierId) {
    return request({
      url: `/api/fix-records/pending-verification/${verifierId}`,
      method: 'get'
    })
  },

  getFixRecordsByStatus(status, teamId) {
    return request({
      url: '/api/fix-records/by-status',
      method: 'get',
      params: { status, teamId }
    })
  },

  submitForVerification(id, verifierId) {
    return request({
      url: `/api/fix-records/${id}/submit-for-verification`,
      method: 'put',
      params: { verifierId }
    })
  },

  verifyFixRecord(id, data) {
    return request({
      url: `/api/fix-records/${id}/verify`,
      method: 'put',
      data
    })
  },

  verifyFixRecordWithTracking(id, data) {
    return request({
      url: `/api/fix-records/${id}/verify-with-tracking`,
      method: 'put',
      data
    })
  },

  batchUpdateFixRecordStatus(ids, status, updatedBy) {
    return request({
      url: '/api/fix-records/batch-update-status',
      method: 'put',
      params: { ids: ids.join(','), status, updatedBy }
    })
  },

  getLatestFixRecord(issueId) {
    return request({
      url: `/api/fix-records/latest/${issueId}`,
      method: 'get'
    })
  },

  countFixRecordsByUser(fixerId, params = {}) {
    return request({
      url: `/api/fix-records/count/${fixerId}`,
      method: 'get',
      params
    })
  },

  calculateFixTimeliness(userId, params = {}) {
    return request({
      url: `/api/fix-records/timeliness/${userId}`,
      method: 'get',
      params
    })
  },

  resubmitFixRecord(id, params = {}) {
    return request({
      url: `/api/fix-records/${id}/resubmit`,
      method: 'put',
      params
    })
  },

  getFixRecordStatistics(teamId, params = {}) {
    return request({
      url: `/api/fix-records/statistics/${teamId}`,
      method: 'get',
      params
    })
  },

  // 问题跟踪
  getTrackingDashboard(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/dashboard/${teamId}`,
      method: 'get',
      params
    })
  },

  getIssueTrendData(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/trend/${teamId}`,
      method: 'get',
      params
    })
  },

  getHotIssues(teamId, limit = 10) {
    return request({
      url: `/api/issue-tracking/hot-issues/${teamId}`,
      method: 'get',
      params: { limit }
    })
  },

  getUserIssueAssignments(teamId) {
    return request({
      url: `/api/issue-tracking/user-assignments/${teamId}`,
      method: 'get'
    })
  },

  autoAssignIssues(teamId) {
    return request({
      url: `/api/issue-tracking/auto-assign/${teamId}`,
      method: 'post'
    })
  },

  escalateOverdueIssues() {
    return request({
      url: '/api/issue-tracking/escalate-overdue',
      method: 'post'
    })
  },

  getResolutionStats(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/resolution-stats/${teamId}`,
      method: 'get',
      params
    })
  },

  getFixTrackingDetail(issueId) {
    return request({
      url: `/api/issue-tracking/fix-tracking/${issueId}`,
      method: 'get'
    })
  },

  getUserFixTasks(userId) {
    return request({
      url: `/api/issue-tracking/user-fix-tasks/${userId}`,
      method: 'get'
    })
  },

  getFixEffectivenessReport(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/effectiveness-report/${teamId}`,
      method: 'get',
      params
    })
  },

  // 工作流管理
  startFixWorkflow(issueId, assigneeId) {
    return request({
      url: `/api/issue-tracking/workflow/start/${issueId}`,
      method: 'post',
      params: { assigneeId }
    })
  },

  advanceWorkflow(issueId, action, parameters = {}) {
    return request({
      url: `/api/issue-tracking/workflow/advance/${issueId}`,
      method: 'post',
      params: { action },
      data: parameters
    })
  },

  getCurrentWorkflow(issueId) {
    return request({
      url: `/api/issue-tracking/workflow/current/${issueId}`,
      method: 'get'
    })
  },

  getOverdueWorkflows() {
    return request({
      url: '/api/issue-tracking/workflow/overdue',
      method: 'get'
    })
  },

  handleOverdueWorkflows() {
    return request({
      url: '/api/issue-tracking/workflow/handle-overdue',
      method: 'post'
    })
  },

  getWorkflowStatistics(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/workflow/statistics/${teamId}`,
      method: 'get',
      params
    })
  },

  // 度量指标
  getFixEfficiencyMetrics(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/metrics/efficiency/${teamId}`,
      method: 'get',
      params
    })
  },

  getQualityImprovementMetrics(teamId, params) {
    return request({
      url: `/api/issue-tracking/metrics/quality-improvement/${teamId}`,
      method: 'get',
      params
    })
  },

  getMemberPerformanceMetrics(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/metrics/member-performance/${teamId}`,
      method: 'get',
      params
    })
  },

  generateFixTrendReport(teamId, params = {}) {
    return request({
      url: `/api/issue-tracking/metrics/trend-report/${teamId}`,
      method: 'get',
      params
    })
  },

  // 导出功能
  exportIssues(params = {}) {
    return request({
      url: '/api/issues/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 批量删除
  batchDelete(ids) {
    return request({
      url: '/api/issues/batch-delete',
      method: 'delete',
      data: { ids }
    })
  }
}

export default issueApi