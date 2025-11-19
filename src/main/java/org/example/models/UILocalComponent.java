package org.example.models;

import org.example.entity.PortEntity;

import java.util.ArrayList;
import java.util.List;

public class UILocalComponent {
    public Long id;            // ComponentEntity id
    public String type;
    public String label;
    public int x;
    public int y;
    public int rotation;
    public List<PortEntity> ports = new ArrayList<>(); // port entities mapped when loaded


    // runtime flags
    public boolean selected = false;
    public boolean ledLit = false;
}