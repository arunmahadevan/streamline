/**
  * Copyright 2017 Hortonworks.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at

  *   http://www.apache.org/licenses/LICENSE-2.0

  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 **/
package com.hortonworks.streamline.streams.actions.container;

import com.hortonworks.streamline.streams.actions.TopologyActions;
import com.hortonworks.streamline.streams.catalog.Component;
import com.hortonworks.streamline.streams.catalog.Namespace;
import com.hortonworks.streamline.streams.catalog.Service;
import com.hortonworks.streamline.streams.catalog.container.NamespaceAwareContainer;
import com.hortonworks.streamline.streams.actions.container.mapping.MappedTopologyActionsImpl;
import com.hortonworks.streamline.streams.catalog.service.EnvironmentService;
import com.hortonworks.streamline.streams.cluster.discovery.ambari.ComponentPropertyPattern;
import com.hortonworks.streamline.streams.layout.TopologyLayoutConstants;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopologyActionsContainer extends NamespaceAwareContainer<TopologyActions> {

    private static final String COMPONENT_NAME_STORM_UI_SERVER = ComponentPropertyPattern.STORM_UI_SERVER.name();
    private static final String COMPONENT_NAME_NIMBUS = ComponentPropertyPattern.NIMBUS.name();
    private static final String NIMBUS_SEEDS = "nimbus.seeds";
    private static final String NIMBUS_PORT = "nimbus.port";
    public static final String STREAMLINE_STORM_JAR = "streamlineStormJar";
    public static final String STORM_HOME_DIR = "stormHomeDir";

    public static final String RESERVED_PATH_STREAMLINE_HOME = "${STREAMLINE_HOME}";
    public static final String SYSTEM_PROPERTY_STREAMLINE_HOME = "streamline.home";
    private static final String DEFAULT_STORM_JAR_LOCATION_DIR = "${STREAMLINE_HOME}/libs";
    private static final String DEFAULT_STORM_JAR_FILE_PREFIX = "streamline-runtime-storm-";

    private final Map<String, String> streamlineConf;

    public TopologyActionsContainer(EnvironmentService environmentService, Map<String, String> streamlineConf) {
        super(environmentService);
        this.streamlineConf = streamlineConf;
    }

    @Override
    protected TopologyActions initializeInstance(Namespace namespace) {
        String streamingEngine = namespace.getStreamingEngine();

        MappedTopologyActionsImpl actionsImpl;
        // Only Storm is supported as streaming engine
        try {
            actionsImpl = MappedTopologyActionsImpl.valueOf(streamingEngine);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unsupported streaming engine: " + streamingEngine, e);
        }

        // FIXME: "how to initialize" is up to implementation detail - now we just only consider about Storm implementation
        Map<String, String> conf = buildStormTopologyActionsConfigMap(namespace, streamingEngine);

        String className = actionsImpl.getClassName();
        return initTopologyActions(conf, className);
    }

    private TopologyActions initTopologyActions(Map<String, String> conf, String className) {
        try {
            TopologyActions topologyActions = instantiate(className);
            topologyActions.init(conf);
            return topologyActions;
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            throw new RuntimeException("Can't initialize Topology actions instance - Class Name: " + className, e);
        }
    }

    private Map<String, String> buildStormTopologyActionsConfigMap(Namespace namespace, String streamingEngine) {
        // Assuming that a namespace has one mapping of streaming engine
        Service streamingEngineService = getFirstOccurenceServiceForNamespace(namespace, streamingEngine);
        if (streamingEngineService == null) {
            throw new RuntimeException("Streaming Engine " + streamingEngine + " is not associated to the namespace " +
                    namespace.getName() + "(" + namespace.getId() + ")");
        }

        Component uiServer = getComponent(streamingEngineService, COMPONENT_NAME_STORM_UI_SERVER);
        String uiHost = uiServer.getHosts().get(0);
        Integer uiPort = uiServer.getPort();

        assertHostAndPort(uiServer.getName(), uiHost, uiPort);

        Component nimbus = getComponent(streamingEngineService, COMPONENT_NAME_NIMBUS);
        List<String> nimbusHosts = nimbus.getHosts();
        Integer nimbusPort = nimbus.getPort();

        assertHostsAndPort(nimbus.getName(), nimbusHosts, nimbusPort);

        Map<String, String> conf = new HashMap<>();

        // We need to have some local configurations anyway because topology submission can't be done with REST API.
        String stormJarLocation = streamlineConf.get(STREAMLINE_STORM_JAR);
        if (stormJarLocation == null) {
            String jarFindDir = applyReservedPaths(DEFAULT_STORM_JAR_LOCATION_DIR);
            stormJarLocation = findFirstMatchingJarLocation(jarFindDir);
        } else {
            stormJarLocation = applyReservedPaths(stormJarLocation);
        }

        conf.put(STREAMLINE_STORM_JAR, stormJarLocation);
        conf.put(STORM_HOME_DIR, streamlineConf.get(STORM_HOME_DIR));

        // Since we're loading the class dynamically so we can't rely on any enums or constants from there
        conf.put(NIMBUS_SEEDS, String.join(",", nimbusHosts));
        conf.put(NIMBUS_PORT, String.valueOf(nimbusPort));
        conf.put(TopologyLayoutConstants.STORM_API_ROOT_URL_KEY, buildStormRestApiRootUrl(uiHost, uiPort));
        conf.putAll(streamlineConf);

        // Topology during run-time will require few critical configs such as schemaRegistryUrl and catalogRootUrl
        // Hence its important to pass StreamlineConfig to TopologyConfig
        conf.putAll(streamlineConf);

        return conf;
    }

    private String findFirstMatchingJarLocation(String jarFindDir) {
        String[] jars = new File(jarFindDir).list((dir, name) -> {
            if (name.startsWith(DEFAULT_STORM_JAR_FILE_PREFIX) && name.endsWith(".jar")) {
                return true;
            }
            return false;
        });

        if (jars == null || jars.length == 0) {
            return null;
        } else {
            return jarFindDir + File.separator + jars[0];
        }
    }

    private String buildStormRestApiRootUrl(String host, Integer port) {
        return "http://" + host + ":" + port + "/api/v1";
    }

    private String applyReservedPaths(String stormJarLocation) {
        return stormJarLocation.replace(RESERVED_PATH_STREAMLINE_HOME, System.getProperty(SYSTEM_PROPERTY_STREAMLINE_HOME, getCWD()));
    }

    private String getCWD() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }
}