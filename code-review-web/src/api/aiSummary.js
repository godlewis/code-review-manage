import request from '@/utils/request'

/**
 * AI Summary API
 */

/**
 * Generate AI summary
 */
export function generateSummary(data) {
  return request({
    url: '/api/ai-summaries/generate',
    method: 'post',
    data
  })
}

/**
 * Get summary by ID
 */
export function getSummaryById(summaryId) {
  return request({
    url: `/api/ai-summaries/${summaryId}`,
    method: 'get'
  })
}

/**
 * Get team summaries
 */
export function getTeamSummaries(teamId, status) {
  return request({
    url: `/api/ai-summaries/team/${teamId}`,
    method: 'get',
    params: { status }
  })
}

/**
 * Get summaries by date range
 */
export function getSummariesByDateRange(params) {
  return request({
    url: '/api/ai-summaries/date-range',
    method: 'get',
    params
  })
}

/**
 * Get latest summary by type
 */
export function getLatestSummary(summaryType, teamId) {
  return request({
    url: '/api/ai-summaries/latest',
    method: 'get',
    params: { summaryType, teamId }
  })
}

/**
 * Update summary
 */
export function updateSummary(summaryId, data) {
  return request({
    url: `/api/ai-summaries/${summaryId}`,
    method: 'put',
    data
  })
}

/**
 * Publish summary
 */
export function publishSummary(summaryId) {
  return request({
    url: `/api/ai-summaries/${summaryId}/publish`,
    method: 'post'
  })
}

/**
 * Archive summary
 */
export function archiveSummary(summaryId) {
  return request({
    url: `/api/ai-summaries/${summaryId}/archive`,
    method: 'post'
  })
}

/**
 * Delete summary
 */
export function deleteSummary(summaryId) {
  return request({
    url: `/api/ai-summaries/${summaryId}`,
    method: 'delete'
  })
}

/**
 * Compare summaries
 */
export function compareSummaries(currentSummaryId, previousSummaryId) {
  return request({
    url: `/api/ai-summaries/${currentSummaryId}/compare/${previousSummaryId}`,
    method: 'get'
  })
}

/**
 * Generate team weekly summary
 */
export function generateTeamWeeklySummary(teamId, weekStart) {
  return request({
    url: `/api/ai-summaries/team/${teamId}/weekly`,
    method: 'post',
    params: { weekStart }
  })
}

/**
 * Generate architect weekly summary
 */
export function generateArchitectWeeklySummary(weekStart) {
  return request({
    url: '/api/ai-summaries/architect/weekly',
    method: 'post',
    params: { weekStart }
  })
}