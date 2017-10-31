package com.hortonworks.streamline.streams.sampling.service.storm;

import com.hortonworks.streamline.streams.StreamlineEvent;
import com.hortonworks.streamline.streams.catalog.Topology;
import com.hortonworks.streamline.streams.catalog.TopologyComponent;
import com.hortonworks.streamline.streams.common.StreamlineEventImpl;
import com.hortonworks.streamline.streams.layout.TopologyLayoutConstants;
import com.hortonworks.streamline.streams.sampling.service.TopologySampling;
import com.hortonworks.streamline.streams.storm.common.StormRestAPIClient;
import com.hortonworks.streamline.streams.storm.common.StormTopologyUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hortonworks.streamline.streams.common.StreamlineEventImpl.TO_STRING_PREFIX;

public class StormTopologySamplingService implements TopologySampling {
    private static final Logger LOG = LoggerFactory.getLogger(StormTopologySamplingService.class);

    Pattern LOG_CONTENT_REGEX = Pattern.compile("<pre id=\"logContent\">(.*)</pre>", Pattern.DOTALL);

    private StormRestAPIClient client;

    public StormTopologySamplingService() {
    }

    @Override
    public void init(Map<String, Object> conf) {
        String stormApiRootUrl = null;
        Subject subject = null;
        if (conf != null) {
            stormApiRootUrl = (String) conf.get(TopologyLayoutConstants.STORM_API_ROOT_URL_KEY);
            subject = (Subject) conf.get(TopologyLayoutConstants.SUBJECT_OBJECT);
        }
        Client restClient = ClientBuilder.newClient(new ClientConfig());
        this.client = new StormRestAPIClient(restClient, stormApiRootUrl, subject);
    }

    @Override
    public boolean enableSampling(Topology topology, int pct, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        return client.enableSampling(topologyId, pct, asUser);
    }

    @Override
    public boolean enableSampling(Topology topology, TopologyComponent component, int pct, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        return client.enableSampling(topologyId, component.getId() + "-" + component.getName(), pct, asUser);
    }

    @Override
    public boolean disableSampling(Topology topology, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        return client.disableSampling(topologyId, asUser);
    }

    @Override
    public boolean disableSampling(Topology topology, TopologyComponent component, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        return client.disableSampling(topologyId, component.getId() + "-" + component.getName(), asUser);
    }

    @Override
    public SamplingStatus getSamplingStatus(Topology topology, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        return buildSamplingStatus(client.getSamplingStatus(topologyId, asUser));
    }

    @Override
    public SamplingStatus getSamplingStatus(Topology topology, TopologyComponent component, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        return buildSamplingStatus(client.getSamplingStatus(topologyId, asUser));
    }

    @Override
    public List<String> getSampledEvents(Topology topology, TopologyComponent component, String asUser) {
        String topologyId = StormTopologyUtil.findStormTopologyId(client, topology.getId(), asUser);
        String componentId = component.getId() + "-" + component.getName();
        String res = client.getSampledEvents(topologyId, componentId, asUser);
        Matcher match = LOG_CONTENT_REGEX.matcher(res);
        if (match.find()) {
            String lines = match.group(1);
            if (!StringUtils.isEmpty(lines)) {
                return getSampledEvents(lines, componentId);
            }
        }
        return Collections.emptyList();
    }

    private List<String> getSampledEvents(String lines, String queriedComponentId) {
        List<String> events = new ArrayList<>();
        // Timestamp, Component name, Component task-id, MessageId (incase of anchoring), List of emitted values
        for (String line : lines.split("\n")) {
            int componentStart = line.indexOf(',') + 1;
            int componentEnd = line.indexOf(',', componentStart);
            String componentName = line.substring(componentStart, componentEnd);
            if (componentName.equals(queriedComponentId)) {
                String eventStr = StringEscapeUtils.unescapeHtml(line.substring(line.indexOf(TO_STRING_PREFIX) + TO_STRING_PREFIX.length(), line.lastIndexOf("]")));
                events.add(eventStr);
            } else {
                LOG.debug("Skipping sampled event for component {}", componentName);
            }
        }
        return events;
    }

    private SamplingStatus buildSamplingStatus(Map result) {
        return result == null ? null : new SamplingStatus() {
            @Override
            public Boolean getEnabled() {
                Object debug = result.get("debug");
                return debug != null && debug instanceof Boolean ? (Boolean) debug : false;
            }

            @Override
            public Integer getPct() {
                Object samplingPct = result.get("samplingPct");
                return samplingPct != null && samplingPct instanceof Number ? ((Number) samplingPct).intValue() : 0;
            }
        };
    }
}
