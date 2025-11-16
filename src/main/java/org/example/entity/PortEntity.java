package org.example.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "port")
public class PortEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int portIndex;

    @Enumerated(EnumType.STRING)
    private PortType type;

    @OneToMany(mappedBy = "sourcePort", cascade = CascadeType.ALL)
    private List<ConnectorEntity> outgoingConnections;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private ComponentEntity component;

    public List<ConnectorEntity> getOutgoingConnections() {
        return outgoingConnections;
    }

    public void setOutgoingConnections(List<ConnectorEntity> outgoingConnections) {
        this.outgoingConnections = outgoingConnections;
    }

    public enum PortType {
        INPUT, OUTPUT
    }

    public PortEntity() {}


    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getPortIndex() { return portIndex; }

    public void setPortIndex(int portIndex) { this.portIndex = portIndex; }

    public PortType getType() { return type; }

    public void setType(PortType type) { this.type = type; }

    public ComponentEntity getComponent() { return component; }

    public void setComponent(ComponentEntity component) { this.component = component; }
}