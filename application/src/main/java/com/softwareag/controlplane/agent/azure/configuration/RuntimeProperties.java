/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


/**
 * Runtime properties read from application.properties or from environment variables
 *
 */
@ConfigurationProperties(prefix = "apicp.runtime")
@Getter
@Setter
@Validated
public class RuntimeProperties {
    /**
     * Name for the Azure API Gateway  - if not specified, the value will be set from the actual name of the Azure API Gateway
     */
    private String name;
    
    /**
     * Description about the Azure API Gateway name
     */
    private String description;
    
    /**
     * The region for the Azure API Gateway name - if not specified, the value will be set from the actual Azure region for the Azure API Gateway
     */
    private String region;
    
    /**
     * The location for the Azure API Gateway name - if not specified, the value will be set from the actual Azure location for the Azure API Gateway
     */
    private String location;
    
    /**
     * Tags for the Azure API Gateway name
     */
    private Set<String> tags;

    /**
     * The API Management Service transaction capacity is represented as number in here
     */
    private String capacityValue;

    /**
     * unit for transaction is configured such as PER_SECOND , PER_MINUTE , PER_HOUR , PER_DAY , PER_WEEK, PER_MONTH , PER_YEAR
     */
    private String capacityUnit;

    /**
     * Pre-requisite for creating your API Management as Runtime into Control Plane is to create Runtime type in Control Plane.
     */
    @NotBlank
    private String type;

}
