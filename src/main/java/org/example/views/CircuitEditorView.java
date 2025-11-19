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

public class CircuitEditorView extends JPanel {

    private final long circuitEntityId;
    private final CircuitService circuitService = new CircuitService();
    private final ComponentService componentService = new ComponentService();
    private final PortService portService = new PortService();
    private final ConnectorService connectorService = new ConnectorService();
    private final SimulationService simulationService = new SimulationService();

    private final List<UILocalComponent> components = new ArrayList<>();
    private final List<ConnectorEntity> connectors = new ArrayList<>();

    // placement mode type (null if not placing)
    private String placementType = null;

    // dragging
    private UILocalComponent dragging = null;
    private int dragOffsetX, dragOffsetY;

    // wiring
    private PortEntity wireStart = null;
    private Point wireCurrent = null;

    // UI constants
    private static final int COMP_W = 90;
    private static final int COMP_H = 56;
    private static final int PORT_R = 6;

    public CircuitEditorView(long circuitEntityId) {
        this.circuitEntityId = circuitEntityId;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 800));
        setLayout(null);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                // if in placement mode -> create new component
                if (placementType != null) {
                    placeNewComponent(placementType, p.x, p.y);
                    placementType = null;
                    repaint();
                    return;
                }

                // check port hit first (start/finish wires)
                PortEntity port = findPortAtPoint(p);
                if (port != null) {
                    handlePortClicked(port, p);
                    return;
                }

                // otherwise check if user clicked a component (start drag)
                UILocalComponent uc = findComponentAtPoint(p);
                if (uc != null) {
                    dragging = uc;
                    dragOffsetX = p.x - uc.x;
                    dragOffsetY = p.y - uc.y;
                    uc.selected = true;
                } else {
                    // click empty space clears selection
                    clearSelection();
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging != null) {
                    // persist moved component
                    persistComponentPosition(dragging);
                }
                dragging = null;

                // if drawing wire and release on an input port -> finalize handled in handlePortClicked
                // clear temporary wire
                wireCurrent = null;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging != null) {
                    Point p = e.getPoint();
                    dragging.x = p.x - dragOffsetX;
                    dragging.y = p.y - dragOffsetY;
                    repaint();
                } else if (wireStart != null) {
                    wireCurrent = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (wireStart != null) {
                    wireCurrent = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    runSimulationAndRefresh();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // initial load from DB
        loadFromDb();
    }

    // allow MainWindow to set placement type from palette
    public void setPlacementType(String type) {
        this.placementType = type;
    }

    /** Load components and connectors from DB and build UI models */
    public void loadFromDb() {
        components.clear();
        connectors.clear();

        var circuitEntity = circuitService.getCircuit(circuitEntityId);
        if (circuitEntity == null) return;

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

        // collect connectors by scanning outgoing connections
        for (UILocalComponent u : components) {
            for (PortEntity p : u.ports) {
                if (p.getType() == PortEntity.PortType.OUTPUT && p.getOutgoingConnections() != null) {
                    connectors.addAll(p.getOutgoingConnections());
                }
            }
        }

        repaint();
    }

    /** Place new component: persist entity + default ports, then reload UI */
    private void placeNewComponent(String type, int x, int y) {
        ComponentEntity ce = new ComponentEntity();
        ce.setType(type);
        ce.setLabel(type + "-" + System.currentTimeMillis()%10000);
        ce.setPosX(x);
        ce.setPosY(y);
        ce.setRotation(0);
        var circuitEntity = circuitService.getCircuit(circuitEntityId);
        ce.setCircuit(circuitEntity);

        componentService.saveComponent(ce);

        // create default ports depending on type
        if ("NOT".equalsIgnoreCase(type)) {
            PortEntity in = new PortEntity(); in.setName("IN"); in.setPortIndex(0); in.setType(PortEntity.PortType.INPUT); in.setComponent(ce); portService.addPort(in);
            PortEntity out = new PortEntity(); out.setName("OUT"); out.setPortIndex(0); out.setType(PortEntity.PortType.OUTPUT); out.setComponent(ce); portService.addPort(out);
        } else if ("LED".equalsIgnoreCase(type)) {
            PortEntity in = new PortEntity(); in.setName("IN"); in.setPortIndex(0); in.setType(PortEntity.PortType.INPUT); in.setComponent(ce); portService.addPort(in);
        } else if ("SWITCH".equalsIgnoreCase(type) || "CLOCK".equalsIgnoreCase(type)) {
            PortEntity out = new PortEntity(); out.setName("OUT"); out.setPortIndex(0); out.setType(PortEntity.PortType.OUTPUT); out.setComponent(ce); portService.addPort(out);
        } else {
            // 2 inputs + 1 output
            PortEntity in1 = new PortEntity(); in1.setName("IN1"); in1.setPortIndex(0); in1.setType(PortEntity.PortType.INPUT); in1.setComponent(ce); portService.addPort(in1);
            PortEntity in2 = new PortEntity(); in2.setName("IN2"); in2.setPortIndex(1); in2.setType(PortEntity.PortType.INPUT); in2.setComponent(ce); portService.addPort(in2);
            PortEntity out = new PortEntity(); out.setName("OUT"); out.setPortIndex(0); out.setType(PortEntity.PortType.OUTPUT); out.setComponent(ce); portService.addPort(out);
        }

        // reload UI models from DB
        loadFromDb();
    }

    /** Find component whose bounding box contains the point (topmost last) */
    private UILocalComponent findComponentAtPoint(Point p) {
        for (int i = components.size()-1; i >= 0; i--) {
            UILocalComponent u = components.get(i);
            Rectangle r = new Rectangle(u.x - COMP_W/2, u.y - COMP_H/2, COMP_W, COMP_H);
            if (r.contains(p)) return u;
        }
        return null;
    }

    /** Compute UI coordinates of a given PortEntity (same positioning logic used for drawing) */
    private Point getPortPosition(PortEntity port) {
        for (UILocalComponent u : components) {
            // find matching PortEntity in u.ports
            for (PortEntity pe : u.ports) {
                if (pe.getId().equals(port.getId())) {
                    // gather input & output lists to compute vertical spacing
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

    /** Hit test ports */
    private PortEntity findPortAtPoint(Point p) {
        for (UILocalComponent u : components) {
            // collect inputs & outputs
            List<PortEntity> ins = new ArrayList<>(), outs = new ArrayList<>();
            for (PortEntity pe : u.ports) {
                if (pe.getType() == PortEntity.PortType.INPUT) ins.add(pe);
                else outs.add(pe);
            }
            // check inputs
            for (int i = 0; i < ins.size(); i++) {
                int py = u.y - (ins.size()-1)*12 + i*24;
                int px = u.x - COMP_W/2 - PORT_R - 2;
                Rectangle pr = new Rectangle(px - PORT_R, py - PORT_R, PORT_R*2, PORT_R*2);
                if (pr.contains(p)) return ins.get(i);
            }
            // check outputs
            for (int i = 0; i < outs.size(); i++) {
                int py = u.y - (outs.size()-1)*12 + i*24;
                int px = u.x + COMP_W/2 + PORT_R + 2;
                Rectangle pr = new Rectangle(px - PORT_R, py - PORT_R, PORT_R*2, PORT_R*2);
                if (pr.contains(p)) return outs.get(i);
            }
        }
        return null;
    }

    /** When user clicks a port: start or finish wire drawing */
    private void handlePortClicked(PortEntity port, Point clickPoint) {
        if (wireStart == null) {
            // can only start from OUTPUT
            if (port.getType() == PortEntity.PortType.OUTPUT) {
                wireStart = port;
                wireCurrent = clickPoint; // visual feedback
            }
        } else {
            // if we have a start, and clicked an INPUT on some component, create connector
            if (port.getType() == PortEntity.PortType.INPUT) {
                connectorService.connectPorts(wireStart, port, "black");
                // refresh connectors from DB
                loadFromDb();
            }
            // clear wire drawing mode
            wireStart = null;
            wireCurrent = null;
            repaint();
        }
    }

    /** Persist the moved component's new coordinates to DB */
    private void persistComponentPosition(UILocalComponent u) {
        if (u.id == null) return;
        var ce = componentService.getComponent(u.id);
        if (ce == null) return;
        ce.setPosX(u.x);
        ce.setPosY(u.y);
        componentService.updateComponent(ce);
    }

    /** Clear selection highlight */
    private void clearSelection() {
        for (UILocalComponent u : components) {
            u.selected = false;
        }
    }

    /** Run backend simulation and update UI LED highlights */
    public void runSimulationAndRefresh() {
        // 1) run simulation and get domain circuit
        var domain = simulationService.runSimulation(circuitEntityId);

        // 2) build mapping of entity-id -> domain LED state
        Map<String, Boolean> ledById = new HashMap<>();
        for (Component d : domain.getComponents()) {
            if (d instanceof LED led) {
                String id = d.getId(); // MapperUtil should set domain id = entity id string
                if (id != null) ledById.put(id, led.isLit());
            }
        }

        // 3) update UI model led lit flags
        for (UILocalComponent u : components) {
            boolean lit = (u.id != null) && Boolean.TRUE.equals(ledById.get(String.valueOf(u.id)));
            u.ledLit = lit;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw wires (connectors)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        for (ConnectorEntity c : connectors) {
            Point s = getPortPosition(c.getSourcePort());
            Point d = getPortPosition(c.getDestPort());
            if (s != null && d != null) g.drawLine(s.x, s.y, d.x, d.y);
        }

        // draw temporary wire if any
        if (wireStart != null && wireCurrent != null) {
            Point s = getPortPosition(wireStart);
            if (s != null) {
                g.setColor(Color.GRAY);
                g.drawLine(s.x, s.y, wireCurrent.x, wireCurrent.y);
            }
        }

        // draw components
        for (UILocalComponent u : components) {
            int cx = u.x, cy = u.y;
            // body
            g.setColor(new Color(245,245,245));
            g.fillRoundRect(cx - COMP_W/2, cy - COMP_H/2, COMP_W, COMP_H, 12, 12);
            g.setColor(Color.DARK_GRAY);
            g.drawRoundRect(cx - COMP_W/2, cy - COMP_H/2, COMP_W, COMP_H, 12, 12);

            // label/type
            g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
            g.drawString(u.type, cx - COMP_W/4, cy);

            // highlight if selected
            if (u.selected) {
                g.setColor(Color.ORANGE);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(cx - COMP_W/2 - 2, cy - COMP_H/2 - 2, COMP_W + 4, COMP_H + 4, 12, 12);
            }

            // LED indicator (green) if lit
            if ("LED".equalsIgnoreCase(u.type) && u.ledLit) {
                g.setColor(Color.GREEN.darker());
                g.fillOval(cx + COMP_W/2 - 18, cy - 12, 16, 16);
            }

            // draw ports (inputs left, outputs right)
            List<PortEntity> ins = new ArrayList<>(), outs = new ArrayList<>();
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
}