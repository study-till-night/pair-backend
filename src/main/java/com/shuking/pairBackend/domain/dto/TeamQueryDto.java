package com.shuking.pairBackend.domain.dto;

import com.shuking.pairBackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "队伍查询DTO")
public class TeamQueryDto extends PageRequest {
    /**
     * 搜索关键词
     */
    private String searchText;
    /**
     * 根据队伍人数搜索
     */
    private Integer maxNum;
    /**
     * 根据队伍状态
     */
    private Integer status;
}
