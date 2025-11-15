package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "connector")
public class ConnectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_port_id")
    private PortEntity sourcePort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_port_id")
    private PortEntity destPort;

    public ConnectorEntity() {}

    // Getters & Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getColor() { return color; }

    public void setColor(String color) { this.color = color; }

    public PortEntity getSourcePort() { return sourcePort; }

    public void setSourcePort(PortEntity sourcePort) { this.sourcePort = sourcePort; }

    public PortEntity getDestPort() { return destPort; }

    public void setDestPort(PortEntity destPort) { this.destPort = destPort; }
}