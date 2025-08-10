import request from '@/utils/request'

// 评审记录相关接口
export interface ReviewRecord {
  id?: number
  assignmentId: number
  title: string
  codeRepository?: string
  codeFilePath?: string
  description?: string
  overallScore?: number
  summary?: string
  status?: string
  needsReReview?: boolean
  completedAt?: string
  createdAt?: string
  updatedAt?: string
  createdBy?: number
  updatedBy?: number
  screenshots?: CodeScreenshot[]
  issues?: Issue[]
  reviewerId?: number
  revieweeId?: number
  reviewerName?: string
  revieweeName?: string
  teamId?: number
  teamName?: string
}

export interface CodeScreenshot {
  id?: number
  reviewRecordId: number
  fileName: string
  fileUrl: string
  fileSize?: number
  fileType?: string
  description?: string
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

export interface Issue {
  id?: number
  reviewRecordId: number
  issueType: string
  severity: string
  title: string
  description: string
  suggestion?: string
  referenceLinks?: string
  status?: string
  lineNumber?: number
  codeSnippet?: string
  createdAt?: string
  updatedAt?: string
  fixRecords?: FixRecord[]
  fixRecordCount?: number
  latestFixStatus?: string
}

export interface FixRecord {
  id?: number
  issueId: number
  fixerId: number
  fixDescription: string
  beforeCodeUrl?: string
  afterCodeUrl?: string
  status?: string
  verifierId?: number
  verificationResult?: string
  verificationRemarks?: string
  verifiedAt?: string
  createdAt?: string
  updatedAt?: string
  fixerName?: string
  verifierName?: string
  issueTitle?: string
  issueDescription?: string
}

// 评审记录API
export const reviewRecordApi = {
  // 创建评审记录
  create: (data: ReviewRecord) => {
    return request({
      url: '/api/review-records',
      method: 'post',
      data
    })
  },

  // 更新评审记录
  update: (id: number, data: ReviewRecord) => {
    return request({
      url: `/api/review-records/${id}`,
      method: 'put',
      data
    })
  },

  // 删除评审记录
  delete: (id: number) => {
    return request({
      url: `/api/review-records/${id}`,
      method: 'delete'
    })
  },

  // 根据ID查询评审记录
  getById: (id: number) => {
    return request({
      url: `/api/review-records/${id}`,
      method: 'get'
    })
  },

  // 查询评审记录详情
  getDetails: (id: number) => {
    return request({
      url: `/api/review-records/${id}/details`,
      method: 'get'
    })
  },

  // 根据分配ID查询评审记录
  getByAssignment: (assignmentId: number) => {
    return request({
      url: `/api/review-records/assignment/${assignmentId}`,
      method: 'get'
    })
  },

  // 根据评审者ID查询评审记录
  getByReviewer: (reviewerId: number, params?: any) => {
    return request({
      url: `/api/review-records/reviewer/${reviewerId}`,
      method: 'get',
      params
    })
  },

  // 根据被评审者ID查询评审记录
  getByReviewee: (revieweeId: number, params?: any) => {
    return request({
      url: `/api/review-records/reviewee/${revieweeId}`,
      method: 'get',
      params
    })
  },

  // 根据团队ID查询评审记录
  getByTeam: (teamId: number, params?: any) => {
    return request({
      url: `/api/review-records/team/${teamId}`,
      method: 'get',
      params
    })
  },

  // 根据状态查询评审记录
  getByStatus: (status: string) => {
    return request({
      url: `/api/review-records/status/${status}`,
      method: 'get'
    })
  },

  // 查询需要重新评审的记录
  getNeedsReReview: (teamId: number) => {
    return request({
      url: `/api/review-records/team/${teamId}/re-review`,
      method: 'get'
    })
  },

  // 提交评审记录
  submit: (id: number) => {
    return request({
      url: `/api/review-records/${id}/submit`,
      method: 'post'
    })
  },

  // 标记需要重新评审
  markReReview: (id: number, reason: string) => {
    return request({
      url: `/api/review-records/${id}/mark-re-review`,
      method: 'post',
      params: { reason }
    })
  },

  // 复制评审记录
  copy: (id: number, newAssignmentId: number) => {
    return request({
      url: `/api/review-records/${id}/copy`,
      method: 'post',
      params: { newAssignmentId }
    })
  },

  // 统计评审记录数量
  countByReviewer: (reviewerId: number, params?: any) => {
    return request({
      url: `/api/review-records/reviewer/${reviewerId}/count`,
      method: 'get',
      params
    })
  },

  // 统计团队评审记录数量
  countByTeam: (teamId: number, params?: any) => {
    return request({
      url: `/api/review-records/team/${teamId}/count`,
      method: 'get',
      params
    })
  },

  // 批量更新状态
  batchUpdateStatus: (ids: number[], status: string, updatedBy: number) => {
    return request({
      url: '/api/review-records/batch/status',
      method: 'put',
      params: { ids: ids.join(','), status, updatedBy }
    })
  }
}

// 代码截图API
export const screenshotApi = {
  // 上传截图
  upload: (file: File, reviewRecordId: number, description?: string) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('reviewRecordId', reviewRecordId.toString())
    if (description) {
      formData.append('description', description)
    }
    
    return request({
      url: '/api/screenshots/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 批量上传截图
  batchUpload: (files: File[], reviewRecordId: number) => {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    formData.append('reviewRecordId', reviewRecordId.toString())
    
    return request({
      url: '/api/screenshots/batch-upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 根据评审记录ID查询截图列表
  getByReviewRecord: (reviewRecordId: number) => {
    return request({
      url: `/api/screenshots/review-record/${reviewRecordId}`,
      method: 'get'
    })
  },

  // 删除截图
  delete: (id: number) => {
    return request({
      url: `/api/screenshots/${id}`,
      method: 'delete'
    })
  },

  // 更新截图排序
  updateOrder: (id: number, sortOrder: number) => {
    return request({
      url: `/api/screenshots/${id}/sort-order`,
      method: 'put',
      params: { sortOrder }
    })
  },

  // 重新排序截图
  reorder: (reviewRecordId: number, screenshotIds: number[]) => {
    return request({
      url: `/api/screenshots/review-record/${reviewRecordId}/reorder`,
      method: 'put',
      params: { screenshotIds: screenshotIds.join(',') }
    })
  },

  // 替换截图文件
  replace: (id: number, newFile: File) => {
    const formData = new FormData()
    formData.append('newFile', newFile)
    
    return request({
      url: `/api/screenshots/${id}/replace`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取下载链接
  getDownloadUrl: (id: number) => {
    return request({
      url: `/api/screenshots/${id}/download-url`,
      method: 'get'
    })
  }
}

// 问题API
export const issueApi = {
  // 创建问题
  create: (data: Issue) => {
    return request({
      url: '/api/issues',
      method: 'post',
      data
    })
  },

  // 更新问题
  update: (id: number, data: Issue) => {
    return request({
      url: `/api/issues/${id}`,
      method: 'put',
      data
    })
  },

  // 删除问题
  delete: (id: number) => {
    return request({
      url: `/api/issues/${id}`,
      method: 'delete'
    })
  },

  // 根据ID查询问题
  getById: (id: number) => {
    return request({
      url: `/api/issues/${id}`,
      method: 'get'
    })
  },

  // 查询问题详情
  getDetails: (id: number) => {
    return request({
      url: `/api/issues/${id}/details`,
      method: 'get'
    })
  },

  // 根据评审记录ID查询问题列表
  getByReviewRecord: (reviewRecordId: number) => {
    return request({
      url: `/api/issues/review-record/${reviewRecordId}`,
      method: 'get'
    })
  },

  // 查询分配给用户的问题
  getAssignedToUser: (userId: number, status?: string) => {
    return request({
      url: `/api/issues/assigned/${userId}`,
      method: 'get',
      params: { status }
    })
  },

  // 查询待整改的问题
  getPendingFix: (userId: number) => {
    return request({
      url: `/api/issues/pending-fix/${userId}`,
      method: 'get'
    })
  },

  // 更新问题状态
  updateStatus: (id: number, status: string) => {
    return request({
      url: `/api/issues/${id}/status`,
      method: 'put',
      params: { status }
    })
  },

  // 关闭问题
  close: (id: number, reason?: string) => {
    return request({
      url: `/api/issues/${id}/close`,
      method: 'post',
      params: { reason }
    })
  },

  // 重新打开问题
  reopen: (id: number, reason?: string) => {
    return request({
      url: `/api/issues/${id}/reopen`,
      method: 'post',
      params: { reason }
    })
  },

  // 获取团队问题统计
  getTeamStatistics: (teamId: number, params?: any) => {
    return request({
      url: `/api/issues/team/${teamId}/statistics`,
      method: 'get',
      params
    })
  },

  // 获取高频问题
  getFrequentIssues: (teamId: number, params?: any) => {
    return request({
      url: `/api/issues/team/${teamId}/frequent`,
      method: 'get',
      params
    })
  }
}

// 整改记录API
export const fixRecordApi = {
  // 提交整改记录
  submit: (data: FixRecord) => {
    return request({
      url: '/api/fix-records',
      method: 'post',
      data
    })
  },

  // 验证整改记录
  verify: (id: number, verifierId: number, result: string, remarks?: string) => {
    return request({
      url: `/api/fix-records/${id}/verify`,
      method: 'post',
      params: { verifierId, result, remarks }
    })
  },

  // 根据问题ID查询整改记录
  getByIssue: (issueId: number) => {
    return request({
      url: `/api/fix-records/issue/${issueId}`,
      method: 'get'
    })
  },

  // 查询待验证的整改记录
  getPendingVerification: (verifierId: number) => {
    return request({
      url: `/api/fix-records/pending-verification/${verifierId}`,
      method: 'get'
    })
  },

  // 撤回整改记录
  withdraw: (id: number, reason?: string) => {
    return request({
      url: `/api/fix-records/${id}/withdraw`,
      method: 'post',
      params: { reason }
    })
  },

  // 重新提交整改记录
  resubmit: (issueId: number, data: FixRecord) => {
    return request({
      url: `/api/fix-records/issue/${issueId}/resubmit`,
      method: 'post',
      data
    })
  }
}