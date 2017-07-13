package com.hortonworks.streamline.streams.actions.beam;

import com.hortonworks.streamline.streams.actions.TopologyActionContext;
import com.hortonworks.streamline.streams.actions.TopologyActions;
import com.hortonworks.streamline.streams.layout.component.TopologyLayout;

import java.nio.file.Path;
import java.util.Map;

public class BeamTopologyActionsImpl implements TopologyActions {


    @Override
    public void init(Map<String, Object> conf) {

    }

    @Override
    public void deploy(TopologyLayout topology, String mavenArtifacts, TopologyActionContext ctx, String asUser) throws Exception {

    }

    @Override
    public void kill(TopologyLayout topology, String asUser) throws Exception {

    }

    @Override
    public Status status(TopologyLayout topology, String asUser) throws Exception {
        return null;
    }

    @Override
    public Path getArtifactsLocation(TopologyLayout topology) {
        return null;
    }

    @Override
    public Path getExtraJarsLocation(TopologyLayout topology) {
        return null;
    }

    @Override
    public String getRuntimeTopologyId(TopologyLayout topology, String asUser) {
        return null;
    }
}
