package hafnium.bukkit.pail.stone.ic;

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.pipe.data.DataController;
import hafnium.bukkit.pail.pipe.data.DatumID;
import hafnium.bukkit.pail.pipe.data.TextDatum;
import hafnium.bukkit.pail.stone.LegacyLoader.LegacyDirection;
import hafnium.bukkit.pail.util.BlockLocation;
import hafnium.bukkit.pail.util.Coordinate;
import hafnium.bukkit.pail.util.sign.SignText;

public class RewriteHelper {
	public static void updateSendRecv(SignText text, boolean send) {
		String oldChannel = text.getLine(1);
		String oldBand = text.getLine(2);

		text.setLine(2, "");

		String newChannel = oldChannel;
		String newBand = oldBand;

		if (oldBand.startsWith("~")) {
			newChannel = oldBand.substring(1) + "-" + oldChannel;
			newBand = "*";

			if (newChannel.length() > 15) {
				int bChars = oldBand.length() - 1;
				int cChars = oldChannel.length();

				while (bChars + cChars + 1 > 15)
					if (bChars > 3)
						bChars--;
					else
						cChars--;

				newChannel = oldBand.substring(1, bChars + 1) + "-" + oldChannel.substring(0, cChars);
			}
		}

		hafnium.bukkit.pail.stone.PailStone.getInstance().getLogger()
				.fine("Rewriting Send or Recv\nOLD ARGS: " + oldChannel + " " + oldBand + "\nNEW ARGS: " + newChannel + " " + newBand);

		text.setLine(1, newChannel);
		text.setLine(2, (!send ? newBand : ""));
	}

	public static Coordinate firstAir(BlockLocation from, LegacyDirection dir) {
		from.getChunk().load();

		int xd = 0;
		int yd = 0;
		int zd = 0;

		if (dir == LegacyDirection.ERROR)
			dir = LegacyDirection.UP;

		switch (dir) {
		case NORTH:
			xd = -1;
			break;
		case SOUTH:
			xd = 1;
			break;
		case EAST:
			zd = -1;
			break;
		case WEST:
			zd = 1;
			break;
		case UP:
			yd = 1;
			break;
		case DOWN:
			yd = -1;
			break;
		}

		int xoff = xd * 2;
		int yoff = yd * 2;
		int zoff = zd * 2;

		int its = 16;

		do {
			xoff += xd;
			yoff += yd;
			zoff += zd;
			its--;
		} while ((its > 0) && (from.add(xoff, yoff, zoff).getBlock().getTypeId() != 0));

		return new Coordinate(xoff, yoff, zoff);
	}

	public static DatumID newTextDatum(String text, String owner) {
		return newTextDatum(text, owner, 0);
	}

	public static DatumID newTextDatum(String text, String owner, int offset) {
		StringBuffer idName = new StringBuffer();

		for (int i = 0; (i < text.length()) && (i < 10); i++) {
			char nc = text.charAt(i);
			if (Character.isLetterOrDigit(nc))
				idName.append(Character.toLowerCase(nc));
		}

		idName.append((Math.abs(text.hashCode() + offset) % 999));

		DatumID id = new DatumID(owner, idName.toString(), hafnium.bukkit.pail.pipe.data.DataScope.PERSONAL);

		DataController<TextDatum> ctrl = PailPipe.getInstance().getTextManager().getController();

		if (ctrl.has(id))
			return newTextDatum(text, owner, offset + 1);

		ctrl.put(id, new TextDatum(text));

		return id;
	}
}
