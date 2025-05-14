import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

// ----- Behavior Tree Node Interfaces -----
interface BehaviorNode {
    enum Status { SUCCESS, FAILURE, RUNNING }
    Status tick();
}

abstract class CompositeNode implements BehaviorNode {
    protected List<BehaviorNode> children = new ArrayList<>();
    public void addChild(BehaviorNode child) { children.add(child); }
}

class SequenceNode extends CompositeNode {
    public Status tick() {
        for (BehaviorNode child : children) {
            Status status = child.tick();
            if (status != Status.SUCCESS) return status;
        }
        return Status.SUCCESS;
    }
}

class SelectorNode extends CompositeNode {
    public Status tick() {
        for (BehaviorNode child : children) {
            Status status = child.tick();
            if (status != Status.FAILURE) return status;
        }
        return Status.FAILURE;
    }
}

// ----- Leaf Nodes -----
class IsPlayerInRange implements BehaviorNode {
    private Enemy enemy;
    private Player player;
    private int range;

    public IsPlayerInRange(Enemy e, Player p, int r) {
        enemy = e;
        player = p;
        range = r;
    }

    public Status tick() {
        double dist = enemy.getPosition().distance(player.getPosition());
        return dist <= range ? Status.SUCCESS : Status.FAILURE;
    }
}

class ChasePlayer implements BehaviorNode {
    private Enemy enemy;
    private Player player;

    public ChasePlayer(Enemy e, Player p) {
        enemy = e;
        player = p;
    }

    public Status tick() {
        enemy.moveTowards(player.getX(), player.getY());
        return Status.RUNNING;
    }
}

class WanderRandomly implements BehaviorNode {
    private Enemy enemy;

    public WanderRandomly(Enemy e) {
        enemy = e;
    }

    public Status tick() {
        enemy.wander();
        return Status.RUNNING;
    }
}

// ----- Entity Classes -----
class Entity {
    protected int x, y;
    public int getX() { return x; }
    public int getY() { return y; }
    public Point getPosition() { return new Point(x, y); }
}

class Player extends Entity {
    public Player(int x, int y) { this.x = x; this.y = y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Enemy extends Entity {
    public Enemy(int x, int y) { this.x = x; this.y = y; }

    public void moveTowards(int targetX, int targetY) {
        if (targetX > x) x++;
        else if (targetX < x) x--;
        if (targetY > y) y++;
        else if (targetY < y) y--;
    }

    public void wander() {
        x += (int)(Math.random() * 3) - 1;
        y += (int)(Math.random() * 3) - 1;
    }
}

// ----- Game Panel with Behavior Tree -----
class GamePanel extends JPanel {
    private Player player;
    private Enemy enemy;
    private BehaviorNode behaviorTree;

    public GamePanel() {
        setPreferredSize(new Dimension(400, 400));
        player = new Player(200, 200);
        enemy = new Enemy(50, 50);

        // Build Behavior Tree
        SelectorNode root = new SelectorNode();
        SequenceNode chaseSeq = new SequenceNode();
        chaseSeq.addChild(new IsPlayerInRange(enemy, player, 80));
        chaseSeq.addChild(new ChasePlayer(enemy, player));
        root.addChild(chaseSeq);
        root.addChild(new WanderRandomly(enemy));
        this.behaviorTree = root;

        // Mouse click to move player
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                player.setPosition(e.getX(), e.getY());
                repaint();
            }
        });

        // Game loop timer
        Timer timer = new Timer(50, e -> {
            behaviorTree.tick();
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw player
        g.setColor(Color.BLUE);
        g.fillOval(player.getX() - 5, player.getY() - 5, 10, 10);

        // Draw enemy
        g.setColor(Color.RED);
        g.fillOval(enemy.getX() - 5, enemy.getY() - 5, 10, 10);

        // Draw detection range
        g.setColor(Color.LIGHT_GRAY);
        g.drawOval(enemy.getX() - 80, enemy.getY() - 80, 160, 160);
    }
}

// ----- Main Application Window -----
public class BehaviorTreeGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Behavior Tree 2D Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new GamePanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

