package lumi.insert.app.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import lumi.insert.app.core.entity.AuthToken;
import lumi.insert.app.dto.response.AuthTokenResponse; 

/**
 * Mapper for {@link AuthToken}, mapped from DTO to Entity and Otherwise.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Mapper(componentModel = "spring", uses = EmployeeMapper.class)
public interface AuthMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "accessToken", source = "accessToken")
    AuthTokenResponse createDtoResponseFromEntity(String accessToken, AuthToken entity);

}
