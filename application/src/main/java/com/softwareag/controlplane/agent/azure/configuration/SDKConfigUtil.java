/**
* Copyright Super iPaaS Integration LLC, an IBM Company 2024
*/
package com.softwareag.controlplane.agent.azure.configuration;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.Location;
import com.softwareag.controlplane.agent.azure.common.constants.Constants;
import com.softwareag.controlplane.agent.azure.common.context.AzureManagersHolder;
import com.softwareag.controlplane.agent.azure.common.utils.AzureAgentUtil;
import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.TlsConfig;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public final class SDKConfigUtil {

    private SDKConfigUtil() {

    }

    public static ControlPlaneConfig controlPlaneConfig(AgentProperties agentProperties){
        TlsConfig tlsConfig = new TlsConfig
                .Builder(agentProperties.getTrustStorePath(), agentProperties.getTrustStoreType())
                .truststorePassword(agentProperties.getTrustStorePassword())
                .keystorePath(ObjectUtils.isNotEmpty(agentProperties.getKeyStorePath()) ?
                        agentProperties.getKeyStorePath() : null)
                .keystorePassword(ObjectUtils.isNotEmpty(agentProperties.getKeyStorePassword()) ?
                        agentProperties.getKeyStorePassword() : null)
                .keyAlias(ObjectUtils.isNotEmpty(agentProperties.getKeyAlias()) ? agentProperties.getKeyAlias() : null)
                .keyPassword(ObjectUtils.isNotEmpty(agentProperties.getKeyPassword()) ? agentProperties.getKeyPassword() : null)
                .keystoreType(ObjectUtils.isNotEmpty(agentProperties.getKeyStoreType()) ? agentProperties.getKeyStoreType() : null)
                .build();


        AuthConfig authConfig;
        if(ObjectUtils.isNotEmpty(agentProperties.getToken())) {
            authConfig = new AuthConfig
                    .Builder(agentProperties.getToken())
                    .build();
        } else {
            authConfig = new AuthConfig
                    .Builder(agentProperties.getUsername(), agentProperties.getPassword())
                    .build();
        }

        return new ControlPlaneConfig.Builder()
                .url(agentProperties.getUrl())
                .authConfig(authConfig)
                .tlsConfig(agentProperties.isSslEnabled() && !agentProperties.getTrustStorePath().isEmpty() && !agentProperties.getTrustStorePassword().isEmpty() ? tlsConfig : null)
                .build();
    }

    public static RuntimeConfig runtimeConfig(AzureProperties azureProperties,RuntimeProperties runtimeProperties, AzureManagersHolder managerHolder){

        //name block
        String runtimeName = ObjectUtils.isEmpty(runtimeProperties.getName()) ? azureProperties.getApiManagementServiceName() : runtimeProperties.getName();

        //region block
        Region azureRegion = Region.fromName(managerHolder.getApiService().regionName());
        String runtimeRegion = ObjectUtils.isEmpty(runtimeProperties.getRegion()) && ObjectUtils.isNotEmpty(azureRegion) ?
               azureRegion.toString() : runtimeProperties.getRegion();

        //location block
        Location azureLocation = managerHolder.getAzureResourceManager().subscriptions()
                .getById(azureProperties.getSubscriptionId())
                .getLocationByRegion(azureRegion);
        String runtimeLocation = ObjectUtils.isEmpty(runtimeProperties.getLocation()) && ObjectUtils.isNotEmpty(azureLocation) ?
               azureLocation.physicalLocation() : runtimeProperties.getLocation();

        //tags block
        Set<String> runtimeTags = ObjectUtils.isEmpty(runtimeProperties.getTags()) ? AzureAgentUtil.convertTags(managerHolder.getApiService().tags()) : runtimeProperties.getTags();

        //capacity block
        Capacity capacity = null;
        if(ObjectUtils.isNotEmpty(runtimeProperties.getCapacityValue())) {
            capacity = new Capacity();
            capacity.setUnit(Capacity.TimeUnit.valueOf(runtimeProperties.getCapacityUnit()));
            capacity.setValue(Long.parseLong(runtimeProperties.getCapacityValue()));
        }

        // runtime ID = subscriptionId_serviceName
        String runtimeId =
                azureProperties.getSubscriptionId() + Constants.UNDERSCORE + azureProperties.getApiManagementServiceName();
        
        String runtimeHost = String.format("https://%s.developer.azure-api.net", azureProperties.getApiManagementServiceName());

        return new RuntimeConfig.Builder(runtimeId, runtimeName, runtimeProperties.getType(),
                Runtime.DeploymentType.PUBLIC_CLOUD)
                .description(runtimeProperties.getDescription())
                .region(runtimeRegion)
                .location(runtimeLocation)
                .tags(runtimeTags)
                .capacity(capacity)
                .host(runtimeHost)
                .build();
    }

}
