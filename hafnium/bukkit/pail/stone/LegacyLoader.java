package hafnium.bukkit.pail.stone;

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.pipe.data.AreaDatum;
import hafnium.bukkit.pail.pipe.data.DataScope;
import hafnium.bukkit.pail.pipe.data.DatumID;
import hafnium.bukkit.pail.stone.ic.ICFactory.Result;
import hafnium.bukkit.pail.util.BlockLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;

public class LegacyLoader {
	private static final File legacyICFile = new File("./plugins/pail/stone/signs.dat");
	private static final File legacyAreaDir = new File("./plugins/pail/stone/areas/");

	public static void loadOldData() {
		Logger log = PailStone.getInstance().getLogger();

		if (legacyICFile.exists()) {

			log.info("\tLoading legacy ICs...");
			log.info("\t( This may take a few minutes )");

			BufferedReader rd;

			try {
				rd = new BufferedReader(new FileReader(legacyICFile));
			} catch (Exception e) {
				log.log(Level.SEVERE, "Could not load legacy ICs due to an error opening their data file.", e);
				return;
			}

			try {
				rd.readLine();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Could not load legacy ICs due to an error reading the file header.", e);
				return;
			}

			String line;
			ArrayList<LegacyIC> ics = new ArrayList<LegacyIC>();

			try {
				while ((line = rd.readLine()) != null)
					ics.add(LegacyIC.fromString(line));
			} catch (IOException e) {
				log.log(Level.SEVERE, "Encountered an error while reading the legacy IC file.", e);
				return;
			}

			log.info("\t\tFound " + ics.size() + " IC definitions on file.");
			log.info("\t\tConverting ICs...");

			for (LegacyIC ic : ics)
				ic.convert();

			log.info("\tFinished loading legacy ICs.");

			try {
				rd.close();
			} catch (IOException e) {
				log.warning("Could not close the legacy IC file. It may not delete properly.");
				legacyICFile.deleteOnExit();
			}

			legacyICFile.delete();
		} else
			log.fine("\tNo legacy ICs found.");

		if (legacyAreaDir.exists()) {

			File[] areas = legacyAreaDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File path) {
					return path.getName().endsWith(".area");
				}
			});

			if (areas.length > 0) {

				log.info("\tLoading legacy areas...");

				for (File areaFile : areas) {
					AreaDatum area = new AreaDatum();
					try {
						area.readFrom(areaFile);
						PailPipe.getInstance().getAreaManager().getController()
								.put(new DatumID(area.getOwner(), area.getName(), DataScope.PERSONAL), area);
						areaFile.delete();
					} catch (Exception e) {
						log.info("\t\tCould not read: " + areaFile.getName());
						log.info("\t\tThe area was lost.");
					}
				}

				log.info("\tDone loading legacy areas.");
			}

			legacyAreaDir.delete();
		}
	}

	public static class LegacyIC {
		private final String world;
		private final int x, y, z;
		private final LegacyDirection direction;
		private final String owner;
		private final String[] lines;
		private final String data;

		/**
		 * @param world
		 * @param x
		 * @param y
		 * @param z
		 * @param direction
		 * @param owner
		 * @param lines
		 * @param data
		 */
		public LegacyIC(String world, int x, int y, int z, LegacyDirection direction, String owner, String[] lines, String data) {
			super();
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.direction = direction;
			this.owner = owner;
			this.lines = lines;
			this.data = data;
		}

		public void convert() {
			BlockLocation loc = new BlockLocation(this.world, this.x, this.y, this.z);

			if (PailStone.getInstance().getRegistry().getICAt(loc) != null)
				PailStone.getInstance().getLogger().fine("IC already exists at: " + loc);

			Result res = hafnium.bukkit.pail.stone.ic.ICFactory.createIC(loc, null, this.owner, true, true, this.data);

			if (res != Result.SUCCESS)
				PailStone.getInstance().getLogger().fine("\t\tError parsing IC at " + loc + ":\n\t\t\t" + res);
		}

		/**
		 * @return the world
		 */
		public String getWorld() {
			return this.world;
		}

		/**
		 * @return the x
		 */
		public int getX() {
			return this.x;
		}

		/**
		 * @return the y
		 */
		public int getY() {
			return this.y;
		}

		/**
		 * @return the z
		 */
		public int getZ() {
			return this.z;
		}

		/**
		 * @return the direction
		 */
		public LegacyDirection getDirection() {
			return this.direction;
		}

		/**
		 * @return the owner
		 */
		public String getOwner() {
			return this.owner;
		}

		/**
		 * @return the lines
		 */
		public String[] getLines() {
			return this.lines;
		}

		/**
		 * @return the data
		 */
		public String getData() {
			return this.data;
		}

		public static LegacyIC fromString(String icDef) {
			String[] params = decombinate(icDef, '\u001F');
			if (params.length >= 11) {

				String world = params[0].trim();

				int x = Integer.parseInt(params[1].trim());
				int y = Integer.parseInt(params[2].trim());
				int z = Integer.parseInt(params[3].trim());

				LegacyDirection facing = LegacyDirection.fromString(params[4].trim());

				String owner = params[5].trim();

				String[] lines = new String[4];
				lines[0] = params[6].trim();
				lines[1] = params[7].trim();
				lines[2] = params[8].trim();
				lines[3] = params[9].trim();

				String data = params[10].trim();

				return new LegacyIC(world, x, y, z, facing, owner, lines, data);
			} else
				return null;
		}
	}

	public static String[] decombinate(String string, char delimiter) {
		String[] result = new String[count(string, delimiter) + 1];
		int pos = 0;

		for (int i = 0; i < result.length; i++)
			result[i] = "";

		for (int i = 0; i < string.length(); i++)
			if (string.charAt(i) == delimiter)
				pos++;
			else
				result[pos] += string.charAt(i);
		return result;
	}

	/**
	 * Counts the amount of a char in a string.
	 * 
	 * @param source
	 * @param toCount
	 * @return
	 */
	public static int count(String source, char toCount) {
		int count = 0;
		for (int i = 0; i < source.length(); i++)
			if (source.charAt(i) == toCount)
				count++;
		return count;
	}

	public static enum LegacyDirection {
		EAST, WEST, NORTH, SOUTH, UP, DOWN, ERROR;
		public static LegacyDirection invert(LegacyDirection d) {

			switch (d) {
			case EAST:
				return WEST;
			case WEST:
				return EAST;
			case NORTH:
				return SOUTH;
			case SOUTH:
				return NORTH;
			case UP:
				return DOWN;
			case DOWN:
				return UP;
			}

			return null;
		}

		public static Location shift(Location l, LegacyDirection d, int distance) {

			Location l2 = new Location(l.getWorld(), 0, 0, 0);

			l2.setX(l.getBlockX());
			l2.setY(l.getBlockY());
			l2.setZ(l.getBlockZ());

			if (d == EAST)
				l2.setZ(l.getBlockZ() - distance);

			if (d == WEST)
				l2.setZ(l.getBlockZ() + distance);

			if (d == NORTH)
				l2.setX(l.getBlockX() - distance);

			if (d == SOUTH)
				l2.setX(l.getBlockX() + distance);

			if (d == UP)
				l2.setY(l.getBlockY() + distance);

			if (d == DOWN)
				l2.setY(l.getBlockY() - distance);

			return l2;

		}

		public static int[] shift(int[] l, LegacyDirection d, int distance) {

			int x, y, z;

			x = l[0];
			y = l[1];
			z = l[2];

			if (d == EAST)
				z -= (distance);

			if (d == WEST)
				z += distance;

			if (d == NORTH)
				x -= (distance);

			if (d == SOUTH)
				x += distance;

			if (d == UP)
				y += distance;

			if (d == DOWN)
				y -= (distance + 1);

			int lo[] = new int[3];

			lo[0] = x;
			lo[1] = y;
			lo[2] = z;

			return lo;

		}

		public static LegacyDirection right(LegacyDirection d) {

			LegacyDirection d2 = d;

			if (d == EAST)
				d2 = SOUTH;

			if (d == WEST)
				d2 = NORTH;

			if (d == NORTH)
				d2 = EAST;

			if (d == SOUTH)
				d2 = WEST;

			return d2;
		}

		public static LegacyDirection left(LegacyDirection d) {

			LegacyDirection d2 = d;

			if (d == EAST)
				d2 = NORTH;

			if (d == WEST)
				d2 = SOUTH;

			if (d == NORTH)
				d2 = WEST;

			if (d == SOUTH)
				d2 = EAST;

			return d2;
		}

		public static LegacyDirection fromString(String line) {

			LegacyDirection t;

			try {
				t = LegacyDirection.valueOf(line);
			} catch (Exception e) {
				t = ERROR;
			}

			return t;

		}

		public static Location center(Location l) {
			l.setX(l.getBlockX() + .5);
			l.setY(l.getBlockY() + .5);
			l.setZ(l.getBlockZ() + .5);

			return l;
		}
	}

}
