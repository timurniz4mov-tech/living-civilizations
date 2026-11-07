package ru.livingcivilizations.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import ru.livingcivilizations.registry.ModItems;

public class CivilizationNpc extends PathfinderMob {
    private final NpcRole role;

    public CivilizationNpc(EntityType<? extends PathfinderMob> type, Level level, NpcRole role) {
        super(type, level);
        this.role = role;
        this.setCanPickUpLoot(role == NpcRole.BANDIT || role == NpcRole.WARRIOR);
        switch (role) {
            case GUARD -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            }
            case WARRIOR -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            }
            case BANDIT -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
            }
            case LORD -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            }
            case KING -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            }
            case TRAVELER -> {
                if (this.random.nextInt(4) == 0)
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.TRAVELERS_STAFF.get()));
            }
            default -> { }
        }
        if (!level.isClientSide) {
            String[] names = {"Альрик", "Эдгар", "Роланд", "Гуго", "Матвей", "Бруно", "Освальд", "Томас", "Герда", "Эльза"};
            this.setCustomName(Component.literal(names[this.random.nextInt(names.length)] + " — " + role.displayName()));
            this.setCustomNameVisible(false);
        }
    }

    public NpcRole getRole() {
        return role;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (role == NpcRole.GUARD || role == NpcRole.WARRIOR || role == NpcRole.BANDIT) {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, role == NpcRole.BANDIT ? 1.15D : 1.0D, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
            if (role == NpcRole.BANDIT) {
                this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
            }
        }
        double speed = (role == NpcRole.TRAVELER || role == NpcRole.CARAVAN_MERCHANT) ? 1.05D : 0.8D;
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, speed));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (role == NpcRole.CARAVAN_MERCHANT || role == NpcRole.VILLAGE_MERCHANT) {
            if (!this.level().isClientSide && !player.getAbilities().instabuild) {
                int price = role == NpcRole.CARAVAN_MERCHANT ? 2 : 1;
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.is(Items.EMERALD) && stack.getCount() >= price) {
                        stack.shrink(price);
                        player.getInventory().add(new ItemStack(Items.BREAD, 4));
                        player.displayClientMessage(Component.literal("Торговец: сделка состоялась!"), true);
                        return InteractionResult.SUCCESS;
                    }
                }
                player.displayClientMessage(Component.literal("Торговец: нужно изумрудов: " + price), true);
            } else if (!this.level().isClientSide) {
                player.getInventory().add(new ItemStack(Items.BREAD, 4));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (!this.level().isClientSide) {
            player.displayClientMessage(Component.literal(role.displayName() + ": здравствуй, путник!"), true);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }
}
