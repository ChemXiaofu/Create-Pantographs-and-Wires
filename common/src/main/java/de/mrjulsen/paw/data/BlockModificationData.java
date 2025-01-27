package de.mrjulsen.paw.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record BlockModificationData(BlockPos newPos, Direction newDirection) { }
