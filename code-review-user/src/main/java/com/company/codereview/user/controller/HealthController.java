package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@Api(tags = "健康检查")
public class HealthController {
    
    /**
     * 健康检查
     */
    @GetMapping
    @ApiOperation("健康检查")
    public ResponseResult<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "code-review-user");
        healthInfo.put("version", "1.0.0");
        
        return ResponseResult.success("服务运行正常", healthInfo);
    }
    
    /**
     * 版本信息
     */
    @GetMapping("/version")
    @ApiOperation("获取版本信息")
    public ResponseResult<Map<String, Object>> version() {
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("service", "code-review-user");
        versionInfo.put("version", "1.0.0");
        versionInfo.put("buildTime", "2024-01-01 00:00:00");
        versionInfo.put("javaVersion", System.getProperty("java.version"));
        
        return ResponseResult.success(versionInfo);
    }
}