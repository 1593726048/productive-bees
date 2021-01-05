package cy.jdkdigital.productivebees.container;

import cy.jdkdigital.productivebees.common.block.Centrifuge;
import cy.jdkdigital.productivebees.common.tileentity.HoneyGeneratorTileEntity;
import cy.jdkdigital.productivebees.common.tileentity.HoneyGeneratorTileEntity;
import cy.jdkdigital.productivebees.common.tileentity.InventoryHandlerHelper;
import cy.jdkdigital.productivebees.init.ModContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class HoneyGeneratorContainer extends AbstractContainer
{
    public final HoneyGeneratorTileEntity tileEntity;

    public final IWorldPosCallable canInteractWithCallable;

    public HoneyGeneratorContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    public HoneyGeneratorContainer(final int windowId, final PlayerInventory playerInventory, final HoneyGeneratorTileEntity tileEntity) {
        this(ModContainerTypes.CENTRIFUGE.get(), windowId, playerInventory, tileEntity);
    }

    public HoneyGeneratorContainer(@Nullable ContainerType<?> type, final int windowId, final PlayerInventory playerInventory, final HoneyGeneratorTileEntity tileEntity) {
        super(type, windowId);

        this.tileEntity = tileEntity;
        this.canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());

        trackIntArray(new IIntArray() {
            @Override
            public int get(int i) {
                return i == 0 ?
                        tileEntity.fluidId :
                        tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(fluidHandler -> fluidHandler.getFluidInTank(0).getAmount()).orElse(0);
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0:
                        tileEntity.fluidId = value;
                    case 1:
                        tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
                            FluidStack fluid = fluidHandler.getFluidInTank(0);
                            if (fluid.isEmpty()) {
                                fluidHandler.fill(new FluidStack(Registry.FLUID.getByValue(tileEntity.fluidId), value), IFluidHandler.FluidAction.EXECUTE);
                            } else {
                                fluid.setAmount(value);
                            }
                        });
                }
            }

            @Override
            public int size() {
                return 2;
            }
        });

        trackInt(new IntReferenceHolder()
        {
            @Override
            public int get() {
                return tileEntity.recipeProgress;
            }

            @Override
            public void set(int value) {
                tileEntity.recipeProgress = value;
            }
        });

        this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            // Comb and bottle slots
            addSlot(new ManualSlotItemHandler((InventoryHandlerHelper.ItemHandler) inv, InventoryHandlerHelper.BOTTLE_SLOT, 139, 17));
            addSlot(new ManualSlotItemHandler((InventoryHandlerHelper.ItemHandler) inv, InventoryHandlerHelper.INPUT_SLOT, 13, 35));

            // Inventory slots
            addSlotBox(inv, InventoryHandlerHelper.OUTPUT_SLOTS[0], 67, 17, 3, 18, 3, 18);
            addSlot(new ManualSlotItemHandler((InventoryHandlerHelper.ItemHandler) inv, InventoryHandlerHelper.FLUID_ITEM_OUTPUT_SLOT, 139, 53));
        });

        this.tileEntity.getUpgradeHandler().ifPresent(upgradeHandler -> {
            addSlotBox(upgradeHandler, 0, 165, 8, 1, 18, 4, 18);
        });

        layoutPlayerInventorySlots(playerInventory, 0, - 5, 84);
    }

    private static HoneyGeneratorTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if (tileAtPos instanceof HoneyGeneratorTileEntity) {
            return (HoneyGeneratorTileEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @Override
    public boolean canInteractWith(@Nonnull final PlayerEntity player) {
        return canInteractWithCallable.applyOrElse((world, pos) -> world.getBlockState(pos).getBlock() instanceof Centrifuge && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    protected TileEntity getTileEntity() {
        return tileEntity;
    }
}