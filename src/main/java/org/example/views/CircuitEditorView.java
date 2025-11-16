package org.example.views;

import org.example.domain.Circuit;
import org.example.domain.Component;
import org.example.domain.io.LED;
import org.example.entity.CircuitEntity;
import org.example.entity.ComponentEntity;
import org.example.entity.ConnectorEntity;
import org.example.entity.PortEntity;
import org.example.models.UILocalComponent;
import org.example.services.*;

import javax.swing.*;
import java.awt.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Canvas for drawing components and wires, handling placement and drag/drop.
 */
public class CircuitEditorView extends JPanel {

    private final long circuitEntityId;
    private final CircuitService circuitService = new CircuitService();
    private final ComponentService componentService = new ComponentService();
    private final PortService portService = new PortService();
    private final ConnectorService connectorService = new ConnectorService();
    private final SimulationService simulationService = new SimulationService();

    private final java.util.List<UILocalComponent> components = new ArrayList<>();
    private final java.util.List<ConnectorEntity> connectors = new ArrayList<>();

    // placement mode: null if not placing, otherwise type string
    private String placementType = null;

    // dragging
    private UILocalComponent draggingComp = null;
    private int dragOffsetX, dragOffsetY;

    // wire drawing (start from an output port)
    private PortEntity wireStartPort = null;
    private Point currentMousePoint = null;

    // UI scale constants
    private static final int COMP_WIDTH = 80;
    private static final int COMP_HEIGHT = 50;
    private static final int PORT_RADIUS = 6;

    public CircuitEditorView(long circuitEntityId) {
        this.circuitEntityId = circuitEntityId;
        setBackground(Color.WHITE);
        setLayout(null);
        setPreferredSize(new Dimension(800, 800));

        // mouse events
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                // if in placement mode -> create new ComponentEntity and UI model
                if (placementType != null) {
                    placeNewComponent(placementType, p.x, p.y);
                    placementType = null;
                    repaint();
                    return;
                }

                // check if click on a port (for wire start or end)
                PortEntity port = findPortAtPoint(p);
                if (port != null) {
                    handlePortClick(port, p);
                    return;
                }

                // check if clicked a component (start drag)
                UILocalComponent c = findComponentAtPoint(p);
                if (c != null) {
                    draggingComp = c;
                    dragOffsetX = p.x - c.x;
                    dragOffsetY = p.y - c.y;
                } else {
                    // click on empty space -> clear selection state
                    clearHighlights();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingComp = null;

                // if a wire is being drawn and we have a start + release on an input port, finalize
                if (wireStartPort != null) {
                    Point p = e.getPoint();
                    PortEntity destPort = findPortAtPoint(p);
                    if (destPort != null && destPort.getType() == PortEntity.PortType.INPUT) {
                        // create connector in DB
                        ConnectorEntity conn = new ConnectorEntity();
                        conn.setSourcePort(wireStartPort);
                        conn.setDestPort(destPort);
                        connectorService.connectPorts(wireStartPort, destPort, "black"); // uses repo save
                        // reload connectors from DB
                        loadConnectors();
                    }
                    wireStartPort = null;
                    currentMousePoint = null;
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                currentMousePoint = p;
                if (draggingComp != null) {
                    draggingComp.x = p.x - dragOffsetX;
                    draggingComp.y = p.y - dragOffsetY;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMousePoint = e.getPoint();
                if (wireStartPort != null) repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // double-click simulation convenience: double click to simulate
                if (e.getClickCount() == 2) {
                    runSimulationAndRefresh();
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);

        // initial load
        loadFromDb();
    }

    /**
     * Set placement type chosen from palette.
     */
    public void setPlacementType(String type) {
        this.placementType = type;
    }

    /**
     * Load components and connectors from DB for this circuit.
     */
    public void loadFromDb() {
        components.clear();
        connectors.clear();

        var circuitEntity = circuitService.getCircuit(circuitEntityId);
        if (circuitEntity == null) return;

        // create UILocalComponent for each ComponentEntity
        for (ComponentEntity ce : circuitEntity.getComponents()) {
            UILocalComponent u = new UILocalComponent();
            u.id = ce.getId();
            u.type = ce.getType();
            u.label = ce.getLabel();
            u.x = ce.getPosX();
            u.y = ce.getPosY();
            u.rotation = ce.getRotation();
            u.ports.addAll(ce.getPorts()); // store PortEntity references for hit-testing
            components.add(u);
        }

        // load connectors
        loadConnectors();

        repaint();
    }

    private void loadConnectors() {
        connectors.clear();
        // simple approach: scan all component ports for outgoing connections
        for (UILocalComponent u : components) {
            for (PortEntity pe : u.ports) {
                if (pe.getType() == PortEntity.PortType.OUTPUT) {
                    if (pe.getOutgoingConnections() != null) {
                        connectors.addAll(pe.getOutgoingConnections());
                    }
                }
            }
        }
    }

    /**
     * Place a new component on canvas (persist to DB with default ports).
     */
    private void placeNewComponent(String type, int x, int y) {
        // 1. create entity
        ComponentEntity ce = new ComponentEntity();
        ce.setType(type);
        ce.setLabel(type + "-" + System.currentTimeMillis()%10000);
        ce.setPosX(x);
        ce.setPosY(y);
        ce.setRotation(0);
        var circuitEntity = circuitService.getCircuit(circuitEntityId);
        ce.setCircuit(circuitEntity);

        // persist component
        componentService.saveComponent(ce);

        // create default ports based on type
        if (type.equalsIgnoreCase("NOT") || type.equalsIgnoreCase("LED")) {
            PortEntity in = new PortEntity();
            in.setName("IN");
            in.setPortIndex(0);
            in.setType(PortEntity.PortType.INPUT);
            in.setComponent(ce);
            portService.addPort(in);

            if (type.equalsIgnoreCase("LED")) {
                // LED has only input
            }
        } else if (type.equalsIgnoreCase("SWITCH") || type.equalsIgnoreCase("CLOCK")) {
            PortEntity out = new PortEntity();
            out.setName("OUT");
            out.setPortIndex(0);
            out.setType(PortEntity.PortType.OUTPUT);
            out.setComponent(ce);
            portService.addPort(out);
        } else {
            // normal gates: 2 inputs + 1 output
            PortEntity in1 = new PortEntity();
            in1.setName("IN1");
            in1.setPortIndex(0);
            in1.setType(PortEntity.PortType.INPUT);
            in1.setComponent(ce);
            portService.addPort(in1);

            PortEntity in2 = new PortEntity();
            in2.setName("IN2");
            in2.setPortIndex(1);
            in2.setType(PortEntity.PortType.INPUT);
            in2.setComponent(ce);
            portService.addPort(in2);

            PortEntity out = new PortEntity();
            out.setName("OUT");
            out.setPortIndex(0);
            out.setType(PortEntity.PortType.OUTPUT);
            out.setComponent(ce);
            portService.addPort(out);
        }

        // re-load UI state
        loadFromDb();
    }

    /**
     * Find UI component at a point (hit-test using bounding box)
     */
    private UILocalComponent findComponentAtPoint(Point p) {
        for (int i = components.size()-1; i >=0; i--) {
            UILocalComponent c = components.get(i);
            Rectangle r = new Rectangle(c.x - COMP_WIDTH/2, c.y - COMP_HEIGHT/2, COMP_WIDTH, COMP_HEIGHT);
            if (r.contains(p)) return c;
        }
        return null;
    }

    /**
     * Find a port (PortEntity) at a canvas point.
     * We compute port positions relative to its component bounding box:
     * - INPUT ports on left side, evenly spaced
     * - OUTPUT ports on right side
     */
    private PortEntity findPortAtPoint(Point p) {
        for (UILocalComponent c : components) {
            int cx = c.x;
            int cy = c.y;
            // inputs
            List<PortEntity> inputs = new ArrayList<>();
            List<PortEntity> outputs = new ArrayList<>();
            for (PortEntity pe : c.ports) {
                if (pe.getType() == PortEntity.PortType.INPUT) inputs.add(pe);
                else outputs.add(pe);
            }

            // inputs: distribute vertically
            int inCount = inputs.size();
            for (int i = 0; i < inCount; i++) {
                int py = cy - (inCount-1)*12 + i*24;
                int px = cx - COMP_WIDTH/2 - PORT_RADIUS;
                Rectangle pr = new Rectangle(px-PORT_RADIUS, py-PORT_RADIUS, PORT_RADIUS*2, PORT_RADIUS*2);
                if (pr.contains(p)) return inputs.get(i);
            }
            // outputs:
            int outCount = outputs.size();
            for (int i=0;i<outCount;i++) {
                int py = cy - (outCount-1)*12 + i*24;
                int px = cx + COMP_WIDTH/2 + PORT_RADIUS;
                Rectangle pr = new Rectangle(px-PORT_RADIUS, py-PORT_RADIUS, PORT_RADIUS*2, PORT_RADIUS*2);
                if (pr.contains(p)) return outputs.get(i);
            }
        }
        return null;
    }

    /**
     * When user clicks a port.
     */
    private void handlePortClick(PortEntity port, Point p) {
        if (wireStartPort == null) {
            // start a wire only from an OUTPUT port
            if (port.getType() == PortEntity.PortType.OUTPUT) {
                wireStartPort = port;
                currentMousePoint = p;
            }
        } else {
            // if we have a start, clicking another port might finish the wire
            if (port.getType() == PortEntity.PortType.INPUT) {
                // create connector
                connectorService.connectPorts(wireStartPort, port, "black");
                loadConnectors();
            }
            wireStartPort = null;
            currentMousePoint = null;
            repaint();
        }
    }

    /**
     * Clear all selection highlights.
     */
    private void clearHighlights() {
        for (UILocalComponent u : components) u.highlighted = false;
    }

    /**
     * Persist moved component's new location (on drag release)
     */
    private void persistComponentPosition(UILocalComponent u) {
        if (u.id == null) return;
        var ce = componentService.getComponent(u.id);
        if (ce == null) return;
        ce.setPosX(u.x);
        ce.setPosY(u.y);
        componentService.updateComponent(ce);
    }

    /**
     * Trigger simulation via SimulationService and update LED UI
     */
    public void runSimulationAndRefresh() {
        // run simulation (returns Domain Circuit)
        Circuit domainCircuit = simulationService.runSimulation(circuitEntityId);

        // map LED states back to UI components (MapperUtil sets domain component id = entity id string)
        Map<String, Boolean> ledStateById = new HashMap<>();
        for (Component dcomp : domainCircuit.getComponents()) {
            if (dcomp instanceof LED led) {
                String idStr = dcomp.getId();
                // idStr should be entity id as string
                ledStateById.put(idStr, led.isLit());
            }
        }

        // update UI (we'll store highlight or color)
        for (UILocalComponent u : components) {
            if (u.id != null && ledStateById.containsKey(String.valueOf(u.id))) {
                boolean lit = ledStateById.get(String.valueOf(u.id));
                u.highlighted = lit;
            } else {
                u.highlighted = false;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw all connectors (wires)
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLACK);
        for (ConnectorEntity conn : connectors) {
            PortEntity sp = conn.getSourcePort();
            PortEntity dp = conn.getDestPort();
            Point s = findPortPosition(sp);
            Point d = findPortPosition(dp);
            if (s != null && d != null) {
                g.drawLine(s.x, s.y, d.x, d.y);
            }
        }

        // draw in-progress wire
        if (wireStartPort != null && currentMousePoint != null) {
            Point s = findPortPosition(wireStartPort);
            if (s != null) {
                g.setColor(Color.GRAY);
                g.drawLine(s.x, s.y, currentMousePoint.x, currentMousePoint.y);
            }
        }

        // draw components
        for (UILocalComponent u : components) {
            int cx = u.x;
            int cy = u.y;
            int w = COMP_WIDTH;
            int h = COMP_HEIGHT;
            // box
            g.setColor(new Color(240, 240, 240));
            g.fillRoundRect(cx - w/2, cy - h/2, w, h, 12, 12);
            g.setColor(Color.DARK_GRAY);
            g.drawRoundRect(cx - w/2, cy - h/2, w, h, 12, 12);

            // label
            g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
            g.drawString(u.type, cx - w/4, cy);

            // highlight if LED is lit
            if (u.highlighted && u.type.equalsIgnoreCase("LED")) {
                g.setColor(Color.GREEN.darker());
                g.fillOval(cx + w/2 - 16, cy - 12, 16, 16);
            }

            // draw ports
            int inCount = (int) u.ports.stream().filter(p -> p.getType() == PortEntity.PortType.INPUT).count();
            int outCount = (int) u.ports.stream().filter(p -> p.getType() == PortEntity.PortType.OUTPUT).count();

            // inputs on left
            int iIndex = 0;
            for (PortEntity pe : u.ports) {
                if (pe.getType() == PortEntity.PortType.INPUT) {
                    int py = cy - (inCount-1)*12 + iIndex*24;
                    int px = cx - w/2 - PORT_RADIUS;
                    g.setColor(Color.BLUE.darker());
                    g.fillOval(px - PORT_RADIUS, py - PORT_RADIUS, PORT_RADIUS*2, PORT_RADIUS*2);
                    iIndex++;
                }
            }
            // outputs on right
            int oIndex = 0;
            for (PortEntity pe : u.ports) {
                if (pe.getType() == PortEntity.PortType.OUTPUT) {
                    int py = cy - (outCount-1)*12 + oIndex*24;
                    int px = cx + w/2 + PORT_RADIUS;
                    g.setColor(Color.RED.darker());
                    g.fillOval(px - PORT_RADIUS, py - PORT_RADIUS, PORT_RADIUS*2, PORT_RADIUS*2);
                    oIndex++;
                }
            }

            // draw border when highlighted (selection)
            if (u.highlighted && !u.type.equalsIgnoreCase("LED")) {
                g.setColor(Color.ORANGE);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(cx - w/2, cy - h/2, w, h, 12, 12);
            }
        }
    }

    /**
     * Find the canvas coordinates of a given PortEntity by looking up the parent component
     * and computing its position (same logic as hit-testing).
     */
    private Point findPortPosition(PortEntity port) {
        for (UILocalComponent u : components) {
            for (int i=0;i<u.ports.size();i++) {
                PortEntity pe = u.ports.get(i);
                if (pe.getId().equals(port.getId())) {
                    int cx = u.x;
                    int cy = u.y;

                    // determine index among input or output
                    List<PortEntity> inputs = new ArrayList<>();
                    List<PortEntity> outputs = new ArrayList<>();
                    for (PortEntity p : u.ports) {
                        if (p.getType() == PortEntity.PortType.INPUT) inputs.add(p);
                        else outputs.add(p);
                    }
                    if (pe.getType() == PortEntity.PortType.INPUT) {
                        int idx = inputs.indexOf(pe);
                        int py = cy - (inputs.size()-1)*12 + idx*24;
                        int px = cx - COMP_WIDTH/2 - PORT_RADIUS;
                        return new Point(px, py);
                    } else {
                        int idx = outputs.indexOf(pe);
                        int py = cy - (outputs.size()-1)*12 + idx*24;
                        int px = cx + COMP_WIDTH/2 + PORT_RADIUS;
                        return new Point(px, py);
                    }
                }
            }
        }
        return null;
    }

    /**
     * When dragging ends, persist component position changes
     */
    @Override
    public void addNotify() {
        super.addNotify();
        // Listen to mouse release events at top-level to persist after drag
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggingComp != null) {
                    persistComponentPosition(draggingComp);
                } else {
                    // persist positions of any components moved recently
                    for (UILocalComponent u : components) {
                        // naive approach: always update to ensure DB sync (could be optimized)
                        if (u.id != null) {
                            persistComponentPosition(u);
                        }
                    }
                }
            }
        });
    }
}