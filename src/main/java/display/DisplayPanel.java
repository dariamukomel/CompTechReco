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
    private List<MarkedPoint> hits = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DisplayPanel(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
    }

    public synchronized void addHit(MarkedPoint hit) {
        hits.add(hit);
        repaint();
        // Удаляем точку через 200 мс
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
        g.fillRect(0, 0, width, height);

        // хиты
        for (MarkedPoint p : hits) {
            g.setColor(p.isSignal ? Color.RED : Color.GRAY);
            g.fillOval(p.x - 2, p.y - 2, 4, 4);


        }
    }
}
