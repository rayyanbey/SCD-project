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
        try {
            icons.put("AND", ImageIO.read(getClass().getResource("/icons/and.png")));
            icons.put("OR", ImageIO.read(getClass().getResource("/icons/or.png")));
            icons.put("NOT", ImageIO.read(getClass().getResource("/icons/not.png")));
            icons.put("XOR", ImageIO.read(getClass().getResource("/icons/xor.png")));
            icons.put("NAND", ImageIO.read(getClass().getResource("/icons/nand.png")));
            icons.put("NOR", ImageIO.read(getClass().getResource("/icons/nor.png")));
            icons.put("SWITCH", ImageIO.read(getClass().getResource("/icons/switch.png")));
            icons.put("LED", ImageIO.read(getClass().getResource("/icons/led.png")));
            icons.put("CLOCK", ImageIO.read(getClass().getResource("/icons/clock.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void loadFromDB() {
        components.clear();
        connectors.clear();

        CircuitEntity entity = circuitService.getCircuit(circuitId);
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

        for (UILocalComponent u : components) {
            for (PortEntity p : u.ports) {
                connectors.addAll(connectorService.getOutgoingConnections(p.getId()));
            }
        }

        repaint();
    }

    // =========================================================
    // =================== PLACEMENT MODE =======================
    // =========================================================

    public void setPlacementType(String type) {
        this.placementType = type;
    }

    private void placeNewComponent(String type, int x, int y) {
        ComponentEntity ce = new ComponentEntity();
        ce.setType(type);
        ce.setLabel(type);
        ce.setPosX(x);
        ce.setPosY(y);
        ce.setRotation(0);
        ce.setCircuit(circuitService.getCircuit(circuitId));

        componentService.saveComponent(ce);

        // Default ports
        if (type.equals("NOT")) {
            createPort(ce, "IN", 0, PortEntity.PortType.INPUT);
            createPort(ce, "OUT", 0, PortEntity.PortType.OUTPUT);
        } else if (type.equals("LED")) {
            createPort(ce, "IN", 0, PortEntity.PortType.INPUT);
        } else if (type.equals("SWITCH") || type.equals("CLOCK")) {
            createPort(ce, "OUT", 0, PortEntity.PortType.OUTPUT);
        } else {
            createPort(ce, "IN1", 0, PortEntity.PortType.INPUT);
            createPort(ce, "IN2", 1, PortEntity.PortType.INPUT);
            createPort(ce, "OUT", 0, PortEntity.PortType.OUTPUT);
        }

        loadFromDB();
    }

    private void createPort(ComponentEntity ce, String name, int index, PortEntity.PortType type) {
        PortEntity p = new PortEntity();
        p.setComponent(ce);
        p.setName(name);
        p.setPortIndex(index);
        p.setType(type);
        portService.addPort(p);
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
            if (port.getType() == PortEntity.PortType.INPUT) {
                connectorService.connectPorts(wireStart, port, "black");
                loadFromDB();
            }
            wireStart = null;
            wireCurrent = null;
        }
    }

    // =========================================================
    // =================== PAINT & HELPERS ======================
    // =========================================================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw connectors/wires
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLACK);
        for (ConnectorEntity c : connectors) {
            Point s = getPortPosition(c.getSourcePort());
            Point d = getPortPosition(c.getDestPort());
            if (s != null && d != null) {
                g.drawLine(s.x, s.y, d.x, d.y);
            }
        }

        // draw temporary wire while drawing
        if (wireStart != null && wireCurrent != null) {
            Point s = getPortPosition(wireStart);
            if (s != null) {
                g.setColor(Color.GRAY);
                g.drawLine(s.x, s.y, wireCurrent.x, wireCurrent.y);
            }
        }

        // draw components (icons or fallback)
        for (UILocalComponent u : components) {
            int cx = u.x;
            int cy = u.y;

            Image icon = icons.get(u.type != null ? u.type.toUpperCase() : null);
            if (icon != null) {
                g.drawImage(icon, cx - COMP_W / 2, cy - COMP_H / 2, COMP_W, COMP_H, this);
            } else {
                g.setColor(new Color(245,245,245));
                g.fillRoundRect(cx - COMP_W/2, cy - COMP_H/2, COMP_W, COMP_H, 12, 12);
                g.setColor(Color.DARK_GRAY);
                g.drawRoundRect(cx - COMP_W/2, cy - COMP_H/2, COMP_W, COMP_H, 12, 12);
                g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
                g.drawString(u.type == null ? "?" : u.type, cx - COMP_W/4, cy);
            }

            // selection border
            if (u.selected) {
                g.setColor(Color.ORANGE);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(cx - COMP_W/2 - 3, cy - COMP_H/2 - 3, COMP_W + 6, COMP_H + 6, 12, 12);
            }

            // LED indicator if lit
            if ("LED".equalsIgnoreCase(u.type) && u.ledLit) {
                g.setColor(Color.GREEN.darker());
                g.fillOval(cx + COMP_W/2 - 18, cy - 12, 16, 16);
            }

            // draw ports: inputs on left, outputs on right
            List<PortEntity> ins = new ArrayList<>();
            List<PortEntity> outs = new ArrayList<>();
            for (PortEntity p : u.ports) {
                if (p.getType() == PortEntity.PortType.INPUT) ins.add(p);
                else outs.add(p);
            }

            for (int i = 0; i < ins.size(); i++) {
                int py = cy - (ins.size()-1)*12 + i*24;
                int px = cx - COMP_W/2 - PORT_R - 2;
                g.setColor(Color.BLUE.darker());
                g.fillOval(px - PORT_R, py - PORT_R, PORT_R*2, PORT_R*2);
            }

            for (int i = 0; i < outs.size(); i++) {
                int py = cy - (outs.size()-1)*12 + i*24;
                int px = cx + COMP_W/2 + PORT_R + 2;
                g.setColor(Color.RED.darker());
                g.fillOval(px - PORT_R, py - PORT_R, PORT_R*2, PORT_R*2);
            }
        }
    }

    /**
     * Find the UILocalComponent whose bounding box contains the given point.
     */
    private UILocalComponent findComponentAtPoint(Point p) {
        for (int i = components.size() - 1; i >= 0; i--) {
            UILocalComponent u = components.get(i);
            Rectangle r = new Rectangle(u.x - COMP_W/2, u.y - COMP_H/2, COMP_W, COMP_H);
            if (r.contains(p)) return u;
        }
        return null;
    }

    /**
     * Find a PortEntity at the canvas point (hit test).
     */
    private PortEntity findPortAtPoint(Point p) {
        for (UILocalComponent u : components) {
            List<PortEntity> ins = new ArrayList<>(), outs = new ArrayList<>();
            for (PortEntity pe : u.ports) {
                if (pe.getType() == PortEntity.PortType.INPUT) ins.add(pe);
                else outs.add(pe);
            }

            for (int i = 0; i < ins.size(); i++) {
                int py = u.y - (ins.size()-1)*12 + i*24;
                int px = u.x - COMP_W/2 - PORT_R - 2;
                Rectangle pr = new Rectangle(px - PORT_R, py - PORT_R, PORT_R*2, PORT_R*2);
                if (pr.contains(p)) return ins.get(i);
            }

            for (int i = 0; i < outs.size(); i++) {
                int py = u.y - (outs.size()-1)*12 + i*24;
                int px = u.x + COMP_W/2 + PORT_R + 2;
                Rectangle pr = new Rectangle(px - PORT_R, py - PORT_R, PORT_R*2, PORT_R*2);
                if (pr.contains(p)) return outs.get(i);
            }
        }
        return null;
    }

    /**
     * Compute canvas coordinates of a PortEntity.
     */
    private Point getPortPosition(PortEntity port) {
        for (UILocalComponent u : components) {
            for (PortEntity pe : u.ports) {
                if (pe.getId().equals(port.getId())) {
                    List<PortEntity> ins = new ArrayList<>(), outs = new ArrayList<>();
                    for (PortEntity p : u.ports) {
                        if (p.getType() == PortEntity.PortType.INPUT) ins.add(p);
                        else outs.add(p);
                    }
                    if (port.getType() == PortEntity.PortType.INPUT) {
                        int idx = ins.indexOf(port);
                        int py = u.y - (ins.size()-1)*12 + idx*24;
                        int px = u.x - COMP_W/2 - PORT_R - 2;
                        return new Point(px, py);
                    } else {
                        int idx = outs.indexOf(port);
                        int py = u.y - (outs.size()-1)*12 + idx*24;
                        int px = u.x + COMP_W/2 + PORT_R + 2;
                        return new Point(px, py);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find a wire (connector) near the point (line hit test).
     */
    private ConnectorEntity findWireAtPoint(Point p) {
        for (ConnectorEntity c : connectors) {
            Point s = getPortPosition(c.getSourcePort());
            Point d = getPortPosition(c.getDestPort());
            if (s == null || d == null) continue;
            double dist = ptLineDist(s.x, s.y, d.x, d.y, p.x, p.y);
            if (dist < 6) return c;
        }
        return null;
    }

    /**
     * Distance from point to line segment.
     */
    private double ptLineDist(int x1, int y1, int x2, int y2, int px, int py) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = -1;
        if (lenSq != 0) param = dot / lenSq;

        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }
        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx*dx + dy*dy);
    }

    // =========================================================
    // =================== DELETE OPERATIONS ====================
    // =========================================================

    private void deleteSelectedComponent() {
        if (clickedComponent == null) return;

        // delete connectors attached to ports (outgoing & incoming)
        for (PortEntity p : clickedComponent.ports) {
            // outgoing
            List<ConnectorEntity> outgoing = connectorService.getOutgoingConnections(p.getId());
            for (ConnectorEntity c : outgoing) {
                connectorService.deleteConnector(c.getId());
            }
            // incoming
            List<ConnectorEntity> incoming = connectorService.getIncomingConnections(p.getId());
            for (ConnectorEntity c : incoming) {
                connectorService.deleteConnector(c.getId());
            }
            // delete port
            portService.deletePort(p.getId());
        }

        // delete component entity
        componentService.deleteComponent(clickedComponent.id);

        // reload
        loadFromDB();
        clickedComponent = null;
        repaint();
    }

    private void deleteSelectedWire() {
        if (clickedWire == null) return;
        connectorService.deleteConnector(clickedWire.getId());
        loadFromDB();
        clickedWire = null;
        repaint();
    }

    // =========================================================
    // =================== PERSIST / PROPERTIES =================
    // =========================================================

    private void persistPosition(UILocalComponent u) {
        if (u.id == null) return;
        ComponentEntity ce = componentService.getComponent(u.id);
        if (ce == null) return;
        ce.setPosX(u.x);
        ce.setPosY(u.y);
        componentService.updateComponent(ce);
    }

    private void openProperties(UILocalComponent u) {
        if (u == null || u.id == null) return;
        ComponentEntity ce = componentService.getComponent(u.id);
        if (ce == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Component Properties", true);
        dlg.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        dlg.add(new JLabel("Label:"), gbc);
        gbc.gridx = 1;
        JTextField labelField = new JTextField(ce.getLabel() == null ? "" : ce.getLabel(), 20);
        dlg.add(labelField, gbc);

        gbc.gridx = 0; gbc.gridy++;

        JCheckBox switchStateBox;
        if ("SWITCH".equalsIgnoreCase(ce.getType())) {
            dlg.add(new JLabel("Switch On:"), gbc);
            gbc.gridx = 1;
            switchStateBox = new JCheckBox();
            try {
                switchStateBox.setSelected(ce.isSwitchState());
            } catch (Exception ignored) {}
            dlg.add(switchStateBox, gbc);
            gbc.gridx = 0; gbc.gridy++;
        } else {
            switchStateBox = null;
        }

        // clock period - if your ComponentEntity supports a 'period' property you can add UI here.
        // Example (uncomment if supported):
        // if ("CLOCK".equalsIgnoreCase(ce.getType())) { ... }

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        dlg.add(saveBtn, gbc);

        saveBtn.addActionListener(ev -> {
            ce.setLabel(labelField.getText());
            if (switchStateBox != null) {
                ce.setSwitchState(switchStateBox.isSelected());
            }
            componentService.updateComponent(ce);
            loadFromDB();
            dlg.dispose();
        });

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // =========================================================
    // =================== SIMULATION REFRESH ===================
    // =========================================================

    /**
     * Run backend simulation and refresh LED visuals (sets UILocalComponent.ledLit).
     */
    public void runSimulationAndRefresh() {
        Circuit domain = simulationService.runSimulation(circuitId);

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
}


