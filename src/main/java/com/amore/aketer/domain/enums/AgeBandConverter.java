package com.amore.aketer.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AgeBandConverter implements AttributeConverter<AgeBand, String> {

    @Override
    public String convertToDatabaseColumn(AgeBand attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public AgeBand convertToEntityAttribute(String dbData) {
        return AgeBand.fromCode(dbData);
    }
}
