package hafnium.bukkit.pail.stone.ic.executors.spec;

import hafnium.bukkit.pail.pipe.PailPipe;
import hafnium.bukkit.pail.pipe.data.AreaDatum;
import hafnium.bukkit.pail.pipe.data.DataController;
import hafnium.bukkit.pail.pipe.data.DataUse;
import hafnium.bukkit.pail.pipe.data.DatumID;
import hafnium.bukkit.pail.stone.ic.ICExecutor;
import hafnium.bukkit.pail.stone.ic.ICStub;
import hafnium.bukkit.pail.stone.ic.states.ICState;
import hafnium.bukkit.pail.stone.parsing.ICParser;
import hafnium.bukkit.pail.stone.parsing.args.DatumIDCapture;
import hafnium.bukkit.pail.stone.trigger.EventArgs;
import hafnium.bukkit.pail.util.redstone.Current;
import hafnium.bukkit.pail.util.sign.SignLocation;
import hafnium.bukkit.pail.util.sign.SignText;
import hafnium.bukkit.pail.util.text.MessageableException;

import org.bukkit.ChatColor;

public class AreaExec extends ICExecutor {

	@Override
	public ICState create(SignLocation loc, SignText text, String owner, ICParser signParser) throws MessageableException {
		AreaState state = new AreaState();

		DatumIDCapture areaID = new DatumIDCapture();

		signParser.getLine(1).add(areaID);

		DatumID id = areaID.getResult();

		PailPipe.getInstance().getAreaManager().getController().fill(id, owner);

		id.assertCanPerform(owner, DataUse.USE, PailPipe.getInstance().getAreaManager().getController());

		state.setAreaID(areaID.getResult());

		return state;
	}

	@Override
	public void trigger(ICStub ic, EventArgs event) {
		if (ic.getController().getInput(0) != Current.RISING)
			return;

		AreaState state = ic.getState();

		DataController<AreaDatum> ctrl = PailPipe.getInstance().getAreaManager().getController();

		if (ctrl.has(state.getAreaID())) {
			state.debug("Updating Area");
			AreaDatum area = ctrl.get(state.getAreaID());
			area.restore();
		} else
			state.debug(ChatColor.DARK_RED + "Invalid Area");
	}

	public static class AreaState extends ICState {
		private DatumID areaID;

		/**
		 * @return the areaID
		 */
		public DatumID getAreaID() {
			return this.areaID;
		}

		/**
		 * @param areaID
		 *            the areaID to set
		 */
		public void setAreaID(DatumID areaID) {
			this.areaID = areaID;
			this.dirty();
		}
	}
}
