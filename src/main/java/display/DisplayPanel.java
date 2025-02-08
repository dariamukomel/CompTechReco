package display;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisplayPanel extends JPanel {
    private int width;
    private int height;
    private final int size = 400;
    private List<MarkedPoint> hits = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DisplayPanel(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(size, size));
    }

    public synchronized void addHit(MarkedPoint hit) {
        hits.add(hit);
        repaint();
        // Удаляем точку через 500 мс
        scheduler.schedule(() -> {
            SwingUtilities.invokeLater(() -> {
                synchronized (hits) {
                    hits.remove(hit);
                    repaint();
                }
            });
        }, 500, TimeUnit.MILLISECONDS);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);

        double scale = (double) size/width;
        double pointSize = 4 * scale;
        // хиты
        for (MarkedPoint p : hits) {
            g.setColor(p.isSignal ? Color.RED : Color.GRAY);
            g.fillOval((int)(p.x*scale - pointSize/2), (int)(p.y*scale - pointSize/2), (int)pointSize, (int)pointSize);


        }
    }
}
