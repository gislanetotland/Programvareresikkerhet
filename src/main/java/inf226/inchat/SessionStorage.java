package inf226.inchat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;

import inf226.storage.*;

import java.sql.*;
import java.io.*;

/**
 * The SessionStorage stores Session objects in a SQL database.
 */
public final class SessionStorage
    implements Storage<Session,SQLException> {
    
    final Connection connection;
    final Storage<Account,SQLException> accountStorage;
    
    public SessionStorage(Connection connection,
                          Storage<Account,SQLException> accountStorage)
      throws SQLException {
        this.connection = connection;
        this.accountStorage = accountStorage;
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS Session (id TEXT PRIMARY KEY, version TEXT, account TEXT, expiry TEXT, FOREIGN KEY(account) REFERENCES Account(id) ON DELETE CASCADE)");
    }
/*    
    @Override
    public Stored<Session> save(Session session)
            throws SQLException {

        final Stored<Session> stored = new Stored<Session>(session);
        String sql =  "INSERT INTO Session VALUES('" + stored.identity + "','"
                + stored.version  + "','"
                + session.account.identity  + "','"
                + session.expiry.toString() + "')";
        connection.createStatement().executeUpdate(sql);
        return stored;
    }
    
 */
    @Override
    public Stored<Session> save(Session session)
            throws SQLException {

        final Stored<Session> stored = new Stored<Session>(session);
        PreparedStatement sql = connection.prepareStatement("INSERT INTO Session VALUES(?,?,?,?)");
        sql.setObject(1, stored.identity);
        sql.setObject(2, stored.version);
        sql.setObject(3, session.account.identity);
        sql.setString(4, session.expiry.toString());
        sql.executeUpdate();
        return stored;
        
    }
    
/*    
    @Override
    public synchronized Stored<Session> update(Stored<Session> session,
                                               Session new_session)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<Session> current = get(session.identity);
        final Stored<Session> updated = current.newVersion(new_session);
        if(current.version.equals(session.version)) {
            String sql = "UPDATE Session SET" +
                    " (version,account,expiry) =('"
                    + updated.version  + "','"
                    + new_session.account.identity  + "','"
                    + new_session.expiry.toString()
                    + "') WHERE id='"+ updated.identity + "'";
            connection.createStatement().executeUpdate(sql);
        } else {
            throw new UpdatedException(current);
        }
        return updated;
    }  
*/    
    @Override
    public synchronized Stored<Session> update(Stored<Session> session,
                                            Session new_session)
        throws UpdatedException,
            DeletedException,
            SQLException {
    final Stored<Session> current = get(session.identity);
    final Stored<Session> updated = current.newVersion(new_session);
    if(current.version.equals(session.version)) {
        PreparedStatement sql = connection.prepareStatement("UPDATE session SET (version,account,expiry)=(?,?,?) WHERE id=?");
        sql.setObject(1, updated.version);
        sql.setObject(2, new_session.account.identity);
        sql.setString(3, new_session.expiry.toString());
        sql.setObject(4, updated.identity);
        sql.executeUpdate();
       
    } else {
        throw new UpdatedException(current);
    }
    return updated;
    }
/*
    @Override
    public synchronized void delete(Stored<Session> session)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<Session> current = get(session.identity);
        if(current.version.equals(session.version)) {
            String sql =  "DELETE FROM Session WHERE id ='" + session.identity + "'";
            connection.createStatement().executeUpdate(sql);

        } else {
            throw new UpdatedException(current);
        }
    }
*/    
    @Override
    public synchronized void delete(Stored<Session> session)
       throws UpdatedException,
              DeletedException,
              SQLException {
        final Stored<Session> current = get(session.identity);
        if(current.version.equals(session.version)) {
        PreparedStatement sql = connection.prepareStatement("DELETE FROM Session WHERE id =?");
        sql.setObject(1, session.identity);
        sql.executeUpdate();

        } else {
        throw new UpdatedException(current);
        }
    }
/*
    @Override
    public Stored<Session> get(UUID id)
            throws DeletedException,
            SQLException {
        final String sql = "SELECT version,account,expiry FROM Session WHERE id = '" + id.toString() + "'";


        final Statement statement = connection.createStatement();
        final ResultSet rs = statement.executeQuery(sql);

        if(rs.next()) {
            final UUID version = UUID.fromString(rs.getString("version"));
            final Stored<Account> account
                    = accountStorage.get(
                    UUID.fromString(rs.getString("account")));
            final Instant expiry = Instant.parse(rs.getString("expiry"));
            return (new Stored<Session>
                    (new Session(account,expiry),id,version));
        } else {
            throw new DeletedException();
        }
    }


}
*/
    @Override
    public Stored<Session> get(UUID id)
      throws DeletedException,
             SQLException {
        final PreparedStatement sql = connection.prepareStatement("SELECT version,account,expiry FROM Session WHERE id =?");
        sql.setString(1, id.toString());


        final ResultSet rs = sql.executeQuery();

        if(rs.next()) {
            final UUID version = UUID.fromString(rs.getString("version"));
            final Stored<Account> account
               = accountStorage.get(
                    UUID.fromString(rs.getString("account")));
            final Instant expiry = Instant.parse(rs.getString("expiry"));
            return (new Stored<Session>
                        (new Session(account,expiry),id,version));
        } else {
            throw new DeletedException();
        }
    }
    
    
} 
