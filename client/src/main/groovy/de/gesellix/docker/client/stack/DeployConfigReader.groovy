package de.gesellix.docker.client.stack

import de.gesellix.docker.client.stack.types.StackNetwork
import de.gesellix.docker.compose.ComposeFileReader
import de.gesellix.docker.compose.types.ComposeConfig
import de.gesellix.docker.compose.types.Config
import de.gesellix.docker.compose.types.Service
import groovy.util.logging.Slf4j

@Slf4j
class DeployConfigReader {

    ComposeFileReader composeFileReader = new ComposeFileReader()

    def loadCompose(String namespace, InputStream composeFile) {
        ComposeConfig composeConfig = composeFileReader.load(composeFile)
        log.info("composeContent: $composeConfig}")

        List<String> serviceNetworkNames = composeConfig.services.collect { String name, Service service ->
            service.networks.collect { String networkName, serviceNetwork ->
                networkName
            }
        }.flatten().unique()
        if (!serviceNetworkNames) {
            serviceNetworkNames = ["default"]
        }
        log.info("service network names: ${serviceNetworkNames}")

        def externalNetworkNames = []
        def networkCreateConfigs = [:]
        serviceNetworkNames.each { String internalName ->
            def network = composeConfig.networks[internalName]
            if (!network) {
                def createOpts = new StackNetwork()
                createOpts.labels = ["${ManageStackClient.LabelNamespace}=${namespace}" as String]
                createOpts.driver = "overlay"
                networkCreateConfigs[internalName] = createOpts
            } else if (network?.external?.external) {
                externalNetworkNames << network.external.name ?: internalName
            } else {
                def createOpts = new StackNetwork()

                def labels = new HashSet<String>()
                labels.addAll(network.labels ?: [])
                labels["${ManageStackClient.LabelNamespace}=${namespace}" as String]
                createOpts.labels = labels
                createOpts.driver = network.driver ?: "overlay"
                createOpts.driverOpts = network.driverOpts.options
                createOpts.internal = network.internal
                createOpts.attachable = network.attachable
                if (network.ipam.driver || network.ipam.config) {
                    createOpts.ipam = [:]
                }
                if (network.ipam.driver) {
                    createOpts.ipam.driver = network.ipam.driver
                }
                if (network.ipam.config) {
                    createOpts.ipam.config = []
                    network.ipam.config.each { Config config ->
                        createOpts.ipam.config << [subnet: config.subnet]
                    }
                }
                networkCreateConfigs[internalName] = createOpts
            }
        }

        log.info("network configs: ${networkCreateConfigs}")
        log.info("external networks: ${externalNetworkNames}")


        def cfg = new DeployStackConfig()

        return cfg
    }
}
