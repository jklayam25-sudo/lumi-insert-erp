package lumi.insert.app.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper; 
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.dto.request.EmployeeUpdateRequest; 
import lumi.insert.app.dto.response.EmployeeResponse; 

/**
 * Mapper for {@link Employee}, mapped from DTO to Entity and Otherwise.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
    EmployeeResponse createDtoResponseFromEmployee(Employee entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE) 
    void updateEmployeeFromDto(EmployeeUpdateRequest dto, @MappingTarget Employee entity);
}
