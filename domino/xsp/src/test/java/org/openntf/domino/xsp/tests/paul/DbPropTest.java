package org.openntf.domino.xsp.tests.paul;

import java.util.HashMap;
import java.util.List;

import org.openntf.domino.Database;
import org.openntf.domino.Session;
import org.openntf.domino.design.DatabaseDesign.DbProperties;
import org.openntf.domino.design.impl.DatabaseDesign;
import org.openntf.domino.junit.TestRunnerUtil;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;

public class DbPropTest implements Runnable {

	public DbPropTest() {

	}

	public static void main(final String[] args) {
		TestRunnerUtil.runAsDominoThread(new DbPropTest(), TestRunnerUtil.NATIVE_SESSION);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			Session sess = Factory.getSession(SessionType.NATIVE);
			Database db = sess.getDatabase("PrivateTest.nsf");
			StringBuilder sb = new StringBuilder();
			getDbInfo(db, sb);
			setDbInfo(db);
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getDbInfo(final Database db, final StringBuilder sb) {
		DatabaseDesign dbDesign = (DatabaseDesign) db.getDesign();
		List<DbProperties> props = dbDesign.getDatabaseProperties();
		for (DbProperties prop : props) {
			sb.append(prop);
			addNewLine(sb);
		}
		sb.append(dbDesign.getTemplateName());
		addNewLine(sb);
		sb.append(dbDesign.getNameIfTemplate());
		addNewLine(sb);
		sb.append("DAS Setting = " + dbDesign.getDasSetting().name());
		addNewLine(sb);
		sb.append("Replicate Unread = " + dbDesign.getReplicateUnreadSetting().name());
		addNewLine(sb);
		sb.append("Max Updated = " + dbDesign.getMaxUpdatedBy());
		addNewLine(sb);
		sb.append("Max Revisions = " + dbDesign.getMaxRevisions());
		addNewLine(sb);
		sb.append("Soft Deletes = " + dbDesign.getSoftDeletionsExpireIn());
	}

	private void setDbInfo(final Database db) {
		DatabaseDesign dbDesign = (DatabaseDesign) db.getDesign();
		HashMap<DbProperties, Boolean> props = new HashMap<>();
		props.put(DbProperties.USE_JS, false);
		props.put(DbProperties.REQUIRE_SSL, true);
		props.put(DbProperties.NO_URL_OPEN, false);
		dbDesign.setDatabaseProperties(props);
		dbDesign.setMaxRevisions(30);
		dbDesign.save();
	}

	private StringBuilder addNewLine(final StringBuilder sb) {
		sb.append("\r\n");
		return sb;
	}

}
