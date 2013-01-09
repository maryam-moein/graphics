package nodebox.graphics;


import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.StringReader;

public class Playground extends JFrame {

    private final JTextArea codeArea;
    private final Viewer viewer;

    public Playground() throws HeadlessException {
        super("Playground");

        codeArea = new JTextArea(20, 60);
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.setPreferredSize(new Dimension(400, 400));
        viewer = new Viewer();
        JMenuBar bar = new JMenuBar();

        JMenu codeMenu = new JMenu("Code");
        JMenuItem runItem = new JMenuItem(new RunAction());
        codeMenu.add(runItem);
        bar.add(codeMenu);


        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codeArea, viewer);
        split.setDividerLocation(0.5);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton runButton = new JButton("Run");
        buttonPanel.add(runButton);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doRun();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(split, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        setContentPane(mainPanel);
        setJMenuBar(bar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        Playground frame = new Playground();
        frame.setVisible(true);
        frame.codeArea.requestFocus();
    }

    private void doRun() {
        String source = codeArea.getText();
        Object o = clojure.lang.Compiler.load(new StringReader("(import '[geocore Path Group])\n" + source));
        viewer.setResult(o);
    }

    private static class Viewer extends JComponent {
        private Object result;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(java.awt.Color.WHITE);
            g2.fill(g.getClip());
            g2.setColor(java.awt.Color.BLACK);

            if (result instanceof Shape) {
                paintGeometry(g2, (Shape) result);
            } else {
                paintObject(g2, result);
            }
        }

        private void paintGeometry(Graphics2D g, Shape shape) {
            g.setColor(Color.DARK_GRAY);
            shape.draw(g);

            paintPoints(g, shape.getPoints());
        }

        private void paintPoints(Graphics2D g, Iterable<Point> points) {
            g.setColor(java.awt.Color.BLUE);
            for (Point p : points) {
                g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
            }
        }

        private void paintObject(Graphics2D g, Object object) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            g.drawString(object == null ? "null" : object.toString(), 10, 20);
        }

        public void setResult(Object result) {
            this.result = result;
            repaint();
        }

    }

    private class RunAction extends AbstractAction {
        private RunAction() {
            super("Run");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            doRun();
        }
    }
}