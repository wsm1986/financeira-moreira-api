package com.financeira.api.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Converter
public class TagsConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> value) {
        if (value == null) return null;
        return String.join(",", value);
    }

    @Override
    public List<String> convertToEntityAttribute(String value) {
        if (value == null) return null;
        if (value.isBlank()) return Collections.emptyList();
        return Arrays.asList(value.split(","));
    }
}
