package display;


import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import rich.RichHit;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DisplaySink extends RichSinkFunction<RichHit> {

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
    public void invoke(RichHit value, Context context) {

        MarkedPoint point = new MarkedPoint(value.ix, value.iy, value.isSignal);

        SwingUtilities.invokeLater(() -> {
            panel.addHit(point);
        });
    }
}
