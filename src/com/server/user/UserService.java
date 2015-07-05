package com.server.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.util.DB;


/**
 * 用户相关的业务逻辑处理类, 与数据库到交道
 * @author slave_1
 */
public class UserService {
	
	public static Logger log = Logger.getLogger(UserService.class);

	private static UserService userService = new UserService();
	
	private UserService() {
		
	}
	
	public static UserService getInstance() {
		if(userService == null) {
			userService = new UserService();
		}
		return userService;
	}

	/**
	 * 验证用户名和密码是否正确
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean check(String username, String password) {
		User u = this.getUserByName(username);
		if(u == null) return false;
		if(!password.equals(u.getPassword())) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public User getUserByName(String name) {
		String sql = "select * from user where name='"+name+"';";
		Connection conn = DB.getConn();
		Statement stmt = DB.getStatement(conn);
		ResultSet rs = null;
		rs = DB.getResultSet(stmt, sql);
		User u = new User();
		try {
			if(!rs.next()) {
				return null;
			} else {
				u.setId(rs.getInt("id"));
				u.setName(rs.getString("name"));
				u.setOnline(rs.getBoolean("online"));
				u.setPassword(rs.getString("password"));
				u.setPort(rs.getString("port"));
				u.setIp(rs.getString("ip"));
				u.setFilePort(rs.getString("file_port"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs);
			DB.close(stmt);
			DB.close(conn);
		}
		return u;
	}

	/**
	 * 得到好友列表
	 * @param msg
	 * @return
	 */
	public List<User> getFriends(String name) {
		List<User> fs = new ArrayList<User> ();
		User user = this.getUserByName(name);
		String sql = "select * from user join friend on user.id = friend.friend_id where friend.user_id="+user.getId()+";";
		Connection conn = DB.getConn();
		Statement stmt = DB.getStatement(conn);
		ResultSet rs = null;
		rs = DB.getResultSet(stmt, sql);
		try {
			while(rs.next()) {
				User u = new User();
				u.setName(rs.getString("name"));
				u.setIp(rs.getString("ip"));
				u.setPort(rs.getString("port"));
				u.setOnline(rs.getBoolean("online"));
				u.setPassword(rs.getString("password"));
				u.setId(rs.getInt("id"));
				fs.add(u);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fs;
	}
	
	/**
	 * 更新ip 和 port
	 * @param ip
	 * @param port
	 */
	public void updateAddress(User user) {
		String sql = "update user set ip=? , port = ?,file_port=? where user.name=?";
		Connection conn = DB.getConn();
		PreparedStatement pst = DB.prepare(conn, sql);
		try {
			pst.setString(1, user.getIp());
			pst.setString(2, user.getPort());
			pst.setString(3, user.getFilePort());
			pst.setString(4, user.getName());
			pst.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateOnline(User user) {
		String sql = "update user set online = ? where user.name=?";
		Connection conn = DB.getConn();
		PreparedStatement pst = DB.prepare(conn, sql);
		try {
			pst.setBoolean(1, user.isOnline());
			pst.setString(2, user.getName());
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addFriend(String userName, String fName) {
		User u1 = this.getUserByName(userName);
		User u2 = this.getUserByName(fName);
		insertFriend(u1, u2);
		insertFriend(u2, u1);
	}

	private void insertFriend(User u1, User u2) {
		String sql = "insert into friend value(? ,?)";
		Connection conn = DB.getConn();
		PreparedStatement pst = DB.prepare(conn, sql);
		try {
			pst.setInt(1, u1.getId());
			pst.setInt(2, u2.getId());
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createUser(User u) {
		String sql = "insert into user values(null , ? , ? , null ,null,default)";
		Connection conn = DB.getConn();
		PreparedStatement pst = DB.prepare(conn, sql);
		try {
			pst.setString(1, u.getName());
			pst.setString(2, u.getPassword());
			pst.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean check(String name) {
		User u = this.getUserByName(name);
		if(u == null) return true;
		return false;
	}
	
}
