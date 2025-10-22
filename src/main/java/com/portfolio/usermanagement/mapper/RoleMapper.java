package com.portfolio.usermanagement.mapper;

import com.portfolio.usermanagement.domain.model.RoleDomain;
import com.portfolio.usermanagement.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Role conversions between Entity and Domain layers.
 *
 * @author Portfolio Project
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    /**
     * Convert Role entity to RoleDomain.
     *
     * @param role the Role entity
     * @return RoleDomain
     */
    RoleDomain toDomain(Role role);

    /**
     * Convert RoleDomain to Role entity.
     *
     * @param domain the RoleDomain
     * @return Role entity
     */
    Role toEntity(RoleDomain domain);
}
