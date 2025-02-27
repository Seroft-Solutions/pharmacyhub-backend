package com.pharmacyhub.dto;

import com.pharmacyhub.domain.entity.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO {
    private Long id;
    private String optionText;
    private String optionLabel;

    public static OptionDTO fromEntity(Option option) {
        OptionDTO dto = new OptionDTO();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setOptionLabel(option.getOptionLabel());
        return dto;
    }

    public static Option toEntity(OptionDTO dto) {
        Option option = new Option();
        option.setId(dto.getId());
        option.setOptionText(dto.getOptionText());
        option.setOptionLabel(dto.getOptionLabel());
        return option;
    }
}
