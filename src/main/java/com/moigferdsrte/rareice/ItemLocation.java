package com.moigferdsrte.rareice;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class ItemLocation extends Vector3d {
    public final float yaw;
    public final float pitch;

    public ItemLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static ItemLocation fromTag(CompoundTag compoundTag, int index) {
        return new ItemLocation(
                compoundTag.getDouble("loc_x" + index),
                compoundTag.getDouble("loc_y" + index),
                compoundTag.getDouble("loc_z" + index),
                compoundTag.getFloat("loc_yaw" + index),
                compoundTag.getFloat("loc_pitch" + index)
        );
    }

    public void toTag(CompoundTag compoundTag, int index) {
        compoundTag.putDouble("loc_x" + index, x);
        compoundTag.putDouble("loc_y" + index, y);
        compoundTag.putDouble("loc_z" + index, z);
        compoundTag.putFloat("loc_yaw" + index, yaw);
        compoundTag.putFloat("loc_pitch" + index, pitch);
    }

    public static ItemLocation fromTag(CompoundTag compoundTag) {
        return new ItemLocation(
                compoundTag.getDouble("x"),
                compoundTag.getDouble("y"),
                compoundTag.getDouble("z"),
                compoundTag.getFloat("yaw"),
                compoundTag.getFloat("pitch")
        );
    }

    public void toTag(CompoundTag compoundTag) {
        compoundTag.putDouble("x", x);
        compoundTag.putDouble("y", y);
        compoundTag.putDouble("z", z);
        compoundTag.putFloat("yaw", yaw);
        compoundTag.putFloat("pitch", pitch);
    }

    @Override
    public String toString() {
        return "ItemLocation{" +
                "yaw=" + yaw +
                ", pitch=" + pitch +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
