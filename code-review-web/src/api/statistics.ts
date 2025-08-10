import request from '@/utils/request'

// 统计数据类型定义
export interface PersonalStatistics {
  completionRate: number
  issuesFound: number
  fixTimeliness: number
  averageReviewScore: number
  totalReviews: number
  completedReviews: number
  pendingIssues: number
  resolvedIssues: number
  growthTrend: GrowthPoint[]
  issueTypeDistribution: Record<string, number>
  severityDistribution: Record<string, number>
  monthlyStats: MonthlyStatistics[]
}

export interface TeamStatistics {
  coverageRate: number
  averageScore: number
  totalIssues: number
  resolvedIssues: number
  resolutionRate: number
  memberCount: number
  activeMemberCount: number
  issueDistribution: Record<string, number>
  severityDistribution: Record<string, number>
  qualityTrend: QualityPoint[]
  memberRankings: MemberPerformance[]
  frequentIssues: FrequentIssueStats[]
  monthlyComparisons: MonthlyComparison[]
}

export interface GlobalStatistics {
  totalTeams: number
  totalUsers: number
  activeUsers: number
  totalReviews: number
  totalIssues: number
  globalResolutionRate: number
  globalAverageScore: number
  teamRankings: TeamPerformance[]
  crossTeamIssueDistribution: Record<string, number>
  usageTrends: UsageTrend[]
  qualityTrends: QualityImprovementTrend[]
  bestPracticeTeams: BestPracticeTeam[]
}

export interface GrowthPoint {
  date: string
  completionRate: number
  issuesFound: number
  averageScore: number
  fixTimeliness: number
}

export interface QualityPoint {
  date: string
  averageQualityScore: number
  issueDensity: number
  criticalIssueRatio: number
  resolutionRate: number
}

export interface MonthlyStatistics {
  yearMonth: string
  reviewCount: number
  issueCount: number
  averageScore: number
  completionRate: number
  fixTimeliness: number
}

export interface MemberPerformance {
  userId: number
  username: string
  realName: string
  completionRate: number
  issuesFound: number
  averageScore: number
  fixTimeliness: number
  rank: number
  overallScore: number
}

export interface FrequentIssueStats {
  issueDescription: string
  issueType: string
  occurrenceCount: number
  percentage: number
  averageSeverity: string
  resolutionRate: number
}

export interface MonthlyComparison {
  yearMonth: string
  currentMonth: MonthlyStatistics
  previousMonth: MonthlyStatistics
  reviewCountChangeRate: number
  issueCountChangeRate: number
  scoreChangeRate: number
  completionRateChange: number
}

export interface TeamPerformance {
  teamId: number
  teamName: string
  coverageRate: number
  averageScore: number
  resolutionRate: number
  memberCount: number
  rank: number
  overallScore: number
  improvementTrend: string
}

export interface UsageTrend {
  date: string
  activeUsers: number
  reviewCount: number
  issueCount: number
  usageRate: number
}

export interface QualityImprovementTrend {
  date: string
  globalAverageScore: number
  resolutionRate: number
  criticalIssueRatio: number
  qualityImprovementIndex: number
}

export interface BestPracticeTeam {
  teamId: number
  teamName: string
  category: string
  practiceDescription: string
  keyMetric: string
  metricValue: number
  recommendationReason: string
}

export interface StatisticsOverview {
  personalStats: PersonalStatistics
  teamStats?: TeamStatistics
  globalStats?: GlobalStatistics
  quickMetrics: {
    todayReviews: number
    pendingIssues: number
    weeklyCompletionRate: number
    qualityTrend: string
    rankingChange: number
  }
}

// API 接口
export const statisticsApi = {
  // 获取个人统计数据
  getPersonalStatistics(userId: number, startDate: string, endDate: string) {
    return request.get<PersonalStatistics>(`/statistics/personal/${userId}`, {
      params: { startDate, endDate }
    })
  },

  // 获取当前用户统计数据
  getCurrentUserStatistics(startDate: string, endDate: string) {
    return request.get<PersonalStatistics>('/statistics/personal/current', {
      params: { startDate, endDate }
    })
  },

  // 获取团队统计数据
  getTeamStatistics(teamId: number, startDate: string, endDate: string) {
    return request.get<TeamStatistics>(`/statistics/team/${teamId}`, {
      params: { startDate, endDate }
    })
  },

  // 获取全局统计数据
  getGlobalStatistics(startDate: string, endDate: string) {
    return request.get<GlobalStatistics>('/statistics/global', {
      params: { startDate, endDate }
    })
  },

  // 获取个人成长趋势
  getPersonalGrowthTrend(userId: number, startDate: string, endDate: string) {
    return request.get<PersonalStatistics>(`/statistics/personal/${userId}/growth-trend`, {
      params: { startDate, endDate }
    })
  },

  // 获取团队质量趋势
  getTeamQualityTrend(teamId: number, startDate: string, endDate: string) {
    return request.get<TeamStatistics>(`/statistics/team/${teamId}/quality-trend`, {
      params: { startDate, endDate }
    })
  },

  // 获取团队成员排名
  getTeamMemberRankings(teamId: number, startDate: string, endDate: string) {
    return request.get<TeamStatistics>(`/statistics/team/${teamId}/member-rankings`, {
      params: { startDate, endDate }
    })
  },

  // 获取系统使用趋势
  getSystemUsageTrend(startDate: string, endDate: string) {
    return request.get<GlobalStatistics>('/statistics/global/usage-trend', {
      params: { startDate, endDate }
    })
  },

  // 获取统计概览
  getStatisticsOverview() {
    return request.get<StatisticsOverview>('/statistics/overview')
  },

  // 导出个人统计报表
  exportPersonalStatistics(userId: number, startDate: string, endDate: string, format: string = 'excel') {
    return request.get(`/statistics/personal/${userId}/export`, {
      params: { startDate, endDate, format },
      responseType: 'blob'
    })
  },

  // 导出团队统计报表
  exportTeamStatistics(teamId: number, startDate: string, endDate: string, format: string = 'excel') {
    return request.get(`/statistics/team/${teamId}/export`, {
      params: { startDate, endDate, format },
      responseType: 'blob'
    })
  },

  // 导出全局统计报表
  exportGlobalStatistics(startDate: string, endDate: string, format: string = 'excel') {
    return request.get('/statistics/global/export', {
      params: { startDate, endDate, format },
      responseType: 'blob'
    })
  }
}