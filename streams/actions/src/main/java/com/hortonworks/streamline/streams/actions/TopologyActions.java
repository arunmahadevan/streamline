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
package com.hortonworks.streamline.streams.actions;

import com.hortonworks.streamline.streams.layout.component.TopologyLayout;
import com.hortonworks.streamline.streams.layout.component.impl.testing.TestRunProcessor;
import com.hortonworks.streamline.streams.layout.component.impl.testing.TestRunSink;
import com.hortonworks.streamline.streams.layout.component.impl.testing.TestRunSource;

import java.nio.file.Path;
import java.util.Map;

/**
 * Interface representing options that need to be supported on a topology
 * layout once its created using the UI.
 */
public interface TopologyActions {
    // Any one time initialization is done here
    void init(Map<String, Object> conf);

    // Deploy the artifact generated using the underlying streaming
    // engine
    void deploy(TopologyLayout topology, String mavenArtifacts, TopologyActionContext ctx, String asUser) throws Exception;

    //Kill the artifact that was deployed using deploy
    void kill(TopologyLayout topology, String asUser) throws Exception;

    // Compose and run parameter topology as test mode using the underlying streaming engine.
    // The parameter 'topology' should contain its own topology DAG.
    // Please refer the javadoc of TestRunSource and also TestRunSink to see which information this method requires.
    default void testRun(TopologyLayout topology, String mavenArtifacts,
                         Map<String, TestRunSource> testRunSourcesForEachSource,
                         Map<String, TestRunProcessor> testRunProcessorsForEachProcessor,
                         Map<String, TestRunSink> testRunSinksForEachSink) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    //Validate the json representing the Streamline based on underlying streaming
    // engine
    default void validate(TopologyLayout topology) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    //Suspend the json representing the Streamline based on underlying streaming
    // engine
    default void suspend(TopologyLayout topology, String asUser) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    //Resume the json representing the Streamline based on underlying streaming
    // engine
    default void resume(TopologyLayout topology, String asUser) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    // return topology status
    Status status(TopologyLayout topology, String asUser) throws Exception;

    /**
     * the Path where topology specific artifacts are kept
     */
    Path getArtifactsLocation(TopologyLayout topology);

    /**
     * the Path where extra jars to be deployed are kept
     */
    Path getExtraJarsLocation(TopologyLayout topology);

    /**
     * the topology id which is running in runtime streaming engine
     */
    String getRuntimeTopologyId(TopologyLayout topology, String asUser);

    interface Status {
        String getStatus();

        Map<String, String> getExtra();
    }
}
