package org.example.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "component")
public class ComponentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circuit_id")
    private CircuitEntity circuit;

    private String type;       // AND, OR, SWITCH, LED, SUBCIRCUIT
    private String label;

    private int posX;
    private int posY;

    private int rotation;




    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcircuit_ref")
    private CircuitEntity subcircuitReference;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PortEntity> ports = new HashSet<>();

    public ComponentEntity() {}


    // persist clock period (optional)
    @Column(name = "clock_period")
    private Integer clockPeriod;

    // persist switch state (for SWITCH component)
    @Column(name = "switch_state")
    private Boolean switchState = false;

    // getters / setters
    public Integer getClockPeriod() { return clockPeriod; }
    public void setClockPeriod(Integer clockPeriod) { this.clockPeriod = clockPeriod; }

    public Boolean getSwitchState() { return switchState; }
    public void setSwitchState(Boolean switchState) { this.switchState = switchState; }

    // convenience boolean getter
    public boolean isSwitchState() {
        return Boolean.TRUE.equals(this.switchState);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CircuitEntity getCircuit() {
        return circuit;
    }

    public void setCircuit(CircuitEntity circuit) {
        this.circuit = circuit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public CircuitEntity getSubcircuitReference() {
        return subcircuitReference;
    }

    public void setSubcircuitReference(CircuitEntity subcircuitReference) {
        this.subcircuitReference = subcircuitReference;
    }

    public Set<PortEntity> getPorts() {
        return ports;
    }

    public void setPorts(Set<PortEntity> ports) {
        this.ports = ports;
    }
}
