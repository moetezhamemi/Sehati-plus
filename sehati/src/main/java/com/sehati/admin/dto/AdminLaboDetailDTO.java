package com.sehati.admin.dto;

import com.sehati.common.dto.WorkHoursDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminLaboDetailDTO extends AdminUserDetailDTO {
    private Long entityId;
    private String nomLabo;
    private String adresseComplete;
    private String ville;
    private Double latitude;
    private Double longitude;
    private List<String> phones;
    private String responsable;
    private List<String> analyses;
    private WorkHoursDTO workHours;
    private Integer consultationTime;
    private Integer capaciteParCreneau;
    private String registreCommerceUrl;
    private Double averageRating;
    private Long reviewCount;
}
