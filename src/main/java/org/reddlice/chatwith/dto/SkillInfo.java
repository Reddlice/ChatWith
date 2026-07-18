package org.reddlice.chatwith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillInfo {

    private String name;
    private String description;

}
