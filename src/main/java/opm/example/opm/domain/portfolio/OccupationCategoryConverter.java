package opm.example.opm.domain.portfolio;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OccupationCategoryConverter implements AttributeConverter<OccupationCategory, String> {

    @Override
    public String convertToDatabaseColumn(OccupationCategory attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public OccupationCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Enum.valueOf 대신 safe parsing 로직 사용
        // OccupationCategory.from() 메서드가 이미 부분 일치 로직을 포함하고 있으므로 활용
        return OccupationCategory.from(dbData);
    }
}
