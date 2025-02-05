package display;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import rich.RichHit;
import rich.Track;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DisplaySink extends RichSinkFunction<Tuple2<Track, List<RichHit>>> {

    private static final int WIDTH = 240;
    private static final int HEIGHT = 240;

    private static JFrame frame;
    private static DisplayPanel panel;

    @Override
    public void open(Configuration parameters) {
        if (frame == null) {
            frame = new JFrame("Flink Real-Time Display");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            panel = new DisplayPanel(WIDTH, HEIGHT);
            frame.getContentPane().add(panel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    @Override
    public void invoke(Tuple2<Track, List<RichHit>> value, Context context) {
        Track track = value.f0;
        List<RichHit> hits = value.f1;

        List<Point> points = new ArrayList<>();
        for (RichHit h : hits) {
            points.add(new Point(h.ix, h.iy));
        }

        SwingUtilities.invokeLater(() -> {
            panel.setTrack(track);
            panel.setHits(points);
        });
    }
}
