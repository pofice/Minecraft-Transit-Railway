package org.mtr.mod.render;

import org.mtr.core.data.Rail;
import org.mtr.core.tools.Utilities;
import org.mtr.init.MTR;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.MinecraftClientHelper;
import org.mtr.mapping.mapper.PlayerHelper;
import org.mtr.mod.Init;
import org.mtr.mod.block.BlockNode;
import org.mtr.mod.block.BlockPlatform;
import org.mtr.mod.block.BlockSignalLightBase;
import org.mtr.mod.block.BlockSignalSemaphoreBase;
import org.mtr.mod.client.ClientData;
import org.mtr.mod.client.Config;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.data.IGui;
import org.mtr.mod.data.RailType;
import org.mtr.mod.item.ItemNodeModifierBase;

import javax.annotation.Nullable;

public class RenderRails implements IGui {

	public static void render(GraphicsHolder graphicsHolder) {
		final MinecraftClient minecraftClient = MinecraftClient.getInstance();
		final ClientWorld clientWorld = minecraftClient.getWorldMapped();
		final ClientPlayerEntity clientPlayerEntity = minecraftClient.getPlayerMapped();

		if (clientWorld == null || clientPlayerEntity == null) {
			return;
		}

		final boolean renderColors = isHoldingRailRelated(clientPlayerEntity);
		final int maxRailDistance = MinecraftClientHelper.getRenderDistance() * 16;

		ClientData.instance.positionToRailConnections.forEach((startPos, railMap) -> railMap.forEach((endPos, rail) -> {
			if (!Utilities.isBetween(clientPlayerEntity.getX(), startPos.getX(), endPos.getX(), maxRailDistance) || !Utilities.isBetween(clientPlayerEntity.getZ(), startPos.getZ(), endPos.getZ(), maxRailDistance)) {
				return;
			}

			switch (rail.getTransportMode()) {
				case TRAIN:
					renderRailStandard(clientWorld, rail, renderColors, 1);
					if (renderColors) {
//						renderSignalsStandard(clientWorld, matrices, vertexConsumers, rail, startPos, endPos);
					}
					break;
				case BOAT:
					if (renderColors) {
						renderRailStandard(clientWorld, rail, true, 0.5F);
//						renderSignalsStandard(clientWorld, matrices, vertexConsumers, rail, startPos, endPos);
					}
					break;
				case CABLE_CAR:
					if (rail.isPlatform() || rail.isSiding() || rail.getSpeedLimitKilometersPerHour() == RailType.CABLE_CAR_STATION.speedLimit) {
						renderRailStandard(clientWorld, rail, 0.25F + SMALL_OFFSET, renderColors, 0.25F, new Identifier(Init.MOD_ID, "textures/block/metal.png"), 0.25F, 0, 0.75F, 1);
					}
					if (renderColors && !rail.isPlatform() && !rail.isSiding()) {
						renderRailStandard(clientWorld, rail, 0.5F + SMALL_OFFSET, true, 1, null, 0, 0.75F, 1, 0.25F);
					}

					final Vector3d playerOffset = RenderTrains.getPlayerOffset();
					graphicsHolder.createVertexConsumer(RenderLayer.getLines());
					rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
						graphicsHolder.drawLineInWorld(
								(float) (x1 - playerOffset.getXMapped()),
								(float) (y1 - playerOffset.getYMapped() + 0.5),
								(float) (z1 - playerOffset.getZMapped()),
								(float) (x3 - playerOffset.getXMapped()),
								(float) (y2 - playerOffset.getYMapped() + 0.5),
								(float) (z3 - playerOffset.getZMapped()),
								renderColors ? RailType.getRailColor(rail) : ARGB_BLACK
						);
					}, 0, 0);

					break;
				case AIRPLANE:
					if (renderColors) {
						renderRailStandard(clientWorld, rail, true, 1);
//						renderSignalsStandard(clientWorld, matrices, vertexConsumers, rail, startPos, endPos);
					} else {
						renderRailStandard(clientWorld, rail, 0.0625F + SMALL_OFFSET, false, 0.25F, new Identifier("textures/block/iron_block.png"), 0.25F, 0, 0.75F, 1);
					}
					break;
			}
		}));
	}

	public static boolean isHoldingRailRelated(ClientPlayerEntity clientPlayerEntity) {
		return PlayerHelper.isHolding(new PlayerEntity(clientPlayerEntity.data),
				item -> item.data instanceof ItemNodeModifierBase ||
						Block.getBlockFromItem(item).data instanceof BlockSignalLightBase ||
						Block.getBlockFromItem(item).data instanceof BlockNode ||
						Block.getBlockFromItem(item).data instanceof BlockSignalSemaphoreBase ||
						Block.getBlockFromItem(item).data instanceof BlockPlatform
		);
	}

	private static void renderRailStandard(ClientWorld clientWorld, Rail rail, boolean renderColors, float railWidth) {
		renderRailStandard(clientWorld, rail, 0.065625F, renderColors, railWidth, renderColors && RailType.getRailColor(rail) == RailType.QUARTZ.color ? new Identifier(Init.MOD_ID, "textures/block/rail_preview.png") : new Identifier("textures/block/rail.png"), -1, -1, -1, -1);
	}

	private static void renderRailStandard(ClientWorld clientWorld, Rail rail, float yOffset, boolean renderColors, float railWidth, @Nullable Identifier texture, float u1, float v1, float u2, float v2) {
		final int maxRailDistance = MinecraftClientHelper.getRenderDistance() * 16;

		if (renderColors || texture != null) {
			rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
				final BlockPos pos2 = Init.newBlockPos(x1, y1, z1);
				if (RenderTrains.shouldNotRender(pos2, maxRailDistance, null)) {
					return;
				}

				if (renderColors && ClientData.instance.oneWayRails.contains(rail)) {
					RenderTrains.scheduleRender(new Identifier(Init.MOD_ID, "textures/block/one_way_rail_arrow.png"), false, RenderTrains.QueuedRenderLayer.EXTERIOR, graphicsHolder -> {
						final Vector3d playerOffset = RenderTrains.getPlayerOffset();
						IDrawing.drawTexture(graphicsHolder, x1, y1 + yOffset, z1, x2, y1 + yOffset + SMALL_OFFSET, z2, x3, y2 + yOffset, z3, x4, y2 + yOffset + SMALL_OFFSET, z4, playerOffset, 0, 0.75F, 1, 0.25F, Direction.UP, ARGB_WHITE, MAX_LIGHT_GLOWING);
						IDrawing.drawTexture(graphicsHolder, x2, y1 + yOffset + SMALL_OFFSET, z2, x1, y1 + yOffset, z1, x4, y2 + yOffset + SMALL_OFFSET, z4, x3, y2 + yOffset, z3, playerOffset, 0, 0.75F, 1, 0.25F, Direction.UP, ARGB_WHITE, MAX_LIGHT_GLOWING);
					});
				}

				if (texture != null) {
					final float textureOffset = (((int) (x1 + z1)) % 4) * 0.25F + (float) Config.trackTextureOffset() / Config.TRACK_OFFSET_COUNT;
					final int color = renderColors || !Config.hideSpecialRailColors() && (rail.isPlatform() || rail.isSiding()) ? RailType.getRailColor(rail) : ARGB_WHITE;
					final int light = renderColors ? MAX_LIGHT_GLOWING : LightmapTextureManager.pack(clientWorld.getLightLevel(LightType.getBlockMapped(), pos2), clientWorld.getLightLevel(LightType.getSkyMapped(), pos2));
					RenderTrains.scheduleRender(texture, false, RenderTrains.QueuedRenderLayer.EXTERIOR, graphicsHolder -> {
						final Vector3d playerOffset = RenderTrains.getPlayerOffset();
						IDrawing.drawTexture(graphicsHolder, x1, y1 + yOffset, z1, x2, y1 + yOffset + SMALL_OFFSET, z2, x3, y2 + yOffset, z3, x4, y2 + yOffset + SMALL_OFFSET, z4, playerOffset, u1 < 0 ? 0 : u1, v1 < 0 ? 0.1875F + textureOffset : v1, u2 < 0 ? 1 : u2, v2 < 0 ? 0.3125F + textureOffset : v2, Direction.UP, color, light);
						IDrawing.drawTexture(graphicsHolder, x2, y1 + yOffset + SMALL_OFFSET, z2, x1, y1 + yOffset, z1, x4, y2 + yOffset + SMALL_OFFSET, z4, x3, y2 + yOffset, z3, playerOffset, u1 < 0 ? 0 : u1, v1 < 0 ? 0.1875F + textureOffset : v1, u2 < 0 ? 1 : u2, v2 < 0 ? 0.3125F + textureOffset : v2, Direction.UP, color, light);
					});
				}
			}, -railWidth, railWidth);
		}
	}
}