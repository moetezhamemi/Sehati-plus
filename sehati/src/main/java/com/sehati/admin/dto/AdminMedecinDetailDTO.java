package com.sehati.admin.dto;

import com.sehati.common.dto.WorkHoursDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminMedecinDetailDTO extends AdminUserDetailDTO {
    private Long entityId;
    private String nom;
    private String prenom;
    private String specialite;
    private String adresseCabinet;
    private String ville;
    private Double latitude;
    private Double longitude;
    private List<String> phones;
    private String biographie;
    private WorkHoursDTO workHours;
    private Integer consultationTime;
    private String diplomeUrl;
    private Double averageRating;
    private Long reviewCount;
}
