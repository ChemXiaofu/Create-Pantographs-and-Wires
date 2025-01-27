package de.mrjulsen.wires;

public enum WireCreationContext {
    BOTH(0),
    COLLISION(1),
    RENDERING(2);

    final int index;

    private WireCreationContext(int index) {
        this.index = index;
    }

    public boolean collisionRequired() {
        return this == COLLISION || this == BOTH;
    }

    public boolean renderingRequired() {
        return this == RENDERING || this == BOTH;
    }

    public boolean allRequired() {
        return this == BOTH;
    }
}
