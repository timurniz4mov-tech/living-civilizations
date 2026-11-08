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
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
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
        registerRoleCombatGoals();
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
            case WORKER -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_AXE));
            }
            case BANDIT -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
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
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D);
    }

    private void registerRoleCombatGoals() {
        if (role == NpcRole.BANDIT) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.18D, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, CivilizationNpc.class, 10,
                    true, false, target -> target.getRole() != NpcRole.BANDIT));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10,
                    true, false, target -> !target.isCreative() && !target.isSpectator()));
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        } else if (role == NpcRole.GUARD || role == NpcRole.WARRIOR) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12D, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, CivilizationNpc.class, 10,
                    true, false, target -> target.getRole() == NpcRole.BANDIT));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        }
    }

    @Override
    protected void registerGoals() {
        // Role can still be null while the Mob constructor invokes this override.
        // Add only role-independent goals here.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.tickCount % 15 != 0 || (this.getTarget() != null && this.getTarget().isAlive())) return;

        if (role == NpcRole.BANDIT) {
            CivilizationNpc civilian = this.level().getEntitiesOfClass(CivilizationNpc.class,
                    this.getBoundingBox().inflate(22.0D),
                    other -> other != this && other.isAlive() && other.getRole() != NpcRole.BANDIT)
                    .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            Player player = this.level().getNearestPlayer(this, 20.0D);
            if (player != null && (player.isCreative() || player.isSpectator())) player = null;
            if (civilian != null && (player == null || this.distanceToSqr(civilian) <= this.distanceToSqr(player))) {
                this.setTarget(civilian);
            } else if (player != null) {
                this.setTarget(player);
            } else {
                Monster monster = this.level().getEntitiesOfClass(Monster.class,
                        this.getBoundingBox().inflate(16.0D))
                        .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
                if (monster != null) this.setTarget(monster);
            }
        } else if (role == NpcRole.GUARD || role == NpcRole.WARRIOR) {
            CivilizationNpc bandit = this.level().getEntitiesOfClass(CivilizationNpc.class,
                    this.getBoundingBox().inflate(24.0D),
                    other -> other != this && other.isAlive() && other.getRole() == NpcRole.BANDIT)
                    .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if (bandit != null) this.setTarget(bandit);
        }
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
