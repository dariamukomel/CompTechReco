package consumer;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rich.RichHit;
import rich.RichHitDeserializer;
import rich.Track;
import rich.TrackDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import display.DisplaySink;

public class KafkaFlinkConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFlinkConsumer.class);

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flink-consumer");

        FlinkKafkaConsumer<RichHit> hitConsumer = new FlinkKafkaConsumer<>(
                "HitStream",
                new RichHitDeserializer(),
                properties
        );
        DataStream<RichHit> hitDataStream = env
                .addSource(hitConsumer)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<RichHit>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                                .withTimestampAssigner((hit, ts) -> convertToNanoseconds(hit.timeSec, hit.timeNanosec))
                );

        hitDataStream.print();

        FlinkKafkaConsumer<Track> trackConsumer = new FlinkKafkaConsumer<>(
                "TrackStream",
                new TrackDeserializer(),
                properties
        );
        DataStream<Track> trackDataStream = env
                .addSource(trackConsumer)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Track>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                                .withTimestampAssigner((track, ts) -> convertToNanoseconds(track.timeSec, track.timeNanosec))
                );

        DataStream<Tuple2<Track, List<RichHit>>> joinedStream = trackDataStream
                .keyBy(track -> 1)
                .connect(hitDataStream.keyBy(hit -> 1))
                .process(new MyCoProcessFunction());


//        joinedStream
//                .map(value -> {
//                    Track track = value.f0;
//                    List<RichHit> hits = value.f1;
//                    return track.toString() + hits.toString();
//                })
//                .print();

        DataStream<RichHit> signalHitStream = joinedStream
                .flatMap((Tuple2<Track, List<RichHit>> value, Collector<RichHit> out) -> {
                    for (RichHit hit : value.f1) {
                        hit.isSignal = true;
                        out.collect(hit);
                    }
                })
                .returns(RichHit.class);

        DataStream<RichHit> finalHitStream = hitDataStream
                .keyBy(hit -> hit.ix + "_" + hit.iy + "_" + hit.timeSec + "_" + hit.timeNanosec)
                .connect(signalHitStream .keyBy(hit -> hit.ix + "_" + hit.iy + "_" + hit.timeSec + "_" + hit.timeNanosec))
                .process(new DeduplicateRichHitsFunction());

        finalHitStream.addSink(new DisplaySink());





        env.execute("KafkaFlinkConsumer");
    }

    private static long convertToNanoseconds(long timeSec, long timeNanosec) {
        return (timeSec * 1_000_000_000L) + timeNanosec;
    }
}
