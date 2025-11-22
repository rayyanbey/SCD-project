package org.example.controllers;

import org.example.entity.CircuitEntity;
import org.example.services.CircuitService;
import org.example.views.CircuitEditorView;

public class CircuitController {

    private final CircuitService service;
    private final CircuitEditorView editorView;

    public CircuitController(CircuitService service, CircuitEditorView editorView) {
        this.service = service;
        this.editorView = editorView;
    }

    public void loadCircuit(Long id) {
        CircuitEntity entity = service.getCircuit(id);

        if (entity == null) {
            System.out.println("Circuit not found");
            return;
        }


        editorView.loadFromDB();
    }
}