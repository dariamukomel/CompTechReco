package display;

import rich.Track;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DisplayPanel extends JPanel {
    private int width;
    private int height;
    private java.util.List<Point> hits = new ArrayList<>();
    private Track track;

    public DisplayPanel(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
    }

    public void setHits(List<Point> hits) {
        this.hits = hits;
        repaint();
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // сигнальные хиты
        g.setColor(Color.RED);
        for (Point p : hits) {
            g.fillOval(p.x - 2, p.y - 2, 4, 4);
        }
    }
}
