package de.mrjulsen.paw.blockentity;

public interface IMultiblockBlockEntity {
    int getXOffset();
    int getYOffset();
    int getZOffset();
    int getXSize();
    int getYSize();
    int getZSize();
    void setOffset(int x, int y, int z);
}
