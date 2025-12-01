package org.example.views;

import org.example.entity.CircuitEntity;
import org.example.entity.ComponentEntity;
import org.example.entity.ConnectorEntity;
import org.example.entity.PortEntity;
import org.example.models.UILocalComponent;
import org.example.services.CircuitService;
import org.example.services.ComponentService;
import org.example.services.ConnectorService;
import org.example.services.PortService;
import org.example.services.SimulationService;
import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.io.LED;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;

public class CircuitEditorView extends JPanel {

    // ====== CONSTANTS ======
    private static final int COMP_W = 90;
    private static final int COMP_H = 56;
    private static final int PORT_R = 6;

    // ====== BACKEND ======
    private final long circuitId;
    private final CircuitService circuitService = new CircuitService();
    private final ComponentService componentService = new ComponentService();
    private final PortService portService = new PortService();
    private final ConnectorService connectorService = new ConnectorService();
    private final SimulationService simulationService = new SimulationService();

    // ====== UI MODELS ======
    private final List<UILocalComponent> components = new ArrayList<>();
    private final List<ConnectorEntity> connectors = new ArrayList<>();
    private final Map<String, Image> icons = new HashMap<>();

    // ====== INTERACTION STATES ======
    private String placementType = null;
    private UILocalComponent dragging = null;
    private int dragOffsetX, dragOffsetY;

    private PortEntity wireStart = null;
    private Point wireCurrent = null;

    private UILocalComponent clickedComponent = null;
    private ConnectorEntity clickedWire = null;

    // ====== MENUS ======
    private JPopupMenu componentMenu;
    private JPopupMenu wireMenu;

    public CircuitEditorView(long circuitId) {
        this.circuitId = circuitId;

        setBackground(Color.WHITE);
        setLayout(null);
        setPreferredSize(new Dimension(900, 800));

        loadIcons();
        setupContextMenus();
        setupMouseEvents();
        loadFromDB();
    }

    // =================== ICON LOADING ===================
    private void loadIcons() {
        // load icons defensively: getResource may return null -> ImageIO.read(null) throws
        icons.put("AND", loadIcon("/icons/and.png"));
        icons.put("OR", loadIcon("/icons/or.png"));
        icons.put("NOT", loadIcon("/icons/not.png"));
        icons.put("XOR", loadIcon("/icons/xor.png"));
        icons.put("NAND", loadIcon("/icons/nand.png"));
        icons.put("NOR", loadIcon("/icons/nor.png"));
        icons.put("SWITCH", loadIcon("/icons/switch.png"));
        icons.put("LED", loadIcon("/icons/led.png"));
        icons.put("CLOCK", loadIcon("/icons/clock.png"));
    }

    private Image loadIcon(String resourcePath) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Icon resource not found: " + resourcePath);
                return createPlaceholderIcon();
            }
            Image img = ImageIO.read(url);
            if (img == null) {
                System.err.println("ImageIO returned null for: " + resourcePath);
                return createPlaceholderIcon();
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return createPlaceholderIcon();
        }
    }

    private Image createPlaceholderIcon() {
        BufferedImage img = new BufferedImage(COMP_W, COMP_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(new Color(230, 230, 230));
            g.fillRect(0, 0, COMP_W, COMP_H);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0, 0, COMP_W - 1, COMP_H - 1);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g.getFontMetrics();
            String s = "?";
            int tx = (COMP_W - fm.stringWidth(s)) / 2;
            int ty = (COMP_H - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(s, tx, ty);
        } finally {
            g.dispose();
        }
        return img;
    }

    // =================== CONTEXT MENUS ===================
    private void setupContextMenus() {
        componentMenu = new JPopupMenu();
        JMenuItem delComp = new JMenuItem("Delete Component");
        delComp.addActionListener(e -> deleteSelectedComponent());
        componentMenu.add(delComp);

        wireMenu = new JPopupMenu();
        JMenuItem delWire = new JMenuItem("Delete Wire");
        delWire.addActionListener(e -> deleteSelectedWire());
        wireMenu.add(delWire);

        JMenuItem colorWire = new JMenuItem("Change Color");
        colorWire.addActionListener(e -> {
            if (clickedWire != null) {
                Color newColor = JColorChooser.showDialog(this, "Choose Wire Color", parseColor(clickedWire.getColor()));
                if (newColor != null) {
                    String hex = String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                    clickedWire.setColor(hex);
                    connectorService.updateConnector(clickedWire); // Need to ensure this method exists or use save
                    loadFromDB();
                }
            }
        });
        wireMenu.add(colorWire);
    }

    // =================== MOUSE EVENTS ===================
    private void setupMouseEvents() {
        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    // =================== LOAD FROM DB ===================
    // =================== LOAD FROM DB ===================
    public void loadFromDB() {
        org.example.utils.LoadingUtil.executeWithLoading(this, "Loading Circuit...", () -> {
            // Create fresh services for background thread
            CircuitService localCircuitService = new CircuitService();
            ConnectorService localConnectorService = new ConnectorService();
            
            components.clear();
            connectors.clear();

            // No need to clear cache of local service
            CircuitEntity entity = localCircuitService.getCircuit(circuitId);
            if (entity == null) return;

            for (ComponentEntity ce : entity.getComponents()) {
                UILocalComponent u = new UILocalComponent();
                u.id = ce.getId();
                u.type = ce.getType();
                u.label = ce.getLabel();
                u.x = ce.getPosX();
                u.y = ce.getPosY();
                u.rotation = ce.getRotation();
                u.ports.addAll(ce.getPorts());
                components.add(u);
            }

            connectors.addAll(localConnectorService.getConnectorsForCircuit(circuitId));
        }, this::repaint);
    }

    // =========================================================
    // =================== PLACEMENT MODE =======================
    // =========================================================

    public void setPlacementType(String type) {
        this.placementType = type;
    }

    private void placeNewComponent(String type, int x, int y) {
        org.example.utils.LoadingUtil.executeWithLoading(this, "Placing Component...", () -> {
            CircuitService localCircuitService = new CircuitService();
            ComponentService localComponentService = new ComponentService();
            PortService localPortService = new PortService();

            ComponentEntity ce = new ComponentEntity();
            ce.setType(type);
            ce.setLabel(type);
            ce.setPosX(x);
            ce.setPosY(y);
            ce.setRotation(0);
            ce.setCircuit(localCircuitService.getCircuit(circuitId));

            localComponentService.saveComponent(ce);

            // Default ports
            if (type.equals("NOT")) {
                createPort(ce, "IN", 0, PortEntity.PortType.INPUT, localPortService);
                createPort(ce, "OUT", 0, PortEntity.PortType.OUTPUT, localPortService);
            } else if (type.equals("LED")) {
                createPort(ce, "IN", 0, PortEntity.PortType.INPUT, localPortService);
            } else if (type.equals("SWITCH") || type.equals("CLOCK")) {
                createPort(ce, "OUT", 0, PortEntity.PortType.OUTPUT, localPortService);
            } else {
                createPort(ce, "IN1", 0, PortEntity.PortType.INPUT, localPortService);
                createPort(ce, "IN2", 1, PortEntity.PortType.INPUT, localPortService);
                createPort(ce, "OUT", 0, PortEntity.PortType.OUTPUT, localPortService);
            }
        }, this::loadFromDB);
    }

    private void createPort(ComponentEntity ce, String name, int index, PortEntity.PortType type, PortService service) {
        PortEntity p = new PortEntity();
        p.setComponent(ce);
        p.setName(name);
        p.setPortIndex(index);
        p.setType(type);
        service.addPort(p);
    }

    // =========================================================
    // ==================== MOUSE HANDLERS ======================
    // =========================================================

    private void handleMousePressed(MouseEvent e) {

        Point p = e.getPoint();

        // Right-click menus
        if (SwingUtilities.isRightMouseButton(e)) {

            UILocalComponent u = findComponentAtPoint(p);
            if (u != null) {
                clickedComponent = u;
                componentMenu.show(this, p.x, p.y);
                return;
            }

            ConnectorEntity wire = findWireAtPoint(p);
            if (wire != null) {
                clickedWire = wire;
                wireMenu.show(this, p.x, p.y);
            }
            return;
        }

        // Placement mode
        if (placementType != null) {
            placeNewComponent(placementType, p.x, p.y);
            placementType = null;
            return;
        }

        // Port clicked? start or end wire
        PortEntity port = findPortAtPoint(p);
        if (port != null) {
            handlePortClicked(port, p);
            return;
        }

        // Component clicked? start dragging
        UILocalComponent u = findComponentAtPoint(p);
        if (u != null) {
            dragging = u;
            dragOffsetX = p.x - u.x;
            dragOffsetY = p.y - u.y;
            u.selected = true;
            return;
        }

    }

    private void handleMouseReleased(MouseEvent e) {
        if (dragging != null) {
            persistPosition(dragging);
            dragging = null;
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (dragging != null) {
            dragging.x = e.getX() - dragOffsetX;
            dragging.y = e.getY() - dragOffsetY;
            repaint();
        } else if (wireStart != null) {
            wireCurrent = e.getPoint();
            repaint();
        }
    }

    private void handleMouseMoved(MouseEvent e) {
        if (wireStart != null) {
            wireCurrent = e.getPoint();
            repaint();
        }
    }

    private void handleMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            UILocalComponent u = findComponentAtPoint(e.getPoint());
            if (u != null) openProperties(u);
        }
    }

    // =========================================================
    // =================== PORT/WIRE HELPERS ====================
    // =========================================================

    private void handlePortClicked(PortEntity port, Point click) {
        if (wireStart == null) {
            if (port.getType() == PortEntity.PortType.OUTPUT) {
                wireStart = port;
                wireCurrent = click;
            }
        } else {
            if (port.getType() == PortEntity.PortType.INPUT && !port.getComponent().getId().equals(wireStart.getComponent().getId())) {
                // Create connection
                org.example.utils.LoadingUtil.executeWithLoading(this, "Connecting...", () -> {
                    ConnectorService localConnectorService = new ConnectorService();
                    localConnectorService.connectPorts(wireStart, port, "#000000");
                }, () -> {
                    wireStart = null;
                    wireCurrent = null;
                    loadFromDB();
                });
            } else {
                // Cancel wire
                wireStart = null;
                wireCurrent = null;
                repaint();
            }
        }
    }

    // =========================================================
    // =================== SIMULATION REFRESH ===================
    // =========================================================

    /**
     * Run backend simulation and refresh LED visuals (sets UILocalComponent.ledLit).
     */
    public void runSimulationAndRefresh() {
        // Use local service to ensure fresh data (avoid stale cache)
        SimulationService localSimulationService = new SimulationService();
        Circuit domain = localSimulationService.runSimulation(circuitId);

        Map<String, Boolean> ledMap = new HashMap<>();
        for (Component dc : domain.getComponents()) {
            if (dc instanceof LED led) {
                String id = dc.getId(); // MapperUtil should set domain id to entity id string
                if (id != null) ledMap.put(id, led.isLit());
            }
        }

        for (UILocalComponent u : components) {
            u.ledLit = (u.id != null) && Boolean.TRUE.equals(ledMap.get(String.valueOf(u.id)));
        }
        repaint();
    }

    // =========================================================
    // =================== EXPORT TO IMAGE ======================
    // =========================================================

    public void exportToImage(java.io.File file) {
        int w = getWidth();
        int h = getHeight();
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        
        // Fill white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        
        this.paint(g2d);
        g2d.dispose();

        try {
            ImageIO.write(bi, "png", file);
            JOptionPane.showMessageDialog(this, "Image exported successfully to " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting image: " + e.getMessage());
        }
    }

    private Color parseColor(String colorName) {
        if (colorName == null) return Color.BLACK;
        try {
            return Color.decode(colorName);
        } catch (NumberFormatException e) {
            // Fallback for named colors if stored as names
            if ("red".equalsIgnoreCase(colorName)) return Color.RED;
            if ("blue".equalsIgnoreCase(colorName)) return Color.BLUE;
            if ("green".equalsIgnoreCase(colorName)) return Color.GREEN;
            return Color.BLACK;
        }
    }

    // =========================================================
    // =================== MISSING METHODS ======================
    // =========================================================

    private void deleteSelectedComponent() {
        if (clickedComponent == null) return;
        org.example.utils.LoadingUtil.executeWithLoading(this, "Deleting Component...", () -> {
            ComponentService localComponentService = new ComponentService();
            localComponentService.deleteComponent(clickedComponent.id);
        }, this::loadFromDB);
        clickedComponent = null;
    }

    private void deleteSelectedWire() {
        if (clickedWire == null) return;
        org.example.utils.LoadingUtil.executeWithLoading(this, "Deleting Wire...", () -> {
            ConnectorService localConnectorService = new ConnectorService();
            localConnectorService.deleteConnector(clickedWire.getId());
        }, this::loadFromDB);
        clickedWire = null;
    }

    private void persistPosition(UILocalComponent u) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ComponentService localComponentService = new ComponentService();
                ComponentEntity ce = localComponentService.getComponent(u.id);
                if (ce != null) {
                    ce.setPosX(u.x);
                    ce.setPosY(u.y);
                    localComponentService.saveComponent(ce);
                }
                return null;
            }
        };
        worker.execute();
    }

    private void openProperties(UILocalComponent u) {
        String newLabel = JOptionPane.showInputDialog(this, "Edit Label:", u.label);
        if (newLabel != null && !newLabel.equals(u.label)) {
            org.example.utils.LoadingUtil.executeWithLoading(this, "Saving Properties...", () -> {
                ComponentService localComponentService = new ComponentService();
                ComponentEntity ce = localComponentService.getComponent(u.id);
                if (ce != null) {
                    ce.setLabel(newLabel);
                    localComponentService.saveComponent(ce);
                }
            }, this::loadFromDB);
        }
    }

    private UILocalComponent findComponentAtPoint(Point p) {
        for (UILocalComponent u : components) {
            if (p.x >= u.x && p.x <= u.x + COMP_W && p.y >= u.y && p.y <= u.y + COMP_H) {
                return u;
            }
        }
        return null;
    }

    private ConnectorEntity findWireAtPoint(Point p) {
        for (ConnectorEntity c : connectors) {
            Point p1 = getPortPosition(c.getSourcePort());
            Point p2 = getPortPosition(c.getDestPort());
            double dist = java.awt.geom.Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
            if (dist < 5) return c;
        }
        return null;
    }

    private PortEntity findPortAtPoint(Point p) {
        for (UILocalComponent u : components) {
            for (PortEntity port : u.ports) {
                Point pos = getPortPosition(port);
                if (p.distance(pos) <= PORT_R + 2) {
                    return port;
                }
            }
        }
        return null;
    }

    private Point getPortPosition(PortEntity p) {
        if (p == null || p.getComponent() == null) return new Point(0,0);
        Long compId = p.getComponent().getId();
        UILocalComponent u = components.stream().filter(c -> c.id.equals(compId)).findFirst().orElse(null);
        if (u == null) return new Point(0,0);

        int px, py;
        if (p.getType() == PortEntity.PortType.INPUT) {
            px = u.x;
            py = u.y + 15 + (p.getPortIndex() * 20);
        } else {
            px = u.x + COMP_W;
            py = u.y + COMP_H / 2;
        }
        return new Point(px, py);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw wires
        g2.setStroke(new BasicStroke(2));
        for (ConnectorEntity c : connectors) {
            drawWire(g2, c);
        }

        // Draw current wire
        if (wireStart != null && wireCurrent != null) {
            g2.setColor(Color.GRAY);
            Point p1 = getPortPosition(wireStart);
            g2.drawLine(p1.x, p1.y, wireCurrent.x, wireCurrent.y);
        }

        // Draw components
        for (UILocalComponent u : components) {
            drawComponent(g2, u);
        }
    }

    private void drawWire(Graphics2D g2, ConnectorEntity c) {
        Point p1 = getPortPosition(c.getSourcePort());
        Point p2 = getPortPosition(c.getDestPort());
        g2.setColor(parseColor(c.getColor()));
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void drawComponent(Graphics2D g2, UILocalComponent u) {
        // Draw body
        g2.setColor(Color.WHITE);
        g2.fillRect(u.x, u.y, COMP_W, COMP_H);
        g2.setColor(u.selected ? Color.BLUE : Color.BLACK);
        g2.drawRect(u.x, u.y, COMP_W, COMP_H);

        // Draw Icon
        Image icon = icons.get(u.type);
        if (icon != null) {
            g2.drawImage(icon, u.x + 5, u.y + 5, COMP_W - 10, COMP_H - 10, null);
        } else {
            g2.drawString(u.type, u.x + 5, u.y + 20);
        }

        // Draw Label
        if (u.label != null) {
            g2.drawString(u.label, u.x, u.y - 5);
        }

        // Draw Ports
        for (PortEntity p : u.ports) {
            Point pos = getPortPosition(p);
            g2.setColor(Color.BLACK);
            g2.fillOval(pos.x - PORT_R/2, pos.y - PORT_R/2, PORT_R, PORT_R);
        }
        
        // Draw LED state
        if (u.ledLit) {
             g2.setColor(Color.RED);
             g2.fillOval(u.x + COMP_W - 15, u.y + 5, 10, 10);
        }
    }
}
