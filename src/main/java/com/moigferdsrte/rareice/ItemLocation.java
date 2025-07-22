package com.moigferdsrte.rareice;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class ItemLocation extends Vector3d {
    public final float yaw;
    public final float pitch;

    public ItemLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = (float)(Math.random() * 360.0F); // 0-360度
        this.pitch = (float)(Math.random() * 360.0F); // 0-360度
    }

    public ItemLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static ItemLocation fromTag(CompoundTag compoundTag) {
        return new ItemLocation(
                compoundTag.getDouble("x"),
                compoundTag.getDouble("y"),
                compoundTag.getDouble("z"),
                compoundTag.getFloat("yaw"), // 使用float
                compoundTag.getFloat("pitch") // 使用float
        );
    }

    public void toTag(CompoundTag compoundTag) {
        compoundTag.putDouble("x", x);
        compoundTag.putDouble("y", y);
        compoundTag.putDouble("z", z);
        compoundTag.putFloat("yaw", yaw); // 保存为float
        compoundTag.putFloat("pitch", pitch); // 保存为float
    }
}
