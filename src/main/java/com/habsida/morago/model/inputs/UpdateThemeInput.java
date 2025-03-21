package com.habsida.morago.model.inputs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.codec.multipart.Part;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateThemeInput {
    private Long id;
    private String name = "";
    private String koreanTitle = "";
    private BigDecimal price;
    private BigDecimal consultantPrice;
    private BigDecimal nightPrice;
    private String description = "";
    private Boolean isPopular = false;
    private Boolean isActive = false;
    private Long categoryId;

    @Setter
    @JsonIgnore
    private Part icon;
}
