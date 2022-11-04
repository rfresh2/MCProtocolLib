package com.github.steveice10.mc.protocol.data.game.window;

import java.util.Objects;

public class UnknownParam implements WindowActionParam {
    private final int value;

    public UnknownParam(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnknownParam that = (UnknownParam) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
