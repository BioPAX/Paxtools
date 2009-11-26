/*
 * RDBConnect.java
 *
 * 2008.05.08 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence;

import java.sql.*;

public class RDBConnect {
	public final static String DRIVERCLASS_POSTGRESQL = "org.postgresql.Driver";
	public final static String DRIVERCLASS_MYSQL = "com.mysql.jdbc.Driver";
	/**
	 * RDBドライバクラス
	 */
	String driverClass = DRIVERCLASS_POSTGRESQL;
	/**
	 * JDBC
	 */
	String connectionURL = "jdbc:postgresql://localhost/PAXTOOLS";
	/**
	 * DB接続のユーザ名
	 */
	String userName = "paxtools";
	/**
	 * DB接続のパスワード
	 */
	String password = "";

	Connection connection = null;

	// for Test
	public boolean bProgress = false;

	public RDBConnect(String driverClass, String connectionURL) {
		this.driverClass = driverClass;
		this.connectionURL = connectionURL;
	}

	/**
	 * ユーザ名を指定してDB接続
	 * @return 成否
	 * @exception Exception エラー発生
	 */
	public boolean connect(String userName, String password) {
		this.userName = userName;
		this.password = password;
		try {
			Class.forName(driverClass);
			connection = DriverManager.getConnection(connectionURL, this.userName, this.password);
			if (bProgress)
				System.out.println("connect RDB");
		}
		catch(Exception e) {
			System.out.println("RDB connect error");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * DB接続解除
	 */
	public void disconnect() {

		try {
			if (connection != null) {
				connection.close();
				if (bProgress)
					System.out.println("disconnect RDB");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		connection = null;
	}

	/**
	 * ステートメントを作成。
	 * @return	ステートメント。
	 */
	public Statement createStatement() {
		if (connection == null)
			return null;
		Statement st = null;
		try {
			st = connection.createStatement();
		}
		catch(Exception e) {
			st = null;
			e.printStackTrace();
		}
		return st;
	}

	/**
	 * ステートメントを閉じる。
	 * @param	st	ステートメント。
	 */
	public void closeStatement(Statement st) {
		if (connection == null || st == null)
			return;
		try {
			st.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void vacuum() {
		if (!driverClass.equals(DRIVERCLASS_POSTGRESQL))
			return;
		Statement st = createStatement();
		if (st == null)
			return;
		try {
			st.execute("vacuum analyze");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		closeStatement(st);
	}

	/**
	 * トランザクション開始。
	 * @param	st	ステートメント。
	 * @return 成否
	 */
	public boolean beginTransaction() {
		boolean done = false;
		Statement st = createStatement();
		try {
			st.execute("begin");
/*
			m_connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
*/
			done = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		closeStatement(st);
		return done;
	}

	/**
	 * トランザクション終了。
	 * @param	st	ステートメント。
	 * @param	bCommit	コミットするか。
	 */
	public void endTransaction(boolean bCommit) {
		Statement st = createStatement();
		try {
			if (bCommit)
				st.execute("commit");
			else
				st.execute("rollback");
/*
			if (bCommit)
				m_connection.commit();
			else
				m_connection.rollback();
*/
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		closeStatement(st);
	}
}
