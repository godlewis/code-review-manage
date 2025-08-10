package com.company.codereview.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队数据传输对象
 */
@Data
public class TeamDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long leaderId;
    private String leaderName;
    private Integer memberCount;
    private Boolean active;
    private LocalDateTime createdAt;
    private List<UserDTO> members;
}