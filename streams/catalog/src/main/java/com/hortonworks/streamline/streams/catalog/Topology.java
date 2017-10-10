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
package com.hortonworks.streamline.streams.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hortonworks.registries.common.Schema;
import com.hortonworks.registries.storage.annotation.SearchableField;
import com.hortonworks.registries.storage.annotation.StorableEntity;
import com.hortonworks.registries.storage.PrimaryKey;
import com.hortonworks.registries.storage.Storable;
import com.hortonworks.registries.storage.StorableKey;
import com.hortonworks.streamline.streams.layout.component.TopologyDag;

import java.util.HashMap;
import java.util.Map;

/**
 * An Streamline topology that will be persisted in
 * storage layer. Generated by UI
 */
@StorableEntity
public class Topology implements Storable {

    public static final String NAMESPACE = "topology";
    public static final String ID = "id";
    public static final String VERSIONID = "versionId";
    public static final String NAME = "name";
    public static final String NAMESPACE_ID = "namespaceId";
    public static final String CONFIG = "config";
    public static final String TIMESTAMP = "timestamp";
    public static final String DESCRIPTION = "description";

    /**
     * Unique id identifying a topology. This is the composite primary key column.
     */
    private Long id;

    /**
     * Unique version id identifying a topology. This is the composite primary key column.
     */
    private Long versionId;

    /**
     * Human readable topology name; input from user from UI.
     */
    @SearchableField
    private String name;

    /**
     * Topology description
     */
    @SearchableField
    private String description;

    /**
     * Corresponding namespace id.
     */
    private Long namespaceId;

    /**
     * Json string representing the topology configuration; generated by UI.
     */
    @SearchableField
    private String config;

    /**
     * Time at which this topology was created/updated.
     */
    private Long versionTimestamp;

    /**
     * The topology DAG. This is internally generated and used for
     * deployment.
     */
    private TopologyDag topologyDag;

    public Topology() {
    }

    public Topology(Topology other) {
        if (other != null) {
            setId(other.getId());
            setVersionId(other.getVersionId());
            setName(other.getName());
            setDescription(other.getDescription());
            setConfig(other.getConfig());
            setNamespaceId(other.getNamespaceId());
            setVersionTimestamp(other.getVersionTimestamp());
        }
        // this will be re-generated during deployment.
        topologyDag = null;
    }

    @JsonIgnore
    public TopologyDag getTopologyDag() {
        return topologyDag;
    }

    @JsonIgnore
    public void setTopologyDag(TopologyDag topologyDag) {
        this.topologyDag = topologyDag;
    }

    @JsonIgnore
    public String getNameSpace () {
        return NAMESPACE;
    }

    @JsonIgnore
    public Schema getSchema () {
        return Schema.of(
                new Schema.Field(ID, Schema.Type.LONG),
                new Schema.Field(VERSIONID, Schema.Type.LONG),
                new Schema.Field(NAME, Schema.Type.STRING),
                new Schema.Field(DESCRIPTION, Schema.Type.STRING),
                new Schema.Field(NAMESPACE_ID, Schema.Type.LONG),
                new Schema.Field(CONFIG, Schema.Type.STRING)
        );
    }

    @JsonIgnore
    public PrimaryKey getPrimaryKey () {
        Map<Schema.Field, Object> fieldToObjectMap = new HashMap<>();
        fieldToObjectMap.put(new Schema.Field(ID, Schema.Type.LONG), this.id);
        fieldToObjectMap.put(new Schema.Field(VERSIONID, Schema.Type.LONG), this.versionId);
        return new PrimaryKey(fieldToObjectMap);
    }

    @JsonIgnore
    public StorableKey getStorableKey () {
        return new StorableKey(getNameSpace(), getPrimaryKey());
    }

    public Map toMap () {
        Map<String, Object> map = new HashMap<>();
        map.put(ID, this.id);
        map.put(VERSIONID, this.versionId);
        map.put(NAME, this.name);
        map.put(DESCRIPTION, this.description);
        map.put(NAMESPACE_ID, this.namespaceId);
        map.put(CONFIG, this.config);
        return map;
    }

    public Topology fromMap (Map<String, Object> map) {
        this.id = (Long) map.get(ID);
        this.versionId = (Long) map.get(VERSIONID);
        this.name = (String) map.get(NAME);
        this.description = (String) map.get(DESCRIPTION);
        this.namespaceId = (Long) map.get(NAMESPACE_ID);
        this.config = (String)  map.get(CONFIG);
        return this;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(Long namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("timestamp")
    public Long getVersionTimestamp() {
        return versionTimestamp;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("timestamp")
    public void setVersionTimestamp(Long timestamp) {
        this.versionTimestamp = timestamp;
    }

    @JsonIgnore
    public Long getTimestamp() {
        return getVersionTimestamp();
    }

    @Override
    public String toString() {
        return "Topology{" +
                "id=" + id +
                ", versionId=" + versionId +
                ", name='" + name + '\'' +
                ", namespaceId=" + namespaceId +
                ", config='" + config + '\'' +
                ", topologyDag=" + topologyDag +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Topology topology = (Topology) o;

        if (id != null ? !id.equals(topology.id) : topology.id != null) return false;
        return versionId != null ? versionId.equals(topology.versionId) : topology.versionId == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        return result;
    }
}
