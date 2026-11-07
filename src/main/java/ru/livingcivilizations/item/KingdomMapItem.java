package ru.livingcivilizations.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.livingcivilizations.kingdom.KingdomSavedData;

public class KingdomMapItem extends Item {
    public KingdomMapItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level.getServer() != null) {
            KingdomSavedData data = KingdomSavedData.get(level.getServer());
            var here = data.findAt(player.blockPosition().getX(), player.blockPosition().getZ());
            if (here == null) {
                player.displayClientMessage(Component.literal("На этой земле пока нет зарегистрированного королевства."), false);
            } else {
                player.displayClientMessage(Component.literal("Королевство " + here.name()
                        + " | столица: " + here.x() + ", " + here.z()
                        + " | радиус земель: " + here.radius()
                        + " блоков | войны: " + String.join(", ", here.wars())), false);
            }
            player.displayClientMessage(Component.literal("Открой чат и используй /civilizations map для списка всех королевств."), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
