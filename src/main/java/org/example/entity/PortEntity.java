package org.example.entity;

import jakarta.persistence.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private ComponentEntity component;

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