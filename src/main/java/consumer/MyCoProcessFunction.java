package consumer;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import rich.RichHit;
import rich.Track;

import java.util.ArrayList;
import java.util.List;

public class MyCoProcessFunction
        extends CoProcessFunction<Track, RichHit, Tuple2<Track, List<RichHit>>> {

    private static final long INTERVAL_NS = 50000;

    private transient ValueState<Track> trackState;     // храним последний Track
    private transient ListState<RichHit> hitsState;     // храним все Hits

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        trackState = getRuntimeContext().getState(
                new ValueStateDescriptor<>("trackState", TypeInformation.of(Track.class))
        );

        hitsState = getRuntimeContext().getListState(
                new ListStateDescriptor<>("hitsState", TypeInformation.of(RichHit.class))
        );
    }

    // левая часть connect(пришел трек)
    @Override
    public void processElement1(Track track,
                                Context ctx,
                                Collector<Tuple2<Track, List<RichHit>>> out) throws Exception {


        trackState.update(track);

        long trackNs = convertToNanoseconds(track.timeSec, track.timeNanosec);

        long fireTimestamp = trackNs + INTERVAL_NS;
        ctx.timerService().registerEventTimeTimer(fireTimestamp);
    }

    // правая часть(пришел хит)
    @Override
    public void processElement2(RichHit hit,
                                Context ctx,
                                Collector<Tuple2<Track, List<RichHit>>> out) throws Exception {

        hitsState.add(hit);
    }

    @Override
    public void onTimer(long timestamp,
                        OnTimerContext ctx,
                        Collector<Tuple2<Track, List<RichHit>>> out) throws Exception {

        Track currentTrack = trackState.value();
        if (currentTrack == null) {
            return;
        }

        long trackNs = convertToNanoseconds(currentTrack.timeSec, currentTrack.timeNanosec);
        long leftBoundary = trackNs - INTERVAL_NS;
        long rightBoundary = trackNs + INTERVAL_NS;

        List<RichHit> collectedHits = new ArrayList<>();
        for (RichHit hit : hitsState.get()) {
            long hitNs = convertToNanoseconds(hit.timeSec, hit.timeNanosec);
            if (hitNs >= leftBoundary && hitNs <= rightBoundary) {
                collectedHits.add(hit);
            }
        }

        out.collect(Tuple2.of(currentTrack, collectedHits));

        trackState.clear();
        hitsState.clear();
    }

    private static long convertToNanoseconds(long timeSec, long timeNanosec) {
        return (timeSec * 1_000_000_000L) + timeNanosec;
    }
}


