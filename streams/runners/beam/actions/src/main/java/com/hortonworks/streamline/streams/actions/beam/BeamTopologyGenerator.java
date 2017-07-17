package com.hortonworks.streamline.streams.actions.beam;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient;
import com.hortonworks.registries.schemaregistry.serdes.avro.kafka.KafkaAvroDeserializer;
import com.hortonworks.streamline.streams.StreamlineEvent;
import com.hortonworks.streamline.streams.common.StreamlineEventImpl;
import com.hortonworks.streamline.streams.layout.component.OutputComponent;
import com.hortonworks.streamline.streams.layout.component.StreamlineSink;
import com.hortonworks.streamline.streams.layout.component.StreamlineSource;
import com.hortonworks.streamline.streams.layout.component.TopologyDag;
import com.hortonworks.streamline.streams.layout.component.TopologyDagVisitor;
import com.hortonworks.streamline.streams.layout.component.TopologyLayout;
import com.hortonworks.streamline.streams.layout.component.impl.KafkaSink;
import com.hortonworks.streamline.streams.layout.component.impl.KafkaSource;
import com.hortonworks.streamline.streams.runtime.serde.StreamlineEventSerializer;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Map;

public class BeamTopologyGenerator extends TopologyDagVisitor {
    private final Table<OutputComponent, String, PCollection<StreamlineEvent>> pcoll = HashBasedTable.create();
    private final Pipeline pipeline;
    private final TopologyLayout topologyLayout;
    private final TopologyDag topologyDag;

    public BeamTopologyGenerator(TopologyLayout layout, Pipeline pipeline) {
        this.pipeline = pipeline;
        this.topologyLayout = layout;
        this.topologyDag = layout.getTopologyDag();
    }

    @Override
    public void visit(StreamlineSource source) {
        if (source instanceof KafkaSource) {
            addKafkaSource((KafkaSource) source);
        }
    }

    @Override
    public void visit(StreamlineSink sink) {
        topologyDag.getEdgesTo(sink).forEach(edge -> {
            edge.getStreamGroupings().forEach(sg -> {
                PCollection<StreamlineEvent> coll = pcoll.get(edge.getFrom(), sg.getStream().getId());
                doVisit(sink, coll);
            });
        });
    }

    private void doVisit(StreamlineSink sink, PCollection<StreamlineEvent> coll) {
        if (sink instanceof KafkaSink) {
            addKafkaSink((KafkaSink) sink, coll);
        }
    }

    private void addKafkaSink(KafkaSink sink, PCollection<StreamlineEvent> coll) {
        coll.apply(KafkaIO.<String, StreamlineEvent>write()
                .withBootstrapServers(sink.getBoostrapServers())
                .withValueSerializer(StreamlineEventSerializer.class)
                .withTopic(sink.getTopic())
                .values());
    }

    private void addKafkaSource(KafkaSource source) {
        PCollection<StreamlineEvent> coll = pipeline.apply(KafkaIO.<String, Object>read()
                .withBootstrapServers(source.getBootstrapServers())
                .withTopic(source.getTopic())
                .withKeyDeserializer(StringDeserializer.class)
                .updateConsumerProperties(ImmutableMap.of(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name(),
                        source.getSchemaRegistryUrl()))
                .withValueDeserializer(KafkaAvroDeserializer.class)
                .withoutMetadata())
                .apply(Values.create())
                .apply("StreamlineEvent", MapElements
                        .into(TypeDescriptor.of(StreamlineEvent.class))
                        .via(input -> StreamlineEventImpl.builder().putAll((Map<String, Object>) input)
                                .build()));
        source.getOutputStreams().forEach(stream -> pcoll.put(source, stream.getId(), coll));
    }
}
