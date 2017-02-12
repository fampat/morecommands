package com.mrnobody.morecommands.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedMethod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * A custom variant of minecraft's {@link net.minecraft.command.EntitySelector} class.<br>
 * Has more possibilities to select targets than minecraft's one.
 * 
 * @author MrNobody98
 */
public final class TargetSelector {
	private TargetSelector() {}
	
	/** This matches the at-tokens introduced for command blocks, including their arguments, if any. */
	private static final Pattern tokenPattern = Pattern.compile("^^@([pareb])(?:\\[([\\w=,!-]*)\\])?$");
	/** This matches things like "-1,,4", and is used for getting x,y,z,range from the token's argument list. */
	private static final Pattern intListPattern = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)");
	/** This matches things like "rm=4,c=2" and is used for handling named token arguments. */
    private static final Pattern keyValueListPattern = Pattern.compile("\\G(\\w+)=([-!]?[\\w-]*)(?:$|,)");
    /** Coordinate Specifiers */
    private static final ImmutableSet<String> coordSpecifiers = ImmutableSet.of("x", "y", "z", "dx", "dy", "dz", "rm", "r");
	
    /** the target selector types that select entities */
    private static final ImmutableSet<String> entityTargetTypes = ImmutableSet.of("p", "a", "r", "e");
    /** the target selector types that select blocks */
    private static final ImmutableSet<String> blockTargetTypes = ImmutableSet.of("b");
    /** a pattern to check whether a target selector has arguments */
    private static final Pattern isTargetSeletorWithArguments = Pattern.compile("^@[pareb]\\[$");
    
    /**
     * Creates an argument map from the given argument map.
     * The argument map is a {@link ListMultimap} that maps the argument name to several
     * values. This allows to target e.g. multiple entities instead of only one
     * 
     * @param argumentMapBuilder the argument map builder to which the arguments are added
     * @param arguments the argument string
     */
	private static void getArguments(ImmutableListMultimap.Builder<String, String> argumentMapBuilder, String arguments) {
		if (arguments == null) return;
		
		Iterator<String> coords = Arrays.asList("x", "y", "z", "r").iterator();
        int coordEnd = -1;

        for (Matcher coordMatcher = intListPattern.matcher(arguments); coordMatcher.find(); coordEnd = coordMatcher.end()) {
        	String coord = coords.hasNext() ? coords.next() : null;

        	if (coord != null && coordMatcher.group(1).length() > 0)
        		argumentMapBuilder.put(coord, coordMatcher.group(1));
        }

        if (coordEnd < arguments.length()) {
        	Matcher keyValueMatcher = keyValueListPattern.matcher(coordEnd == -1 ? arguments : arguments.substring(coordEnd));
        	ListMultimap<String, String> tracker = ArrayListMultimap.create();
        	
        	while (keyValueMatcher.find()) {
        		String key = keyValueMatcher.group(1), value = keyValueMatcher.group(2);
        		
        		if (!tracker.containsEntry(key, value)) {
        			argumentMapBuilder.put(key, value);
        			tracker.put(key, value);
        		}
        	}
        }
    }
	
	/**
	 * Gets the target coordinate from the argument map
	 * 
	 * @param argumentMap the argument map
	 * @param default_ the default target coordinate
	 * @return the target coordinate
	 */
	private static BlockPos getCoordinate(ListMultimap<String, String> argumentMap, BlockPos default_) {
		return new BlockPos(
				getIntWithDefault(argumentMap, "x", default_.getX()), 
				getIntWithDefault(argumentMap, "y", default_.getY()), 
				getIntWithDefault(argumentMap, "z", default_.getZ()));
	}
	
	/**
	 * Checks if the argument map contains any target coordinates (x and/or y and/or z)
	 * 
	 * @param argumentMap the argument map
	 * @return whether the argument map contains any target coordinate
	 */
	private static boolean containsCoordinates(ListMultimap<String, String> argumentMap) {
        Iterator<String> iterator = coordSpecifiers.iterator();
        String coordSpecifier;

        do {
        	if (!iterator.hasNext()) return false;
        	coordSpecifier = iterator.next();
        }
        while (!argumentMap.containsKey(coordSpecifier));

        return true;
	}
    
	/**
	 * Gets an integer from the argument map. Uses always the first value of the list that is mapped to a key
	 * 
	 * @param argumentMap the argument map
	 * @param key the key to get the integer from
	 * @param default_ the default value (e.g. if the map does not contain the key or the key is not mapped to an integer)
	 * @return the integer value
	 */
	private static int getIntWithDefault(ListMultimap<String, String> argumentMap, String key, int default_) {
		return argumentMap.containsKey(key) ? 
				MathHelper.parseIntWithDefault(argumentMap.get(key).get(0), default_)
				: default_;
	}
    
	/**
	 * Extracts the nbt properties from a target selector. They can't be matched with regexes because
	 * regex can't handle nested structures like nbt data that is represented by json strings.
	 * 
	 * @param selector the target selector from which to extract nbt data
	 * @return A {@link Pair} whose left value is the new target selector string and whose right
	 *         right value is a list of nbt properties
	 */
	private static ImmutablePair<String, List<String>> extractNBTProperties(String selector) {
		if (AbstractCommand.isTargetSelectorWithArguments(selector) && selector.contains("nbt=")) {
			String[] split = selector.split("nbt=");
			StringBuilder newSelector = new StringBuilder(split[0]);
			String nbt, after; List<String> nbtArgs = new ArrayList<String>();
			
			for (int index = 1; index < split.length; index++) {
				nbt = split[index]; int openCount = 0, splitIndex = 0;
				
				for (char ch : nbt.toCharArray()) {
					if (ch == '{' || ch == '[') openCount++;
					else if (ch == '}' || ch == ']') openCount--;
					
					if ((ch == ']' && openCount == -1) || (ch == ',' && openCount == 0)) break;
					splitIndex++;
				}
				
				after = nbt.substring(splitIndex);
				nbt = nbt.substring(0, splitIndex);
				
				if (after.charAt(0) == ']' && newSelector.charAt(newSelector.length() - 1) == ',') 
					newSelector.deleteCharAt(newSelector.length() - 1);
				
				newSelector.append(after.charAt(0) == ',' ? after.substring(1) : after);
				nbtArgs.add(nbt);
			}
			
			return ImmutablePair.of(newSelector.toString(), nbtArgs);
		}
		return ImmutablePair.of(selector, null);
	}
    
	/**
	 * This class finds all blocks specified by a block target selector
	 * 
	 * @author MrNobody98
	 */
	public static final class BlockSelector {
		/**
		 * As it would be an overkill to return a list of tile entities and a list of coordinates 
		 * (which are actually only wrapper objects for three integers) that can become quite huge,
		 * e.g. if the player specifies a 100x100x100 area (which would be 1000000 blocks if there is no
		 * other filter), a callback is used. This reduces the performance that is needed drastically as
		 * there is no need to create thousands of wrapper objects (although that's one aspect for which
		 * the virtual machine is heavily optimized) and a list that needs to be resized
		 * continuously only for a temporary purpose.
		 * 
		 * @author MrNobody98
		 */
		public static interface BlockCallback {
			/**
			 * This method will be invoked when a block matches the given specifications
			 * 
			 * @param world the world of the block
			 * @param x the x coordinate of the block
			 * @param y the y coordinate of the block
			 * @param z the z coordinate of the block
			 */
			void applyToCoordinate(World world, BlockPos pos);
			
			/**
			 * This method will be invoked when a tile entity matches the given specifications
			 * 
			 * @param entity the tile entity
			 */
			void applyToTileEntity(TileEntity entity);
		}
		
		/**
		 * Matches blocks that are specified by a block target selector
		 * 
		 * @param sender the command sender who sent the command with the target selector
		 * @param selector the actual target selector
		 * @param matchTileEntites whether to match tile entities
		 * @param callback the callback that will be invoked when a match occurs
		 */
		public static void matchBlocks(ICommandSender sender, String selector, boolean matchTileEntites, BlockCallback callback) {
			ImmutableListMultimap.Builder<String, String> argumentMapBuilder = ImmutableListMultimap.builder();
			ImmutablePair<String, List<String>> pair = extractNBTProperties(selector);
			if (pair.getRight() != null) argumentMapBuilder.putAll("nbt", pair.getRight());
			selector = pair.getLeft();
			
			Matcher tokenMatcher = tokenPattern.matcher(selector);
			
			if (!tokenMatcher.matches()) return;
			String targetType = tokenMatcher.group(1);
			if (!blockTargetTypes.contains(targetType)) return;
			
			getArguments(argumentMapBuilder, tokenMatcher.group(2));
			ListMultimap<String, String> argumentMap = argumentMapBuilder.build();
			if (!containsCoordinates(argumentMap)) return;
			
			BlockPos coordinate = getCoordinate(argumentMap, sender.getPosition());
			World world = sender.getEntityWorld();
			if (world == null) return;

			Predicate<TileEntity> nbt = getNBTPredicate(argumentMap, sender);
        	Predicate<Block> blocks = getBlocksPredicate(argumentMap);
        	Predicate<Integer> metas = getMetaPredicate(argumentMap);
        	Predicate<BlockPos> radius = getBlockRadiusPredicate(argumentMap, coordinate);
        	AxisAlignedBB aabb = getAABB(argumentMap, coordinate);
        	
        	final int x1 = (int) Math.min(aabb.minX, aabb.maxX), x2 = (int) Math.max(aabb.minX, aabb.maxX);
        	final int y1 = (int) Math.min(aabb.minY, aabb.maxY), y2 = (int) Math.max(aabb.minY, aabb.maxY);
        	final int z1 = (int) Math.min(aabb.minZ, aabb.maxZ), z2 = (int) Math.max(aabb.minZ, aabb.maxZ);
        	
        	for (int x = x1; x < x2; x++) {
        		for (int y = y1; y < y2; y++) {
        			for (int z = z1; z < z2; z++) {
        				BlockPos pos = new BlockPos(x, y, z);
        				
                		if (world.isBlockLoaded(pos)) {
                			IBlockState state = world.getBlockState(pos);
                			if (state == null) continue;
                			Block block = state.getBlock();
                			int meta = block.getMetaFromState(state);
                			
                			if ((blocks == null || blocks.apply(block)) && (metas == null || metas.apply(meta)) && 
                				(radius == null || radius.apply(pos))) {
                				
                				if (block.hasTileEntity(state)) {
                					TileEntity te = world.getTileEntity(pos);
                					if (te == null || te.isInvalid() || (nbt != null && !nbt.apply(te))) continue;
                					
                					callback.applyToCoordinate(world, pos);
                					if (matchTileEntites) callback.applyToTileEntity(te);
                				}
                				else callback.applyToCoordinate(world, pos);
                			}
                		}
                	}
            	}
        	}
		}
		
		/**
		 * Returns a {@link Predicate} that filters tile entities by their nbt data
		 * 
		 * @param argumentMap the argument map
		 * @param sender the command sender
		 * @return the predicate that filters tile entities by their nbt data
		 */
		private static Predicate<TileEntity> getNBTPredicate(ListMultimap<String, String> argumentMap, ICommandSender sender) {
			final boolean equalLists = argumentMap.containsKey("nbtm") && argumentMap.get("nbtm").get(0).equalsIgnoreCase("EQUAL");
			List<String> nbtData = argumentMap.get("nbt");
	        
			if (nbtData != null && !nbtData.isEmpty()) {
				final List<NBTBase> allowedNbt = Lists.<NBTBase>newArrayList();
				final List<NBTBase> disallowedNbt = Lists.<NBTBase>newArrayList();
				
				for (String nbt : nbtData) {
					if (nbt == null) continue; NBTBase tag = AbstractCommand.getNBTFromParam(nbt.startsWith("!") ? nbt.substring(1) : nbt);
					if (tag == null) continue;
					
					if (nbt.startsWith("!")) disallowedNbt.add(tag);
					else allowedNbt.add(tag);
				}
				
				return new Predicate<TileEntity>() {
					@Override public boolean apply(TileEntity te) {
						NBTTagCompound compound = new NBTTagCompound(); te.writeToNBT(compound);
						boolean matchesAllowed = false; String id = null, name = compound.getString("id");
						
						for (NBTBase nbt : allowedNbt) {
							if (nbt instanceof NBTTagCompound && (((NBTTagCompound) nbt).hasKey("id", NBT.TAG_STRING))) 
								id = ((NBTTagCompound) nbt).getString("id");
							
							if (id != null) {nbt = nbt.copy(); ((NBTTagCompound) nbt).removeTag("id");}
							
							if (id != null && (!id.equalsIgnoreCase(name) || Block.getBlockFromName(id) != te.getBlockType())) {matchesAllowed = true; break;}
							else if (id == null || id.equalsIgnoreCase(name) || Block.getBlockFromName(id) == te.getBlockType())
								if (nbtContains(compound, nbt, !equalLists)) {matchesAllowed = true; break;}
							
							id = null;
						}
						
						if (!matchesAllowed && !disallowedNbt.isEmpty()) {
							boolean matchesDisallowed = false; id = null;
							
							for (NBTBase nbt : disallowedNbt) {
								if (nbt instanceof NBTTagCompound && (((NBTTagCompound) nbt).hasKey("id", NBT.TAG_STRING))) 
									id = ((NBTTagCompound) nbt).getString("id");
								
								if (id != null) {nbt = nbt.copy(); ((NBTTagCompound) nbt).removeTag("id");}
								
								if (id != null && (!id.equalsIgnoreCase(name) || Block.getBlockFromName(id) != te.getBlockType())) {matchesAllowed = true; break;}
								else if (id == null || id.equalsIgnoreCase(name) || Block.getBlockFromName(id) == te.getBlockType())
									if (nbtContains(compound, nbt, !equalLists)) {matchesDisallowed = true; break;}
								
								id = null;
							}
							
							return !matchesDisallowed;
						}
						else return matchesAllowed;
					}
				};
			}
			
			return null;
		}
		
		/**
		 * Gets the {@link AxisAlignedBB} that represents the bounding box in which the matching blocks
		 * should be searched
		 * 
		 * @param argumentMap the argument map
		 * @param coord the anchor or middle point (if a radius is used) coordinate of the returned bounding box
		 * @return the bounding box representing the area of block that should be checked
		 */
		private static AxisAlignedBB getAABB(ListMultimap<String, String> argumentMap, final BlockPos coord) {
	        int dx = getIntWithDefault(argumentMap, "dx", 0);
	        int dy = getIntWithDefault(argumentMap, "dy", 0);
	        int dz = getIntWithDefault(argumentMap, "dz", 0);
	        int radius = getIntWithDefault(argumentMap, "r", -1);
	        AxisAlignedBB aabb;
	        
	        if (!argumentMap.containsKey("dx") && !argumentMap.containsKey("dy") && !argumentMap.containsKey("dz"))
	        	aabb = new AxisAlignedBB(coord.getX() - radius, coord.getY() - radius, coord.getZ() - radius, coord.getX() + radius + 1, coord.getY() + radius + 1, coord.getZ() + radius + 1);
	        else
	        	aabb = getAABB(coord, dx, dy, dz);
	        
	        return aabb;
		}
		
        /**
         * Creates a bounding box
         * 
         * @param coord the anchor coordinate of the bounding box
         * @param dx the x length
         * @param dy the y length
         * @param dz the z length
         * @return the bounding box
         */
		private static AxisAlignedBB getAABB(BlockPos coord, int dx, int dy, int dz) {
			boolean dxNeg = dx < 0;
			boolean dyNeg = dy < 0;
			boolean dzNeg = dz < 0;
			int minX = coord.getX() + (dxNeg ? dx : 0);
			int minY = coord.getY() + (dyNeg ? dy : 0);
			int minZ = coord.getZ() + (dzNeg ? dz : 0);
			int maxX = coord.getX() + (dxNeg ? 0 : dx) + 1;
			int maxY = coord.getY() + (dyNeg ? 0 : dy) + 1;
			int maxZ = coord.getZ() + (dzNeg ? 0 : dz) + 1;
			return new AxisAlignedBB((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ);
		}
		
		/**
		 * Creates a predicate that accepts only coordinates that have a certain distance from another coordinate
		 * 
		 * @param argumentMap the argument map
		 * @param coord the coordinate to which the distance is to be measureed
		 * @return the predicate
		 */
		private static Predicate<BlockPos> getBlockRadiusPredicate(ListMultimap<String, String> argumentMap, final BlockPos coord) {
			final int radiusMin = getIntWithDefault(argumentMap, "rm", -1);
	        final int radiusMax = getIntWithDefault(argumentMap, "r", -1);

	        if (coord != null && (radiusMin >= 0 || radiusMax >= 0)) {
	        	final int rmSquared = radiusMin * radiusMin;
	        	final int rSquared = radiusMax * radiusMax;
	        	
	        	return new Predicate<BlockPos>() {
	        		public boolean apply(BlockPos coordinate) {
	        			int distance = (int) getDistanceSqToCenter(coordinate, coord);
	        			return (radiusMin < 0 || distance >= rmSquared) && (radiusMax < 0 || distance <= rSquared);
	        		}
	        	};
	        }
	        
	        return null;
		}
		
		/**
		 * Gets the squared distance between two coordinates
		 * 
		 * @param coord coordinate 1
		 * @param center coordinate 2
		 * @return the squared distance
		 */
		private static double getDistanceSqToCenter(BlockPos coord, BlockPos center) {
			double d3 = center.getX() + 0.5D - coord.getX();
			double d4 = center.getY() + 0.5D - coord.getY();
			double d5 = center.getZ() + 0.5D - coord.getZ();
			return d3 * d3 + d4 * d4 + d5 * d5;
		}
		
		/**
		 * Returns a predicate that accepts only certain metadata
		 * 
		 * @param argumentMap the argument map
		 * @return the predicate
		 */
		private static Predicate<Integer> getMetaPredicate(ListMultimap<String, String> argumentMap) {
			List<String> meta = argumentMap.get("meta");
			final List<Integer> metas = Lists.<Integer>newArrayList();
			if (meta == null || meta.isEmpty()) return null;
			
			for (String m : meta) {
				try {int i = Integer.parseInt(m); if (i >= 0) metas.add(i);}
				catch (NumberFormatException nfe) {}
			}
			
			return new Predicate<Integer>() {
				@Override public boolean apply(Integer meta) {
					return metas.contains(meta);
				}
			};
		}
		
		/**
		 * Returns a predicate that accepts only certain blocks
		 * 
		 * @param argumentMap the argument map
		 * @return the predicate
		 */
		private static Predicate<Block> getBlocksPredicate(ListMultimap<String, String> argumentMap) {
			List<String> ids = argumentMap.get("id");
			final List<Block> blocks = Lists.<Block>newArrayList();
			if (ids == null || ids.isEmpty()) return null;
			
			for (String id : ids) {
				Block b = Block.getBlockFromName(id);
				if (b != null) blocks.add(b); 
			}
			
			return new Predicate<Block>() {
				@Override public boolean apply(Block block) {
					return blocks.contains(block);
				}
			};
		}
	}
	
	/**
	 * This class finds all entities specified by an entity target selector
	 * 
	 * @author MrNobody98
	 */
	public static class EntitySelector {
		/**
		 * Matches entities which are specified by an entity target selector
		 * 
		 * @param sender the command sender who sent the command with the target selector
		 * @param selector the entity target selector
		 * @param entityClass the required entity super class
		 * @return all matched entities
		 */
		public static <T extends Entity> List<? extends T> matchEntites(ICommandSender sender, String selector, Class<T> entityClass) {
			ImmutableListMultimap.Builder<String, String> argumentMapBuilder = ImmutableListMultimap.builder();
			ImmutablePair<String, List<String>> pair = extractNBTProperties(selector);
			if (pair.getRight() != null) argumentMapBuilder.putAll("nbt", pair.getRight());
			selector = pair.getLeft();
			
			Matcher tokenMatcher = tokenPattern.matcher(selector);
			
			if (!tokenMatcher.matches())
				return Collections.<T>emptyList();
			
			String targetType = tokenMatcher.group(1);
			
			if (!entityTargetTypes.contains(targetType))
				return Collections.<T>emptyList();
			
			getArguments(argumentMapBuilder, tokenMatcher.group(2));
			ListMultimap<String, String> argumentMap = argumentMapBuilder.build();
			
			if (!isValidType(sender, argumentMap))
				return Collections.<T>emptyList();
			
			BlockPos coordinate = getCoordinate(argumentMap, sender.getPosition());
			Iterator<World> worlds = getWorlds(sender, argumentMap).iterator();
			List<T> entities = Lists.<T>newArrayList();
            
			ImmutableList.Builder<Predicate<Entity>> predicateBuilder = ImmutableList.builder();
        	getEntityTypePredicates(argumentMap, targetType, predicateBuilder);
        	getEntityExperiencePredicates(argumentMap, predicateBuilder);
        	getEntityGamemodePredicates(argumentMap, predicateBuilder);
        	getEntityTeamPredicates(argumentMap, predicateBuilder);
        	getEntityScorePredicates(sender.getServer() == null ? FMLCommonHandler.instance().getMinecraftServerInstance() : sender.getServer(), argumentMap, predicateBuilder);
        	getEntityNamePredicates(argumentMap, predicateBuilder);
        	getEntityTagPredicates(argumentMap, predicateBuilder);
        	getEntityRadiusPredicates(argumentMap, coordinate, predicateBuilder);
        	getEntityLookPredicates(argumentMap, predicateBuilder);
        	getEntityNBTPredicates(argumentMap, predicateBuilder);
			
			while (worlds.hasNext()) {
                World world = worlds.next();

                if (world != null)
                	entities.addAll(getEntities(argumentMap, entityClass, predicateBuilder.build(), targetType, world, coordinate));
			}

			return finalFilter(entities, argumentMap, sender, entityClass, targetType, coordinate);
		}
		
		/**
		 * Gets the target worlds of the target selector
		 * 
		 * @param sender the command sender
		 * @param argumentMap the argument map
		 * @return the set of target worlds
		 */
		private static Set<World> getWorlds(ICommandSender sender, ListMultimap<String, String> argumentMap) {
			Set<World> worlds = Sets.<World>newHashSet();
			
			if (containsCoordinates(argumentMap)) 
				worlds.add(sender.getEntityWorld());
			else if (!argumentMap.containsKey("dim")) 
				worlds.addAll(Arrays.asList(sender.getServer().worldServers));
			else {
				Map<String, World> dims = Maps.newHashMapWithExpectedSize(sender.getServer().worldServers.length);
				for (World world : sender.getServer().worldServers) dims.put(world.provider.getDimensionType().getName(), world);
				
				dims.put("0", sender.getServer().worldServerForDimension(0));
				dims.put("-1", sender.getServer().worldServerForDimension(-1));
				dims.put("1", sender.getServer().worldServerForDimension(1));
				
				dims.put("Surface", sender.getServer().worldServerForDimension(0));
				dims.put("Nether", sender.getServer().worldServerForDimension(-1));
				dims.put("End", sender.getServer().worldServerForDimension(1));
				
				for (String dim : argumentMap.get("dim")) 
					if (dims.containsKey(dim)) worlds.add(dims.get(dim));
			}

			return worlds;
		}
		
		/**
		 * Applies the last filter to the list of entities that was previously filtered by some predicates.
		 * 
		 * @param entities the pre-filtered list of entities
		 * @param argumentMap the argument map
		 * @param sender the command sender
		 * @param entityClass the required entity superclass
		 * @param targetType the target type (one of {@link TargetSelector#entityTargetTypes})
		 * @param coord the source coordinate (middle point of a radius, achor of a bounding box, etc.)
		 * @return the final list of filtered entities
		 */
		private static <T extends Entity> List<? extends T> finalFilter(List<T> entities, ListMultimap<String, String> argumentMap, ICommandSender sender, Class<T> entityClass, String targetType, final BlockPos coord) {
	        int maxEntities = getIntWithDefault(argumentMap, "c", !targetType.equals("a") && !targetType.equals("e") ? 1 : 0);
	        
	        if (targetType.equals("r"))
	        	Collections.shuffle(entities);
	        else if (coord != null) {
	        	Collections.sort(entities, new Comparator<T>() {
	        		public int compare(T p_compare_1_, T p_compare_2_) {
	        			return ComparisonChain.start().compare(p_compare_1_.getDistanceSq(coord), p_compare_2_.getDistanceSq(coord)).result();
	        		}
	        	});
	        }

	        Entity entity = sender.getCommandSenderEntity();

	        if (entity != null && entityClass.isAssignableFrom(entity.getClass()) && maxEntities == 1 && entities.contains(entity) && !"r".equals(targetType))
	            entities = Lists.<T>newArrayList((T) entity);

	        if (maxEntities != 0) {
	        	if (maxEntities < 0) Collections.reverse(entities);
	        	entities = entities.subList(0, Math.min(Math.abs(maxEntities), entities.size()));
	        }

	        return entities;
	    }
		
		/**
		 * Gets the entities that match the given predicates from the given world
		 * 
		 * @param argumentMap the argument map
		 * @param entityClass the required entity superclass
		 * @param predicates the entity filter predicates
		 * @param targetType the target type (one of {@link TargetSelector#entityTargetTypes})
		 * @param world the world from which to get the entities
		 * @param coord the source coordinate (middle point of a radius, anchor of a bounding box, etc.)
		 * @return the filtered entity list
		 */
		private static <T extends Entity> List<? extends T> getEntities(ListMultimap<String, String> argumentMap, Class<T> entityClass, List<Predicate<Entity>> predicates, String targetType, World world, BlockPos coord) {
			final Predicate<Entity> isEntityAlive = new Predicate<Entity>() {
				public boolean apply(Entity entity) {
					return entity.isEntityAlive();
				}
			};
	    	
	        List<T> entities = Lists.<T>newArrayList();
	        boolean targetIsPlayer = !targetType.equals("e");
	        boolean randomTargetSpecified = targetType.equals("r") && argumentMap.containsKey("type");
	        int dx = getIntWithDefault(argumentMap, "dx", 0);
	        int dy = getIntWithDefault(argumentMap, "dy", 0);
	        int dz = getIntWithDefault(argumentMap, "dz", 0);
	        int radius = getIntWithDefault(argumentMap, "r", -1);
	        Predicate<Entity> entityPredicates = Predicates.<Entity>and(predicates);
	        Predicate<Entity> entityAlivePredicates = Predicates.<Entity>and(isEntityAlive, entityPredicates);

            int playerCount = world.playerEntities.size();
            int entityCount = world.loadedEntityList.size();
            boolean lessPlayers = playerCount < entityCount / 16;
            final AxisAlignedBB aabb;

            if (!argumentMap.containsKey("dx") && !argumentMap.containsKey("dy") && !argumentMap.containsKey("dz")) {
                if (radius >= 0) {
                    aabb = new AxisAlignedBB((double) (coord.getX() - radius), (double) (coord.getY() - radius), (double) (coord.getZ() - radius), (double) (coord.getX() + radius + 1), (double) (coord.getY() + radius + 1), (double) (coord.getZ() + radius + 1));

                    if (targetIsPlayer && lessPlayers && !randomTargetSpecified)
                    	entities.addAll(world.getPlayers(entityClass, entityAlivePredicates));
                    else
                    	entities.addAll(world.getEntitiesWithinAABB(entityClass, aabb, entityAlivePredicates));
                }
                else if (targetType.equals("a"))
                	entities.addAll(world.getPlayers(entityClass, entityPredicates));
                else if (!targetType.equals("p") && (!targetType.equals("r") || randomTargetSpecified))
                	entities.addAll(world.getEntities(entityClass, entityAlivePredicates));
                else
                	entities.addAll(world.getPlayers(entityClass, entityAlivePredicates));
            }
            else {
                aabb = getAABB(coord, dx, dy, dz);

                if (targetIsPlayer && lessPlayers && !randomTargetSpecified) {
                	Predicate<Entity> entityInAABB = new Predicate<Entity>() {
                		@Override public boolean apply(Entity entity) {
                			return entity != null && aabb.intersectsWith(entity.getEntityBoundingBox());
                		}
                	};
                	entities.addAll(world.getPlayers(entityClass, Predicates.and(entityAlivePredicates, entityInAABB)));
                }
                else
                	entities.addAll(world.getEntitiesWithinAABB(entityClass, aabb, entityAlivePredicates));
            }

	        return entities;
		}
		
		/**
		 * Creates a bounding box
		 * 
		 * @param coord the anchor coordinate of the bounding box
		 * @param dx the x length
		 * @param dy the y length
		 * @param dz the z length
		 * @return the bounding box
		 */
		private static AxisAlignedBB getAABB(BlockPos coord, int dx, int dy, int dz) {
			boolean dxNeg = dx < 0;
			boolean dyNeg = dy < 0;
			boolean dzNeg = dz < 0;
			int minX = coord.getX() + (dxNeg ? dx : 0);
			int minY = coord.getY() + (dyNeg ? dy : 0);
			int minZ = coord.getZ() + (dzNeg ? dz : 0);
			int maxX = coord.getX() + (dxNeg ? 0 : dx) + 1;
			int maxY = coord.getY() + (dyNeg ? 0 : dy) + 1;
			int maxZ = coord.getZ() + (dzNeg ? 0 : dz) + 1;
			return new AxisAlignedBB((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ);
		}
		
		/**
		 * Creates predicates which accept only entities which have certain nbt data
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityNBTPredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			final boolean equalLists = argumentMap.containsKey("nbtm") && argumentMap.get("nbtm").get(0).equalsIgnoreCase("EQUAL");
			List<String> nbtData = argumentMap.get("nbt");
	        
			if (nbtData != null && !nbtData.isEmpty()) {
				final List<NBTBase> allowedNbt = Lists.<NBTBase>newArrayList();
				final List<NBTBase> disallowedNbt = Lists.<NBTBase>newArrayList();
				
				for (String nbt : nbtData) {
					if (nbt == null) continue; NBTBase tag = AbstractCommand.getNBTFromParam(nbt.startsWith("!") ? nbt.substring(1) : nbt);
					if (tag == null) continue;
					
					if (nbt.startsWith("!")) disallowedNbt.add(tag);
					else allowedNbt.add(tag);
				}
				
				predicateBuilder.add(new Predicate<Entity>() {
					@Override public boolean apply(Entity entity) {
						NBTTagCompound compound = new NBTTagCompound(); entity.writeToNBT(compound);
						compound.setString("id", getEntityNameString(entity));
						
						for (NBTBase nbt : allowedNbt)
							if (nbtContains(compound, nbt, !equalLists)) return true;
						
						for (NBTBase nbt : disallowedNbt)
							if (!nbtContains(compound, nbt, !equalLists)) return true;
						
						return false;
					}
				});
			}
		}
		
		
		/**
		 * Creates predicates which accept only entities which have a certain look (yaw and pitch)
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityLookPredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			if (argumentMap.containsKey("rym") || argumentMap.containsKey("ry")) {
	            final int minYaw = trimAngle(getIntWithDefault(argumentMap, "rym", 0));
	            final int maxYaw = trimAngle(getIntWithDefault(argumentMap, "ry", 359));
	            
	            predicateBuilder.add(new Predicate<Entity>() {
	            	public boolean apply(Entity entity) {
	            		int yaw = trimAngle((int) Math.floor((double) entity.rotationYaw));
	            		return minYaw > maxYaw ? yaw >= minYaw || yaw <= maxYaw : yaw >= minYaw && yaw <= maxYaw;
	            	}
	            });
			}

			if (argumentMap.containsKey("rxm") || argumentMap.containsKey("rx")) {
	            final int minPitch = trimAngle(getIntWithDefault(argumentMap, "rxm", 0));
	            final int maxPitch = trimAngle(getIntWithDefault(argumentMap, "rx", 359));
	            
	            predicateBuilder.add(new Predicate<Entity>() {
	            	public boolean apply(Entity entity) {
	            		int pitch = trimAngle((int) Math.floor((double) entity.rotationPitch));
	            		return minPitch > maxPitch ? pitch >= minPitch || pitch <= maxPitch : pitch >= minPitch && pitch <= maxPitch;
	            	}
	            });
			}
		}
		
		/**
		 * Trims an angle to be between 0 and 359 degrees
		 * 
		 * @param angle the angle
		 * @return the trimmed angle
		 */
		private static int trimAngle(int angle) {
			angle %= 360;

			if (angle >= 160) angle -= 360;
			if (angle < 0) angle += 360;

	        return angle;
	    }
		
		/**
		 * Creates predicates which accept only entities which have a certain distance to a point
		 * 
		 * @param argumentMap the argument map
		 * @param coord the point to which the distance should be measured
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityRadiusPredicates(ListMultimap<String, String> argumentMap, final BlockPos coord, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			final int radiusMin = getIntWithDefault(argumentMap, "rm", -1);
	        final int radiusMax = getIntWithDefault(argumentMap, "r", -1);

	        if (coord != null && (radiusMin >= 0 || radiusMax >= 0)) {
	        	final int rmSquared = radiusMin * radiusMin;
	        	final int rSquared = radiusMax * radiusMax;
	        	
	        	predicateBuilder.add(new Predicate<Entity>() {
	        		public boolean apply(Entity entity) {
	        			int distance = (int) getDistanceSqToCenter(entity, coord);
	        			return (radiusMin < 0 || distance >= rmSquared) && (radiusMax < 0 || distance <= rSquared);
	        		}
	        	});
	        }
		}
		
		/**
		 * Gets the squared distance between an entity and a coordinate
		 * 
		 * @param entity the entity
		 * @param coorda the coordinate
		 * @return the squared distance
		 */
		private static double getDistanceSqToCenter(Entity entity, BlockPos coord) {
			double d3 = coord.getX() + 0.5D - entity.posX;
			double d4 = coord.getY() + 0.5D - entity.posY;
			double d5 = coord.getZ() + 0.5D - entity.posZ;
			return d3 * d3 + d4 * d4 + d5 * d5;
		}
		
		/**
		 * Creates predicates which accept only entities which have a certain name
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityNamePredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			List<String> names = argumentMap.get("name");
	        
			if (names != null && !names.isEmpty()) {
				final List<String> allowedNames = Lists.<String>newArrayList();
				final List<String> disallowedNames = Lists.<String>newArrayList();
				
				for (String name : names) {
					if (name == null) continue;
					if (name.startsWith("!")) disallowedNames.add(name.substring(1));
					else allowedNames.add(name);
				}
				
				predicateBuilder.add(new Predicate<Entity>() {
					@Override public boolean apply(Entity entity) {
							String name = entity.getName();
							return allowedNames.contains(name) || (!disallowedNames.isEmpty() && !disallowedNames.contains(name));
					}
				});
			}
	    }
		
		/**
		 * Creates predicates which accept only entities which have certain tags
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityTagPredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			List<String> tags = argumentMap.get("tag");
			
			if (tags != null && !tags.isEmpty()) {
				final List<String> allowedTags = Lists.<String>newArrayList();
				final List<String> disallowedTags = Lists.<String>newArrayList();
				
				for (String tag : tags) {
					if (tag == null) continue;
					if (tag.startsWith("!")) disallowedTags.add(tag.substring(1));
					else allowedTags.add(tag);
				}
				
				predicateBuilder.add(new Predicate<Entity>() {
					@Override public boolean apply(Entity entity) {
						boolean containsAllowed = entity.getTags().containsAll(allowedTags);
						boolean containsDisallowed = false; 
						for (String disallowed : disallowedTags) 
							if (entity.getTags().contains(disallowed)) {containsDisallowed = true; break;}
						
						return containsAllowed && !containsDisallowed;
					}
				});
			}
		}
		
		/**
		 * Creates predicates which accept only entities which have a maximum or minimum score
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityScorePredicates(final MinecraftServer server, ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
	        final Map<String, Integer> scores = getScores(argumentMap);

	        if (scores != null && !scores.isEmpty()) {
	        	predicateBuilder.add(new Predicate<Entity>() {
	                public boolean apply(Entity entity) {
	                	Scoreboard scoreboard = server.worldServerForDimension(0).getScoreboard();
	                	Iterator<Entry<String, Integer>> iterator = scores.entrySet().iterator();
	                	Entry<String, Integer> entry;
	                	boolean isMin; int score;

	                	do {
	                        if (!iterator.hasNext()) return true;

	                        entry = iterator.next();
	                        String key = entry.getKey();
	                        isMin = false;

	                        if (key.endsWith("_min") && key.length() > 4) {
	                        	isMin = true;
	                        	key = key.substring(0, key.length() - 4);
	                        }

	                        ScoreObjective scoreobjective = scoreboard.getObjective(key);
	                        if (scoreobjective == null) return false;

	                        String entityName = entity instanceof EntityPlayerMP ? entity.getName() : entity.getUniqueID().toString();
	                        if (!scoreboard.entityHasObjective(entityName, scoreobjective)) return false;

	                        score = scoreboard.getOrCreateScore(entityName, scoreobjective).getScorePoints();
	                        if (score < entry.getValue() && isMin) return false;
	                    }
	                    while (score <= entry.getValue() || isMin);

	                    return false;
	                }
	            });
	        }
	    }
		
		/**
		 * Gets the entity score arguments from the argument map
		 * 
		 * @param argumentMap the argument map
		 * @return the team<->score map
		 */
		private static Map<String, Integer> getScores(ListMultimap<String, String> argumentMap) {
	        Map<String, Integer> scores = Maps.<String, Integer>newHashMap();
	        Iterator<String> iterator = argumentMap.keySet().iterator();

	        while (iterator.hasNext()) {
	        	String key = iterator.next();

	        	if (key.startsWith("score_") && key.length() > "score_".length())
	        		scores.put(key.substring("score_".length()), getIntWithDefault(argumentMap, key, 1));
	        }

	        return scores;
	    }
		
		/**
		 * Creates predicates which accept only entities which are in a certain team
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityTeamPredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			List<String> teams = argumentMap.get("team");
			
			if (teams != null && !teams.isEmpty()) {
				final List<String> allowedTeams = Lists.<String>newArrayList();
				final List<String> disallowedTeams = Lists.<String>newArrayList();
				
				for (String team : teams) {
					if (team == null) continue;
					if (team.startsWith("!")) disallowedTeams.add(team.substring(1));
					else allowedTeams.add(team);
				}
				
				predicateBuilder.add(new Predicate<Entity>() {
					@Override public boolean apply(Entity entity) {
						if (!(entity instanceof EntityLivingBase)) return false;
						else {
							EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
							Team team = entitylivingbase.getTeam();
							String teamName = team == null ? null : team.getRegisteredName();
							return allowedTeams.contains(teamName) || (!disallowedTeams.isEmpty() && !disallowedTeams.contains(teamName));
	                    }
					}
				});
			}
	    }
		
		/**
		 * Creates predicates which accept only entities which have a certain game mode
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityGamemodePredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			final List<GameType> gameTypes = getGameTypes(argumentMap);

			if (!gameTypes.isEmpty()) {
				predicateBuilder.add(new Predicate<Entity>() {
					public boolean apply(Entity entity) {
						if (!(entity instanceof EntityPlayerMP))return false;
						else {
							EntityPlayerMP player = (EntityPlayerMP) entity;
							return gameTypes.contains(player.interactionManager.getGameType());
						}
					}
				});
			}
	    }
		
		/**
		 * Get the accepted game types from the argument map
		 * 
		 * @param argumentMap the argument map
		 * @return the accepted game types
		 */
		private static List<GameType> getGameTypes(ListMultimap<String, String> argumentMap) {
			List<String> argumentTypes = argumentMap.get("m");
			List<GameType> gameTypes = Lists.<GameType>newArrayList();
			if (argumentTypes == null || argumentTypes.isEmpty()) return gameTypes;
			
			for (String type : argumentTypes) {
				int id; boolean isID;
				try {id = Integer.parseInt(type); isID = true;}
				catch (NumberFormatException nfe) {id = -1; isID = false;}
				
				for (GameType gt : GameType.values()) {
					if (!isID && gt.getName().equalsIgnoreCase(type)) gameTypes.add(gt);
					else if (isID && gt.getID() == id) gameTypes.add(gt);
				}
			}
			
			return gameTypes;
		}
		
		/**
		 * Creates predicates which accept only entities which have a maximum or minimum of xp
		 * 
		 * @param argumentMap the argument map
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityExperiencePredicates(ListMultimap<String, String> argumentMap, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
			final int xpMin = getIntWithDefault(argumentMap, "lm", -1);
	        final int xpMax = getIntWithDefault(argumentMap, "l", -1);
	        
	        if (xpMin > -1 || xpMax > -1) {
	        	predicateBuilder.add(new Predicate<Entity>() {
	        		@Override public boolean apply(Entity entity) {
	        			if (!(entity instanceof EntityPlayerMP)) return false;
	        			else {
	        				EntityPlayerMP player = (EntityPlayerMP) entity;
	        				return (xpMin <= -1 || player.experienceLevel >= xpMin) && (xpMax <= -1 || player.experienceLevel <= xpMax);
	                    }
	                }
	            });
	        }
	    }
		
		/**
		 * Creates predicates which accept only entities of a certain type
		 * 
		 * @param argumentMap the argument map
		 * @param targetType the entity target type (one of {@link TargetSelector#entityTargetTypes})
		 * @param predicateBuilder the list builder to add the predicates to
		 */
		private static void getEntityTypePredicates(ListMultimap<String, String> argumentMap, String targetType, ImmutableList.Builder<Predicate<Entity>> predicateBuilder) {
	        List<String> entityTypes = argumentMap.get("type");
	        
	        if (entityTypes != null && !entityTypes.isEmpty() && (targetType.equals("e") || targetType.equals("r"))) {
				final List<String> allowedTypes = Lists.<String>newArrayList();
				final List<String> disallowedTypes = Lists.<String>newArrayList();
				
				for (String type : entityTypes) {
					if (type == null) continue;
					if (type.startsWith("!")) disallowedTypes.add(type.substring(1));
					else allowedTypes.add(type);
				}
				
				predicateBuilder.add(new Predicate<Entity>() {
					@Override public boolean apply(Entity entity) {
						String entityType = getEntityNameString(entity); if (entityType == null) return false;
						return allowedTypes.contains(entityType) || (!disallowedTypes.isEmpty() && !disallowedTypes.contains(entityType));
					}
				});
	        }
	        else if (!targetType.equals("e")){
	        	predicateBuilder.add(new Predicate<Entity>() {
	        		@Override public boolean apply(Entity entity) {
	        			return entity instanceof EntityPlayer;
	        		}
	        	});
	        }
	    }
		
		/**
		 * Gets a string representing the type of an entity
		 * 
		 * @param entity the entity
		 * @return the string representing the entity type
		 */
		private static String getEntityNameString(Entity entity) {
			String entityName = (String)EntityList.classToStringMapping.get(entity.getClass());

			if (entityName == null && entity instanceof EntityPlayer) entityName = "Player";
			else if (entityName == null && entity instanceof EntityLightningBolt) entityName = "LightningBolt";

			return entityName;
	    }
		
		/**
		 * Checks whether the arguments contain legal entity types
		 * 
		 * @param sender the command sender
		 * @param argumentMap the argument map
		 * @return whether the arguments contain legal entity types
		 */
		private static boolean isValidType(ICommandSender sender, ListMultimap<String, String> argumentMap) {
	        List<String> types = argumentMap.get("type");
	        if (types == null || types.isEmpty()) return true;
	        
	        for (String type : types) {
	        	type = type != null && type.startsWith("!") ? type.substring(1) : type;
	        	
	        	if (type != null && !isStringValidEntityType(type)) {
	        		ITextComponent component = new TextComponentTranslation("commands.generic.entity.invalidType", type);
	        		component.getChatStyle().setColor(TextFormatting.RED);
		            sender.addChatMessage(component);
		            return false;
	        	}
	        }
	        
	        return true;
		}
		
		/**
		 * Whether the given string is a valid name for an entity type
		 * 
		 * @param type the entity type
		 * @return whether the given entity type string is valid
		 */
		private static boolean isStringValidEntityType(String type) {
			return "Player".equals(type) || getEntityNameList().contains(type);
		}
		
		/**
		 * Gets the list of existing entity names
		 * 
		 * @return the entity name list
		 */
		private static List<String> getEntityNameList() {
			ArrayList<String> entityNames = Lists.<String>newArrayList();
			Iterator<String> iterator = EntityList.stringToClassMapping.keySet().iterator();

			while (iterator.hasNext()) {
				String name = iterator.next();
				Class entityClass = (Class) EntityList.stringToClassMapping.get(name);

				if (!Modifier.isAbstract(entityClass.getModifiers()))
					entityNames.add(name);
			}

			entityNames.add("LightningBolt");
			return entityNames;
		}
	}
	
	/**
	 * Merges two nbt tag compounds
	 * 
	 * @param tag the tag to merge the "other" tag into
	 * @param other the tag which shall be merged into "tag"
	 * @param mergeLists whether lists shall be replaced or merged
	 */
	public static void nbtMerge(NBTTagCompound tag, NBTTagCompound other, boolean mergeLists) {
		if (tag == null || other == null) return;
        Iterator iterator = other.getKeySet().iterator();

        while (iterator.hasNext())
        {
            String s = (String)iterator.next();
            NBTBase nbtbase = other.getTag(s);

            if (nbtbase.getId() == NBT.TAG_COMPOUND && tag.hasKey(s, NBT.TAG_COMPOUND))
            {
                   nbtMerge(tag.getCompoundTag(s), (NBTTagCompound) nbtbase, mergeLists);
            }
            else if (nbtbase.getId() == NBT.TAG_LIST && tag.hasKey(s, NBT.TAG_LIST) && mergeLists) {
            	if (((NBTTagList) nbtbase).getTagType() == ((NBTTagList) tag.getTag(s)).getTagType()) {
            		NBTTagList l1 = (NBTTagList) nbtbase, l2 = (NBTTagList) tag.getTag(s);
            		
            		for (int i = 0; i < l1.tagCount(); i++)
            			l2.appendTag(l1.get(i).copy());
            	}
            	else tag.setTag(s, nbtbase.copy());
            }
            else
            {
            	tag.setTag(s, nbtbase.copy());
            }
        }
	}
	
	/**
	 * checks whether a nbt base contains another nbt base
	 * 
	 * @param container the nbt base which shall be checked if it contains "toContain"
	 * @param toContain the nbt base which shall be contained in "container"
	 * @param listContains whether lists shall be checked if they contain all elements from the other list or if they are totally equal
	 * @return whether "container" contains "toContain"
	 */
    public static boolean nbtContains(NBTBase container, NBTBase toContain, boolean listContains) {
        if (toContain == container)
        {
            return true;
        }
        else if (toContain == null)
        {
            return true;
        }
        else if (container == null)
        {
            return false;
        }
        else if (!toContain.getClass().equals(container.getClass()) && !(toContain instanceof NBTPrimitive && container instanceof NBTPrimitive))
        {
            return false;
        }
        else if (toContain instanceof NBTTagCompound)
        {
            NBTTagCompound nbttagcompound = (NBTTagCompound)toContain;
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)container;
            Iterator iterator = nbttagcompound.getKeySet().iterator();
            String s;
            NBTBase nbtbase3;

            do
            {
                if (!iterator.hasNext())
                {
                    return true;
                }

                s = (String)iterator.next();
                nbtbase3 = nbttagcompound.getTag(s);
            }
            while (nbtContains(nbttagcompound1.getTag(s), nbtbase3, listContains));

            return false;
        }
        else if (toContain instanceof NBTTagList && listContains)
        {
            NBTTagList nbttaglist = (NBTTagList)toContain;
            NBTTagList nbttaglist1 = (NBTTagList)container;

            if (nbttaglist.tagCount() == 0)
            {
                return nbttaglist1.tagCount() == 0;
            }
            else
            {
            	for (int i = 0; i < nbttaglist.tagCount(); i++) {
            		NBTBase nbtbase2 = nbttaglist.get(i);
            		boolean flag1 = false;
            		
            		for (int j = 0; j < nbttaglist1.tagCount(); j++) {
            			if (nbtContains(nbttaglist1.get(j), nbtbase2, listContains)) {flag1 = true; break;}
            		}
            		
            		if (!flag1) return false;
            	}

                return true;
            }
        }
        else if (toContain instanceof NBTPrimitive) { //Compare numbers for equality (cast all integer types to long to compare them regardless to their type)
        	if ((toContain.getId() == NBT.TAG_BYTE || toContain.getId() == NBT.TAG_SHORT || toContain.getId() == NBT.TAG_INT || toContain.getId() == NBT.TAG_LONG) &&
        		(container.getId() == NBT.TAG_BYTE || container.getId() == NBT.TAG_SHORT || container.getId() == NBT.TAG_INT || container.getId() == NBT.TAG_LONG))
        		return ((NBTPrimitive) toContain).getLong() == ((NBTPrimitive) container).getLong();
        	else if (toContain.getId() == NBT.TAG_FLOAT && container.getId() == NBT.TAG_FLOAT)
        		return Float.compare(((NBTPrimitive) toContain).getFloat(), ((NBTPrimitive) container).getFloat()) == 0;
        	else if (toContain.getId() == NBT.TAG_DOUBLE && container.getId() == NBT.TAG_DOUBLE)
        		return Double.compare(((NBTPrimitive) toContain).getDouble(), ((NBTPrimitive) container).getDouble()) == 0;
        	else return false; //Don't compare double with float (a double-casted float has another representation than a real double, e.g. Double.compare(4.587F, 4.587D) == -1)
        }
        else
        {
            return toContain.equals(container);
        }
    }
    
    /** A map which maps shortcut names for inventory indices to the actual inventory indices */
    private static final ImmutableMap<String, Integer> SHORTCUTS;
    
    static {
    	ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
    	
    	builder.put("slot.weapon", EntityEquipmentSlot.MAINHAND.ordinal());
    	builder.put("slot.weapon.mainhand", EntityEquipmentSlot.MAINHAND.ordinal());
    	builder.put("slot.weapon.offhand", EntityEquipmentSlot.OFFHAND.ordinal());
    	builder.put("slot.armor.head", EntityEquipmentSlot.HEAD.ordinal());
    	builder.put("slot.armor.chest", EntityEquipmentSlot.CHEST.ordinal());
    	builder.put("slot.armor.legs", EntityEquipmentSlot.LEGS.ordinal());
    	builder.put("slot.armor.feet", EntityEquipmentSlot.FEET.ordinal());
    	
    	for (int i = 0; i < 54; ++i)
    		builder.put("slot.container." + i, 100 + i);
    	
    	for (int i = 0; i < 9; ++i)
    		builder.put("slot.hotbar." + i, 100 + i);

    	for (int i = 0; i < 27; ++i)
    		builder.put("slot.inventory." + i, 100 + 9 + i);
    	
    	for (int i = 0; i < 15; ++i)
    		builder.put("slot.horse." + i, 100 + 2 + i);

    	for (int i = 0; i < 27; ++i)
    		builder.put("slot.enderchest." + i, 200 + i);
    	
    	for (int i = 0; i < 8; ++i)
    		builder.put("slot.villager." + i, 300 + i);
    	
    	builder.put("slot.horse.saddle", 400);
    	builder.put("slot.horse.armor", 401);
    	builder.put("slot.horse.chest", 499);
    	
    	SHORTCUTS = builder.build();
    }
    
    /**
     * Gets the inventory slot for a shortcut
     * 
     * @param shortcut the shortcut
     * @return the slot or -1 if the shortcut name was invalid
     */
    public static int getSlotForShortcut(String shortcut) {
        if (!SHORTCUTS.containsKey(shortcut)) return -1;
        else return SHORTCUTS.get(shortcut);
    }
    
    /** The private "initHorseChest" method of an {@link EntityHorse} */
    private static final Method initChest = ReflectionHelper.getMethod(ObfuscatedMethod.EntityHorse_initHorseChest);
    /** The private "updateHorseSlots" method of an {@link EntityHorse} */
    private static final Method updateSlots = ReflectionHelper.getMethod(ObfuscatedMethod.EntityHorse_updateHorseSlots);
    
    /**
     * Replaces the item that an entity has currently selected
     * 
     * @param entity the entity
     * @param item the item that the old item will be replaced with
     * @return whether this entity can have a currently selected item
     */
    public static boolean replaceCurrentItem(Entity entity, ItemStack item) {
    	if (entity instanceof EntityLivingBase) {
    		entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item == null ? null : item.copy());
    		return true;
    	}
    	else return false;
    }
    
    /**
     * Replaces the the item tag of an item that an entity has currently selected
     * 
     * @param entity the entity
     * @param tag the tag that the old tag will be replaced with
     * @param mergeLists See the "mergeLists" parameter of {@link #nbtMerge(NBTTagCompound, NBTTagCompound, boolean)}
     * @return whether this entity can have a currently selected item
     */
    public static boolean replaceCurrentTag(Entity entity, NBTTagCompound tag, boolean mergeLists) {
    	if (entity instanceof EntityLivingBase) {
    		if (((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null) {
    			ItemStack stack = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).copy();
    			nbtMerge(stack.getTagCompound(), tag, mergeLists);
    			entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
    		}
    		return true;
    	}
    	else return false;
    }
    
    /**
     * Replaces the items in the inventory of an entity which fulfill some conditions
     * 
     * @param entity the entity
     * @param matchingItem the item that the ItemStack which is to be replaced must have, null if it doesn't matter
     * @param matchingMeta the meta type that the ItemStack which is to be replaced must have, -1 if it doesn't matter
     * @param matchingNBT the nbt tag that the ItemStack which is to be replaced must have, null if it doesn't matter
     * @param equalLists The opposite of the "listContains" parameter of {@link #nbtContains(NBTBase, NBTBase, boolean)}
     * @param item the ItemStack which is used as replacement for the matching ItemStacks
     * @return whether the entity has a modifiable inventory at all
     */
    public static boolean replaceMatchingItems(Entity entity, Item matchingItem, int matchingMeta, NBTTagCompound matchingNBT, boolean equalLists, ItemStack item) {
    	if (entity instanceof IInventory) 
    		return replaceMatchingItems((IInventory) entity, matchingItem, matchingMeta, matchingNBT, equalLists, item);
    	IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    	
    	if (entity instanceof EntityVillager) {
    		if (!(itemHandler instanceof IItemHandlerModifiable)) itemHandler = new InvWrapper(((EntityVillager) entity).getVillagerInventory());
    		else itemHandler = new CombinedInvWrapper((IItemHandlerModifiable) itemHandler, new InvWrapper(((EntityVillager) entity).getVillagerInventory()));
    	}
    	
    	if (itemHandler instanceof IItemHandlerModifiable) {
    		IItemHandlerModifiable handler = (IItemHandlerModifiable) itemHandler;
    		NBTTagCompound compound = null; int slot = entity instanceof EntityHorse ? 2 : 0;
    		
    		for (; slot < handler.getSlots(); slot++) {
    			ItemStack stack = handler.getStackInSlot(slot); if (stack == null) continue;
    			if (matchingNBT != null) {compound = new NBTTagCompound(); stack.writeToNBT(compound);}
    			
    			if ((matchingItem == null || stack.getItem() == matchingItem) && 
    				(matchingMeta == -1 || stack.getItemDamage() == matchingMeta) && 
    				(matchingNBT == null || nbtContains(compound, matchingNBT, !equalLists))) 
    				handler.setStackInSlot(slot, item == null ? null : item.copy());
    		}
    		
    		return true;
    	}
    	return false;
    }
    
    /**
     * Replaces the items in the inventory of an {@link IInventory} which fulfill some conditions
     * 
     * @param inventory the {@link IInventory}
     * @param matchingItem the item that the ItemStack which is to be replaced must have, null if it doesn't matter
     * @param matchingMeta the meta type that the ItemStack which is to be replaced must have, -1 if it doesn't matter
     * @param matchingNBT the nbt tag that the ItemStack which is to be replaced must have, null if it doesn't matter
     * @param equalLists The opposite of the "listContains" parameter of {@link #nbtContains(NBTBase, NBTBase, boolean)}
     * @param item the ItemStack which is used as replacement for the matching ItemStacks
     * @return whether the items were successfully replaced (always true)
     */
    public static boolean replaceMatchingItems(IInventory inventory, Item matchingItem, int matchingMeta, NBTTagCompound matchingNBT, boolean equalLists, ItemStack item) {
    	NBTTagCompound compound = null;
    	
    	for (int i = 0; i < inventory.getSizeInventory(); i++) {
    		ItemStack stack = inventory.getStackInSlot(i); if (stack == null) continue; 
    		if (matchingNBT != null) {compound = new NBTTagCompound(); stack.writeToNBT(compound);}
    		
			if ((matchingItem == null || stack.getItem() == matchingItem) && 
    			(matchingMeta == -1 || stack.getItemDamage() == matchingMeta) && 
    			(matchingNBT == null || nbtContains(compound, matchingNBT, !equalLists))) 
				inventory.setInventorySlotContents(i, item == null ? null : item.copy());
    	}
    	
    	return true;
    }
    
    /**
     * Replaces the nbt tag of an ItemStack by an inventory slot
     * 
     * @param inventory the {@link IInventory}
     * @param slot the inventory slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param tag the new nbt tag
     * @param mergeLists See the "mergeLists" parameter of {@link #nbtMerge(NBTTagCompound, NBTTagCompound, boolean)}
     * @return whether the nbt tag could be replaced (e.g. false for an invalid slot)
     */
    public static boolean replaceTagInInventory(IInventory inventory, int slot, NBTTagCompound tag, boolean mergeLists) {
    	slot -= 100;
    	
    	if (slot >= inventory.getSizeInventory() || slot < 0) return false;
    	else {
    		if (inventory.getStackInSlot(slot) != null) {
    			ItemStack stack = inventory.getStackInSlot(slot).copy();
    			nbtMerge(stack.getTagCompound(), tag, mergeLists);
    			inventory.setInventorySlotContents(slot, stack);
    		}
    		return true;
    	}
    }
    
    /**
     * Replaces an ItemStack by an inventory slot
     * 
     * @param inventory the {@link IInventory}
     * @param slot the inventory slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param item the new ItemStack
     * @return whether the item could be replaced (e.g. false for an invalid slot)
     */
    public static boolean replaceItemInInventory(IInventory inventory, int slot, ItemStack item) {
    	slot -= 100;
    	
    	if (slot >= inventory.getSizeInventory() || slot < 0) return false;
    	else {inventory.setInventorySlotContents(slot, item == null ? null : item.copy()); return true;}
    }
    
    /**
     *  Replaces the nbt tag of an ItemStack in a certain slot in the inventory of an entity
     * 
     * @param entity the entity
     * @param slot the inventory slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param tag the new nbt tag
     * @param mergeLists See the "mergeLists" parameter of {@link #nbtMerge(NBTTagCompound, NBTTagCompound, boolean)}
     * @return whether the nbt tag could be replaced (e.g. false for an invalid slot or for an entity which doesn't have an inventory).<br>
     *         Also false if reflective access failed (only needed for special horse inventory slots such as saddle, armor and chest)
     */
    public static boolean replaceTagInInventory(Entity entity, int slot, NBTTagCompound tag, boolean mergeLists) {
    	try {return replaceItemOrTagInInventory(entity, slot, null, tag, mergeLists);}
    	catch (Exception ex) {return false;}
    }
    
    /**
     *  Replaces the ItemStack in a certain slot in the inventory of an entity
     * 
     * @param entity the entity
     * @param slot the inventory slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param item the new ItemStack
     * @return whether the item could be replaced (e.g. false for an invalid slot or for an entity which doesn't have an inventory).<br>
     *         Also false if reflective access failed (only needed for special horse inventory slots such as saddle, armor and chest)
     */
    public static boolean replaceItemInInventory(Entity entity, int slot, ItemStack item) {
    	try {return replaceItemOrTagInInventory(entity, slot, item == null ? null : item.copy(), null, false);}
    	catch (Exception ex) {return false;}
    }
    

    /**
     *  Replaces the ItemStack or the compound tag of an ItemStack in a certain slot in the inventory of an entity
     * 
     * @param entity the entity
     * @param slot the slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param stack the new ItemStack
     * @param tag the new nbt tag. If this is not null, the nbt tag will be replaced instead of the whole item
     * @return whether the item could be replaced (e.g. false for an invalid slot or for an entity which doesn't have an inventory)
     * @throws Exeption if reflective access failed
     */
    private static boolean replaceItemOrTagInInventory(Entity entity, int slot, ItemStack stack, NBTTagCompound tag, boolean mergeLists) throws Exception {
    	if (entity instanceof IInventory) return replaceItemInInventory((IInventory) entity, slot, stack);
    	
    	if (slot < 0) return false;
    	else if (slot >= 0 && slot < 100) {
    		if (slot >= EntityEquipmentSlot.values().length) return false;
    		EntityEquipmentSlot equSlot = EntityEquipmentSlot.values()[slot];
    		
    		if (!acceptStack(entity, equSlot, stack)) return false;
    		else if (tag != null) {
    			if (!(entity instanceof EntityLivingBase)) return false;
    			EntityLivingBase living = (EntityLivingBase) entity;
    			
        		if (living.getItemStackFromSlot(equSlot) != null) {
        			stack = living.getItemStackFromSlot(equSlot).copy();
        			nbtMerge(stack.getTagCompound(), tag, mergeLists);
        			living.setItemStackToSlot(equSlot, stack);
        		}
    		}
    		else entity.setItemStackToSlot(equSlot, stack == null ? null : stack.copy());
    	}
    	else if (slot >= 100 && slot < 200) {
    		slot -= 100;
    		IItemHandler h = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    		if (!(h instanceof IItemHandlerModifiable)) return false;
    		
    		IItemHandlerModifiable inventory = (IItemHandlerModifiable) h;
    		if (slot >= inventory.getSlots()) return false;
    		else if (tag != null) {
        		if (inventory.getStackInSlot(slot) != null) {
        			stack = inventory.getStackInSlot(slot).copy();
        			nbtMerge(stack.getTagCompound(), tag, mergeLists);
        			inventory.setStackInSlot(slot, stack);
        		}
    		}
    		else inventory.setStackInSlot(slot, stack == null ? null : stack.copy());
    	}
    	else if (slot >= 200 && slot < 300 && entity instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer) entity;
    		slot -= 200;
    		
    		if (slot >= player.getInventoryEnderChest().getSizeInventory()) return false;
    		else if (tag != null) {
        		if (player.getInventoryEnderChest().getStackInSlot(slot) != null) {
        			stack = player.getInventoryEnderChest().getStackInSlot(slot).copy();
        			nbtMerge(stack.getTagCompound(), tag, mergeLists);
        			player.getInventoryEnderChest().setInventorySlotContents(slot, stack);
        		}
    		}
    		else player.getInventoryEnderChest().setInventorySlotContents(slot, stack == null ? null : stack.copy());
    	}
    	else if (slot >= 300 && slot < 400 && entity instanceof EntityVillager) {
    		EntityVillager villager = (EntityVillager) entity;
    		slot -= 300;
    		
    		if (slot >= villager.getVillagerInventory().getSizeInventory()) return false;
    		else if (tag != null) {
        		if (villager.getVillagerInventory().getStackInSlot(slot) != null) {
        			stack = villager.getVillagerInventory().getStackInSlot(slot).copy();
        			nbtMerge(stack.getTagCompound(), tag, mergeLists);
        			villager.getVillagerInventory().setInventorySlotContents(slot, stack);
        		}
    		}
    		else villager.getVillagerInventory().setInventorySlotContents(slot, stack == null ? null : stack.copy());
    	}
    	else if (slot >= 400 && slot < 500 && entity instanceof EntityHorse) {
    		if (!handleHorseSpecialSlots((EntityHorse) entity, slot - 400, stack, tag, mergeLists)) return false;
    	}
    	else return false;
    	
    	return true;
    }
    
    /**
     * Checks whether the given entity accepts an item for a certain slot
     * 
     * @param entity the entity
     * @param slot the slot to check
     * @param stack the item to check
     * @return whether the entity accepts that item for the slot
     */
    private static boolean acceptStack(Entity entity, EntityEquipmentSlot slot, ItemStack stack) {
    	if (entity instanceof EntityArmorStand || entity instanceof EntityLiving) {
    		if (stack != null && !EntityLiving.func_184648_b(slot, stack) && slot != EntityEquipmentSlot.HEAD) return false;
    		else return true;
    	}
    	else if (entity instanceof EntityPlayer) {
    		if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && stack != null && stack.getItem() != null) {
    			if (!(stack.getItem() instanceof ItemArmor) && !(stack.getItem() instanceof ItemElytra)) {
    				if (slot != EntityEquipmentSlot.HEAD) return false;
    				else return true;
    			}
    			else if (EntityLiving.getSlotForItemStack(stack) != slot) return false;
    			else return true;
    		}
    		else return true;
    	}
    	else return true;
    }
    
    /**
     * Handles special slots of the horse inventory (Slot 0: Saddle, Slot 1: Armor) and
     * a special slot 99 which must be a chest (sets whether the horse is chested or not)
     * 
     * @param slot the special horse slot (0, 1 or 99)
     * @param horse the horse
     * @param stack the item stack (must be saddle for slot 0, any horse armor for slot 1 and chest for slot 99)
     * @param tag the new nbt tag of the special slot (works only for 0 and 1, e.g. to enchant the armor)
     * @param mergeLists See the "mergeLists" parameter of {@link #nbtMerge(NBTTagCompound, NBTTagCompound, boolean)}
     * @return whether the given data was valid (valid slot, valid item, etc.) and whether the special slots could successfully be handled
     * @throws Exeption if reflective access failed
     */
    private static boolean handleHorseSpecialSlots(EntityHorse horse, int slot, ItemStack stack, NBTTagCompound tag, boolean mergeLists) throws Exception {
    	IItemHandler h = horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    	IItemHandlerModifiable inventory = h instanceof IItemHandlerModifiable ? (IItemHandlerModifiable) h : null;
    	if (initChest == null || updateSlots == null || inventory == null) return false;
    	
    	if (tag != null) {
    		if (slot < inventory.getSlots()) return false;
    		
    		if (inventory.getStackInSlot(slot) != null) {
    			stack = inventory.getStackInSlot(slot).copy();
    			nbtMerge(stack.getTagCompound(), tag, mergeLists);
    			inventory.setStackInSlot(slot, stack);
    			updateSlots.invoke(horse);
    		}
    		
    		return true;
    	}
    	
    	if (slot == 99 && horse.getType().canBeChested()) {
    		if (stack == null || horse.isChested()) {
    			horse.setChested(false);
    			initChest.invoke(horse);
    			return true;
    		}
    		
    		if (stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.chest) && !horse.isChested()) {
    			horse.setChested(true);
    			initChest.invoke(horse);
    			return true;
    		}
    		
    		return false;
    	}
    	else if (slot >= 0 && slot < 2 && slot < inventory.getSlots()) {
    		if (slot == 0 && stack != null && stack.getItem() != Items.saddle) return false;
    		else if (slot != 1 || (stack == null || HorseType.func_188577_b(stack.getItem())) && horse.getType().isHorse()) {
    			inventory.setStackInSlot(slot, stack == null ? null : stack.copy());
    			updateSlots.invoke(horse);
    			return true;
    		}
    		else return false;
    	}
    	else return false;
    }
    
    /**
     * Compares a given ItemStack with the one at a slot of an {@link IInventory}
     * 
     * @param inventory the {@link IInventory}
     * @param slot the inventory slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param stack the item stack to compare (the item and the nbt tag can be null if they shouldn't be compared).
     * @param noMeta whether to compare metadata
     * @param equalLists The opposite of the "listContains" parameter of {@link #nbtContains(NBTBase, NBTBase, boolean)}
     * @return whether the two ItemStacks are equal
     */
    public static boolean isSlotMatching(IInventory inventory, int slot, ItemStack stack, boolean noMeta, boolean equalLists) {
    	slot -= 100;
    	
    	if (slot >= inventory.getSizeInventory() || slot < 0) return false;
    	else {
    		ItemStack stackInSlot = inventory.getStackInSlot(slot);
    		
        	if (stack == null || stackInSlot == null) return stackInSlot == null && stack == null;
        	else {
        		boolean matches = stack.getItem() == null || stack.getItem() == stackInSlot.getItem();
        		matches = matches && (noMeta || stack.getItemDamage() == stackInSlot.getItemDamage());
        		matches = matches && (stack.stackSize == -1 || stack.stackSize == stackInSlot.stackSize);
        		matches = matches && nbtContains(stackInSlot.getTagCompound(), stack.getTagCompound(), !equalLists);
        		return matches;
        	}
    	}
    }
    
    /**
     * Compares a given ItemStack with the one at a slot of an inventory of an entity
     * 
     * @param entity the entity
     * @param slot the inventory slot to replace corresponding to a slot shortcut (see {@link #getSlotForShortcut(String)}
     * @param stack the item stack to compare (the item and the nbt tag can be null if they shouldn't be compared).
     * @param noMeta whether to compare metadata
     * @param equalLists The opposite of the "listContains" parameter of {@link #nbtContains(NBTBase, NBTBase, boolean)}
     * @return whether the two ItemStacks are equal
     */
    public static boolean isSlotMatching(Entity entity, int slot, ItemStack stack, boolean noMeta, boolean equalLists) {
    	if (entity instanceof IInventory) return isSlotMatching((IInventory) entity, slot, stack, noMeta, equalLists);
    	ItemStack stackInSlot;
    	
    	if (slot >= 400 && slot < 500 && entity instanceof EntityHorse)
    		slot -= 300;
    	
    	if (slot >= 0 && slot < 100 && entity instanceof EntityLivingBase) {
    		if (slot >= EntityEquipmentSlot.values().length) return false;
    		stackInSlot = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.values()[slot]);
    	}
    	else if (slot >= 100 && slot < 200) {
    		slot -= 100;
    		IItemHandler h = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    		if (!(h instanceof IItemHandlerModifiable)) return false;
    		
    		IItemHandlerModifiable inventory = (IItemHandlerModifiable) h;
    		if (slot >= inventory.getSlots()) return false;
    		else stackInSlot = inventory.getStackInSlot(slot);
    	}
    	else if (slot >= 200 && slot < 300 && entity instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer) entity;
    		slot -= 200;
    		
    		if (slot >= player.getInventoryEnderChest().getSizeInventory()) return false;
    		else stackInSlot = player.getInventoryEnderChest().getStackInSlot(slot);
    	}
    	else if (slot >= 300 && slot < 400 && entity instanceof EntityVillager) {
    		EntityVillager villager = (EntityVillager) entity;
    		slot -= 300;
    		
    		if (slot >= villager.getVillagerInventory().getSizeInventory()) return false;
    		else stackInSlot = villager.getVillagerInventory().getStackInSlot(slot);
    	}
    	else return false;
    	
    	if (stack == null || stackInSlot == null) return stackInSlot == null && stack == null;
    	else {
    		boolean matches = stack.getItem() == null || stack.getItem() == stackInSlot.getItem();
    		matches = matches && (noMeta || stack.getItemDamage() == stackInSlot.getItemDamage());
    		matches = matches && (stack.stackSize == -1 || stack.stackSize == stackInSlot.stackSize);
    		matches = matches && nbtContains(stackInSlot.getTagCompound(), stack.getTagCompound(), !equalLists);
    		return matches;
    	}
    }
}
