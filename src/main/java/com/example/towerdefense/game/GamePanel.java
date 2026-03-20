package com.example.towerdefense.game;

import com.example.towerdefense.model.Tower;
import com.example.towerdefense.service.TowerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Panel principal — torre a la derecha, fantasmas vienen de la izquierda.
 * El jugador hace click para disparar cohetes.
 */
public class GamePanel extends JPanel implements ActionListener {

    // ── Dimensiones ──────────────────────────────────────────────────────────
    static final int W = 860, H = 600;

    // ── Posición de la torre (derecha, centro vertical) ───────────────────────
    static final int TOWER_X = W - 80;
    static final int TOWER_Y = H / 2;

    // ── Paleta pastel ─────────────────────────────────────────────────────────
    static final Color BG_TOP      = new Color(235, 218, 255);
    static final Color BG_BOT      = new Color(255, 225, 240);
    static final Color BG_GRID     = new Color(220, 205, 240);
    static final Color HUD_BG      = new Color(255, 240, 250, 220);
    static final Color HUD_BORDER  = new Color(255, 182, 215);
    static final Color C_PINK      = new Color(255, 105, 155);
    static final Color C_PURPLE    = new Color(180, 100, 220);
    static final Color C_MINT      = new Color(100, 210, 180);
    static final Color C_YELLOW    = new Color(255, 210, 80);
    static final Color C_BLUE      = new Color(120, 180, 255);
    static final Color C_PEACH     = new Color(255, 160, 120);
    static final Color C_TEXT      = new Color(120, 60, 100);
    static final Color C_MUTED     = new Color(190, 150, 180);
    static final Color TOWER_BODY  = new Color(220, 170, 230);
    static final Color TOWER_DARK  = new Color(190, 130, 210);
    static final Color TOWER_LIGHT = new Color(245, 210, 255);

    static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
    static final Font FONT_HUD   = new Font("SansSerif", Font.BOLD, 13);
    static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);

    // ── Estado ───────────────────────────────────────────────────────────────
    private final TowerService    towerService;
    private final List<Enemy>     enemies    = new ArrayList<>();
    private final List<Bullet>    bullets    = new ArrayList<>();
    private final List<Particle>  particles  = new ArrayList<>();
    private final List<FloatText> floatTexts = new ArrayList<>();

    private int     wave       = 1;
    private int     kills      = 0;
    private boolean waveActive = false;
    private int     spawnCount = 0;
    private int     spawnMax   = 0;
    private long    spawnTimer = 0;
    private long    lastTick   = System.currentTimeMillis();
    private String  notifMsg   = "";
    private long    notifEnd   = 0;

    // ── Vida de la torre ──────────────────────────────────────────────────────
    static final int MAX_TOWER_HP = 200;
    int     towerHp  = MAX_TOWER_HP;
    boolean gameOver = false;

    // ── Límite de mejoras ─────────────────────────────────────────────────────
    private final java.util.Map<String, Integer> upgradeCount = new java.util.HashMap<>();
    private static final int MAX_PER_TYPE = 2;

    // Estrellas decorativas
    private final float[][] stars;
    private final Timer  gameTimer;
    private final Random rng = new Random();
    private JButton btnWave;

    // ── Líneas del "camino" (decorativas, horizontales) ───────────────────────
    private static final int[] LANE_Y = { H/4, H/2, 3*H/4 };

    public GamePanel(TowerService towerService) {
        this.towerService = towerService;
        setPreferredSize(new Dimension(W + 220, H));
        setBackground(BG_TOP);
        setLayout(null);

        stars = new float[20][3];
        for (int i = 0; i < stars.length; i++) {
            stars[i][0] = rng.nextFloat() * W;
            stars[i][1] = rng.nextFloat() * H;
            stars[i][2] = 3 + rng.nextFloat() * 4;
        }

        JPanel sidebar = buildSidebar();
        sidebar.setBounds(W, 0, 220, H);
        add(sidebar);

        // Click para disparar
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver || e.getX() >= W) return;
                fireAtClick(e.getX(), e.getY());
            }
        });

        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(255, 240, 252));
        p.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, HUD_BORDER));

        JLabel title = new JLabel("✨ Mejoras ✨");
        title.setForeground(C_PINK);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        p.add(title);

        p.add(upgradeBtn("⚡ Ataque Rápido",  "×1.5 velocidad",    C_BLUE,   e -> applyUpgrade("speed")));
        p.add(upgradeBtn("❄  Congelar",       "+5 daño · lento",   C_MINT,   e -> applyUpgrade("freeze")));
        p.add(upgradeBtn("🛡 Escudo",          "+8 daño · defensa", C_PURPLE, e -> applyUpgrade("shield")));
        p.add(upgradeBtn("💰 Generador Oro",   "+3 daño · oro",     C_YELLOW, e -> applyUpgrade("gold")));
        p.add(Box.createVerticalStrut(10));
        btnWave = waveButton();
        p.add(btnWave);

        // Instrucción de disparo
        JLabel hint = new JLabel("<html><center>🖱 Click en el campo<br>para disparar</center></html>");
        hint.setForeground(C_MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        p.add(hint);

        p.add(Box.createVerticalGlue());

        // Separador visual antes del reiniciar
        JSeparator sep = new JSeparator();
        sep.setForeground(HUD_BORDER);
        sep.setMaximumSize(new Dimension(180, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(sep);
        p.add(Box.createVerticalStrut(8));
        p.add(upgradeBtn("🔄 Reiniciar",  "volver al inicio", C_PEACH, e -> resetGame()));
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private JButton upgradeBtn(String name, String desc, Color accent, ActionListener al) {
        JButton b = new JButton("<html><b>" + name + "</b><br><font size='2'>" + desc + "</font></html>");
        b.setForeground(accent.darker());
        b.setBackground(new Color(255, 248, 255));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120), 2, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(200, 52));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            Color bg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30);
            public void mouseEntered(MouseEvent e) { b.setBackground(bg); }
            public void mouseExited(MouseEvent e)  { b.setBackground(new Color(255, 248, 255)); }
        });
        return b;
    }

    private JButton waveButton() {
        JButton b = new JButton("🌸 ¡Enviar Oleada!");
        b.setForeground(Color.WHITE);
        b.setBackground(C_PINK);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(200, 44));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(e -> startWave());
        return b;
    }

    // ── Game loop ─────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) { repaint(); return; }
        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastTick) / 1000f, 0.05f);
        lastTick = now;

        spawnEnemies(now);
        updateEnemies(dt);
        updateBullets(dt);
        updateParticles(dt);
        updateFloatTexts(dt);
        checkWaveEnd();
        repaint();
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2);
        drawLanes(g2);
        drawTower(g2);
        for (Enemy e    : enemies)    e.draw(g2);
        for (Bullet b   : bullets)    b.draw(g2);
        for (Particle p : particles)  p.draw(g2);
        for (FloatText f: floatTexts) f.draw(g2);
        drawHUD(g2);
        drawNotif(g2);
        if (gameOver) drawGameOver(g2);
    }

    // ── Draw: fondo ───────────────────────────────────────────────────────────
    private void drawBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, H, BG_BOT));
        g2.fillRect(0, 0, W, H);
        g2.setPaint(null);
        g2.setColor(BG_GRID);
        g2.setStroke(new BasicStroke(0.6f));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);

        long t = System.currentTimeMillis();
        for (float[] s : stars) {
            float pulse = (float)(0.7 + 0.3 * Math.sin(t / 800.0 + s[0]));
            drawStar(g2, (int)s[0], (int)s[1], (int)(s[2] * pulse), new Color(255, 180, 220, 80));
        }
    }

    // ── Draw: carriles ────────────────────────────────────────────────────────
    private void drawLanes(Graphics2D g2) {
        for (int ly : LANE_Y) {
            // Sombra del carril
            g2.setColor(new Color(200, 160, 220, 50));
            g2.setStroke(new BasicStroke(34, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.drawLine(0, ly, W, ly);
            // Carril rosa
            g2.setColor(new Color(255, 200, 220, 120));
            g2.setStroke(new BasicStroke(28, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.drawLine(0, ly, W, ly);
            // Línea central punteada
            g2.setColor(new Color(255, 160, 190, 100));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1, new float[]{8, 10}, 0));
            g2.drawLine(0, ly, W, ly);
        }
        g2.setStroke(new BasicStroke(1));

        // Flecha indicando dirección (izquierda → derecha)
        g2.setColor(new Color(255, 130, 170, 100));
        for (int ly : LANE_Y) {
            for (int ax = 60; ax < W - 100; ax += 120) {
                AffineTransform old = g2.getTransform();
                g2.translate(ax, ly);
                g2.fillPolygon(new int[]{0, -10, -10}, new int[]{0, -6, 6}, 3);
                g2.setTransform(old);
            }
        }
    }

    private void drawStar(Graphics2D g2, int cx, int cy, int r, Color c) {
        if (r < 1) return;
        g2.setColor(c);
        int[] px = new int[8], py = new int[8];
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int rad = (i % 2 == 0) ? r : r / 2;
            px[i] = cx + (int)(Math.cos(a) * rad);
            py[i] = cy + (int)(Math.sin(a) * rad);
        }
        g2.fillPolygon(px, py, 8);
    }

    // ── Draw: torre (fija, apunta a la izquierda) ─────────────────────────────
    private void drawTower(Graphics2D g2) {
        Tower t = towerService.getCurrentTower();
        List<String> fx = t.getEffects();
        int x = TOWER_X, y = TOWER_Y;

        // Aura
        if (!fx.isEmpty()) {
            Color aura = getEffectColor(fx.get(fx.size() - 1));
            RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(x, y), 85,
                new float[]{0f, 1f},
                new Color[]{new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 70),
                            new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 0)});
            g2.setPaint(rg);
            g2.fillOval(x - 85, y - 85, 170, 170);
            g2.setPaint(null);
        }

        // Sombra
        g2.setColor(new Color(180, 130, 200, 50));
        g2.fillOval(x - 30, y + 52, 60, 14);

        // Base
        g2.setColor(TOWER_DARK);
        g2.fillRoundRect(x - 26, y + 30, 52, 22, 10, 10);
        g2.setColor(TOWER_LIGHT);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x - 26, y + 30, 52, 22, 10, 10);

        // Cuerpo
        g2.setColor(TOWER_BODY);
        g2.fillRoundRect(x - 20, y - 20, 40, 52, 12, 12);
        g2.setColor(TOWER_LIGHT);
        g2.drawRoundRect(x - 20, y - 20, 40, 52, 12, 12);

        // Ventana
        g2.setColor(new Color(200, 230, 255));
        g2.fillOval(x - 10, y - 8, 20, 20);
        g2.setColor(new Color(150, 190, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - 10, y - 8, 20, 20);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.fillOval(x - 6, y - 5, 7, 7);

        // Almenas
        for (int mx : new int[]{x-16, x-5, x+5, x+16}) {
            g2.setColor(TOWER_DARK);
            g2.fillRoundRect(mx - 5, y - 36, 10, 18, 5, 5);
            g2.setColor(TOWER_LIGHT);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(mx - 5, y - 36, 10, 18, 5, 5);
        }
        g2.setColor(TOWER_BODY);
        g2.fillRoundRect(x - 20, y - 22, 40, 6, 3, 3);

        // Cañón fijo apuntando a la izquierda (ángulo = PI)
        AffineTransform old = g2.getTransform();
        g2.translate(x, y + 4);
        g2.rotate(Math.PI - Math.PI / 2); // apunta izquierda
        g2.setColor(new Color(255, 140, 180));
        g2.fillRoundRect(-4, -22, 8, 22, 4, 4);
        g2.setColor(new Color(255, 80, 130));
        g2.fillPolygon(new int[]{-4,4,0}, new int[]{-22,-22,-32}, 3);
        g2.setColor(new Color(255, 180, 210));
        g2.fillPolygon(new int[]{-4,-8,-4}, new int[]{0,-6,0}, 3);
        g2.fillPolygon(new int[]{ 4, 8, 4}, new int[]{0,-6,0}, 3);
        g2.setColor(new Color(200, 230, 255));
        g2.fillOval(-3, -18, 6, 6);
        g2.setTransform(old);

        // Escudo pulsante
        if (fx.contains("Shield")) {
            float alpha = (float)(0.3 + 0.15 * Math.sin(System.currentTimeMillis() / 400.0));
            g2.setColor(new Color(180, 100, 220, (int)(alpha * 255)));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(x - 52, y - 52, 104, 104);
            g2.setColor(new Color(220, 180, 255, 22));
            g2.fillOval(x - 52, y - 52, 104, 104);
        }

        // Estrellitas orbitando (Gold)
        if (fx.contains("Gold Generator")) {
            long now = System.currentTimeMillis();
            for (int i = 0; i < 4; i++) {
                double a = (now / 700.0) + i * (Math.PI / 2);
                drawStar(g2, (int)(x + Math.cos(a)*48), (int)(y + Math.sin(a)*48), 6, C_YELLOW);
            }
        }

        // Copos de nieve (Freeze)
        if (fx.contains("Freeze")) {
            long now = System.currentTimeMillis();
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            for (int i = 0; i < 3; i++) {
                double a = (now / 900.0) + i * (Math.PI * 2 / 3);
                g2.setColor(new Color(180, 220, 255, 200));
                g2.drawString("❄", (int)(x + Math.cos(a)*44)-6, (int)(y + Math.sin(a)*44)+5);
            }
        }

        // Corazón encima
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.setColor(C_PINK);
        g2.drawString("♥", x - 6, y - 42);

        // Barra de vida sobre la torre
        int bw = 76, bh = 7;
        int bx = x - bw/2, by = y - 56;
        g2.setColor(new Color(220, 200, 220));
        g2.fillRoundRect(bx, by, bw, bh, 4, 4);
        float pct = (float) towerHp / MAX_TOWER_HP;
        g2.setColor(pct > 0.5f ? new Color(150,230,180) : pct > 0.25f ? new Color(255,220,100) : new Color(255,130,150));
        g2.fillRoundRect(bx, by, (int)(bw * pct), bh, 4, 4);
        g2.setColor(HUD_BORDER);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(bx, by, bw, bh, 4, 4);
    }

    private Color getEffectColor(String effect) {
        return switch (effect) {
            case "Fast Attack"    -> C_BLUE;
            case "Freeze"         -> C_MINT;
            case "Shield"         -> C_PURPLE;
            case "Gold Generator" -> C_YELLOW;
            default               -> C_PINK;
        };
    }

    // ── Draw: HUD ─────────────────────────────────────────────────────────────
    private void drawHUD(Graphics2D g2) {
        g2.setColor(HUD_BG);
        g2.fillRect(0, 0, W, 40);
        g2.setColor(HUD_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(0, 40, W, 40);

        g2.setFont(FONT_TITLE);
        g2.setColor(C_PINK);
        g2.drawString("✨ Torre Defensiva ✨", 12, 26);

        Tower t = towerService.getCurrentTower();
        int sx = 230;
        hudStat(g2, sx,       "⚔ Daño",    String.valueOf(t.getDamage()),              C_PEACH);
        hudStat(g2, sx + 110, "⚡ Vel.",    String.format("%.2f", t.getAttackSpeed()), C_BLUE);
        hudStat(g2, sx + 220, "💀 Bajas",  String.valueOf(kills),                     C_PURPLE);
        hudStat(g2, sx + 330, "🌸 Oleada", String.valueOf(wave),                      C_PINK);

        // Barra HP
        int hpX = sx + 440, hpY = 8, hpW = 130, hpH = 20;
        g2.setFont(FONT_SMALL);
        g2.setColor(C_MUTED);
        g2.drawString("🏰 HP:", hpX, 15);
        g2.setColor(new Color(220, 200, 220));
        g2.fillRoundRect(hpX, hpY + 10, hpW, hpH, 6, 6);
        float hpPct = (float) towerHp / MAX_TOWER_HP;
        g2.setColor(hpPct > 0.5f ? new Color(150,230,180) : hpPct > 0.25f ? new Color(255,220,100) : new Color(255,130,150));
        g2.fillRoundRect(hpX, hpY + 10, (int)(hpW * hpPct), hpH, 6, 6);
        g2.setColor(HUD_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(hpX, hpY + 10, hpW, hpH, 6, 6);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(C_TEXT);
        g2.drawString(towerHp + "/" + MAX_TOWER_HP, hpX + hpW/2 - 14, hpY + 23);

        // Chips de efectos
        List<String> fx = t.getEffects();
        if (!fx.isEmpty()) {
            int ex = hpX + hpW + 10;
            g2.setFont(FONT_SMALL);
            g2.setColor(C_MUTED);
            g2.drawString("Mods:", ex, 25);
            ex += 40;
            for (String ef : fx) {
                Color c = getEffectColor(ef);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
                g2.fillRoundRect(ex - 2, 8, 64, 22, 10, 10);
                g2.setColor(c.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(ex - 2, 8, 64, 22, 10, 10);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.drawString(ef, ex + 2, 23);
                ex += 70;
            }
        }
    }

    private void hudStat(Graphics2D g2, int x, String label, String value, Color c) {
        g2.setFont(FONT_SMALL); g2.setColor(C_MUTED); g2.drawString(label, x, 15);
        g2.setFont(FONT_HUD);   g2.setColor(c);        g2.drawString(value, x, 32);
    }

    private void drawNotif(Graphics2D g2) {
        if (notifMsg.isEmpty() || System.currentTimeMillis() > notifEnd) return;
        g2.setFont(FONT_HUD);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(notifMsg);
        int nx = (W - tw) / 2 - 16, ny = 50;
        g2.setColor(new Color(255, 240, 252, 230));
        g2.fillRoundRect(nx, ny, tw + 32, 30, 15, 15);
        g2.setColor(HUD_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(nx, ny, tw + 32, 30, 15, 15);
        g2.setColor(C_PINK);
        g2.drawString(notifMsg, nx + 16, ny + 20);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, W, H);
        int bw = 380, bh = 200, bx = (W-bw)/2, by = (H-bh)/2;
        g2.setColor(new Color(255, 235, 245, 240));
        g2.fillRoundRect(bx, by, bw, bh, 24, 24);
        g2.setColor(C_PINK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(bx, by, bw, bh, 24, 24);

        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2.setColor(new Color(220, 60, 100));
        String title = "💀 ¡GAME OVER! 💀";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, bx + (bw - fm.stringWidth(title))/2, by + 58);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(C_TEXT);
        String s1 = "Oleadas superadas: " + (wave - 1);
        String s2 = "Fantasmas eliminados: " + kills;
        g2.drawString(s1, bx + (bw - g2.getFontMetrics().stringWidth(s1))/2, by + 98);
        g2.drawString(s2, bx + (bw - g2.getFontMetrics().stringWidth(s2))/2, by + 122);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(C_PINK);
        String hint = "Pulsa  🔄 Reiniciar  para jugar de nuevo";
        g2.drawString(hint, bx + (bw - g2.getFontMetrics().stringWidth(hint))/2, by + 165);
    }

    // ── Lógica ────────────────────────────────────────────────────────────────

    /** Dispara al fantasma más cercano al punto donde hizo click el jugador */
    private void fireAtClick(int clickX, int clickY) {
        Tower t = towerService.getCurrentTower();
        Enemy target = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e.dead) continue;
            double d = Math.hypot(e.x - clickX, e.y - clickY);
            if (d < minDist) { minDist = d; target = e; }
        }
        if (target == null) return;
        bullets.add(new Bullet(TOWER_X, TOWER_Y, target, t.getDamage(), t.getEffects()));
    }

    private void startWave() {
        if (waveActive) return;
        waveActive  = true;
        spawnMax    = 6 + wave * 3;
        spawnCount  = 0;
        spawnTimer  = System.currentTimeMillis();
        showNotif("Oleada " + wave + " — ¡" + spawnMax + " fantasmas!");
        if (btnWave != null) btnWave.setEnabled(false);
    }

    private void spawnEnemies(long now) {
        if (!waveActive || spawnCount >= spawnMax) return;
        if (now - spawnTimer < 550) return;
        spawnTimer = now;

        // Elegir tipo: 60% normal, 25% rápido, 15% jefe (más jefes en oleadas altas)
        int roll = rng.nextInt(100);
        Enemy.Type type;
        if (roll < 15 + wave * 2 && wave >= 2) type = Enemy.Type.BOSS;
        else if (roll < 40)                     type = Enemy.Type.FAST;
        else                                    type = Enemy.Type.NORMAL;

        // Asignar a un carril aleatorio
        float laneY = LANE_Y[rng.nextInt(LANE_Y.length)];
        // Pequeño offset para que no vayan todos en fila exacta
        laneY += (rng.nextFloat() - 0.5f) * 20;

        enemies.add(new Enemy(wave, rng, laneY, type));
        spawnCount++;
    }

    private void updateEnemies(float dt) {
        for (Enemy e : enemies) {
            e.update(dt);
            if (!e.dead && e.reachedTower(TOWER_X)) {
                e.dead  = true;
                towerHp = Math.max(0, towerHp - e.contactDamage);
                for (int i = 0; i < 10; i++) {
                    double a = Math.random() * Math.PI * 2;
                    float spd = 60 + (float)(Math.random() * 80);
                    particles.add(new Particle(TOWER_X, TOWER_Y, new Color(255,100,150),
                        (float)Math.cos(a)*spd, (float)Math.sin(a)*spd, 0.6f, 5));
                }
                floatTexts.add(new FloatText("-" + e.contactDamage,
                    TOWER_X + rng.nextInt(20) - 10, TOWER_Y - 40, new Color(255, 60, 80)));
                if (towerHp <= 0) {
                    gameOver = true;
                    if (btnWave != null) SwingUtilities.invokeLater(() -> btnWave.setEnabled(false));
                }
            }
        }
    }

    private void checkWaveEnd() {
        if (!waveActive || spawnCount < spawnMax) return;
        if (enemies.stream().anyMatch(e -> !e.dead)) return;
        waveActive = false;
        wave++;
        enemies.clear();
        bullets.clear();
        showNotif("¡Oleada superada! ✨  Siguiente: Oleada " + wave);
        if (btnWave != null) SwingUtilities.invokeLater(() -> btnWave.setEnabled(true));
    }

    private void updateBullets(float dt) {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update(dt, particles, floatTexts, kills);
            kills = b.kills;
            if (b.dead) it.remove();
        }
    }

    private void updateParticles(float dt)  { particles.removeIf(p -> { p.update(dt); return p.dead; }); }
    private void updateFloatTexts(float dt) { floatTexts.removeIf(f -> { f.update(dt); return f.dead; }); }

    private void applyUpgrade(String type) {
        int count = upgradeCount.getOrDefault(type, 0);
        if (count >= MAX_PER_TYPE) {
            showNotif("¡Límite alcanzado! (máx " + MAX_PER_TYPE + " por mejora)");
            return;
        }
        if (towerService.applyDecorator(type)) {
            upgradeCount.put(type, count + 1);
            showNotif("¡Mejora activada! ✨  (" + (MAX_PER_TYPE - count - 1) + " uso(s) restante(s))");
        }
    }

    private void resetGame() {
        towerService.resetTower();
        upgradeCount.clear();
        enemies.clear(); bullets.clear(); particles.clear(); floatTexts.clear();
        waveActive = false; wave = 1; kills = 0;
        towerHp = MAX_TOWER_HP; gameOver = false; spawnCount = 0;
        if (btnWave != null) SwingUtilities.invokeLater(() -> btnWave.setEnabled(true));
        showNotif("¡Torre y campo reiniciados! 🌸");
    }

    private void showNotif(String msg) {
        notifMsg = msg;
        notifEnd = System.currentTimeMillis() + 2500;
    }
}
