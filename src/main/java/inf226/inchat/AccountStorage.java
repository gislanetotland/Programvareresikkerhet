package inf226.inchat;

import java.sql.*;
import java.io.*;
import java.time.Instant;
import java.util.UUID;

import inf226.storage.*;

import inf226.util.immutable.List;
import inf226.util.*;

/**
 * This class stores accounts in the database.
 */
public final class AccountStorage
    implements Storage<Account,SQLException> {
    
    final Connection connection;
    final Storage<User,SQLException> userStore;
    final Storage<Channel,SQLException> channelStore;
   
    /**
     * Create a new account storage.
     *
     * @param  connection   The connection to the SQL database.
     * @param  userStore    The storage for User data.
     * @param  channelStore The storage for channels.
     */
    public AccountStorage(Connection connection,
                          Storage<User,SQLException> userStore,
                          Storage<Channel,SQLException> channelStore) 
      throws SQLException {
        this.connection = connection;
        this.userStore = userStore;
        this.channelStore = channelStore;
        
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS Account (id TEXT PRIMARY KEY, version TEXT, user TEXT, password TEXT, FOREIGN KEY(user) REFERENCES User(id) ON DELETE CASCADE)");
        connection.createStatement()
                .executeUpdate("CREATE TABLE IF NOT EXISTS AccountChannel (account TEXT, channel TEXT, alias TEXT, ordinal INTEGER, PRIMARY KEY(account,channel), FOREIGN KEY(account) REFERENCES Account(id) ON DELETE CASCADE, FOREIGN KEY(channel) REFERENCES Channel(id) ON DELETE CASCADE)");
    }
   /*
    @Override
    public Stored<Account> save(Account account)
      throws SQLException {
        
        final Stored<Account> stored = new Stored<Account>(account);
        String sql =
           "INSERT INTO Account VALUES('" + stored.identity + "','"
                                          + stored.version  + "','"
                                          + account.user.identity + "','"
                                          + account.password + "')";
        connection.createStatement().executeUpdate(sql);
*/
    @Override
    public Stored<Account> save(Account account)
        throws SQLException {

        final Stored<Account> stored = new Stored<Account>(account);
            PreparedStatement ps = connection.prepareStatement("INSERT INTO Account VALUES(?,?,?,?)");
            ps.setObject(1, stored.identity);
            ps.setObject(2, stored.version);
            ps.setObject(3, account.user.identity);
            ps.setString(4, account.password);
            ps.executeUpdate();



/*
        // Write the list of channels
        final Maybe.Builder<SQLException> exception = Maybe.builder();
        final Mutable<Integer> ordinal = new Mutable<Integer>(0);
        account.channels.forEach(element -> {
            String alias = element.first;
            Stored<Channel> channel = element.second;
            final String msql
              = "INSERT INTO AccountChannel VALUES('" + stored.identity + "','"
                                                      + channel.identity + "','"
                                                      + alias + "','"
                                                      + ordinal.get().toString() + "')";
            try { connection.createStatement().executeUpdate(msql); }
            catch (SQLException e) { exception.accept(e) ; }
            ordinal.accept(ordinal.get() + 1);

        });
*/
        // Write the list of channels
        final Maybe.Builder<SQLException> exception = Maybe.builder();
        final Mutable<Integer> ordinal = new Mutable<Integer>(0);
        account.channels.forEach(element -> {
            String alias = element.first;
            Stored<Channel> channel = element.second;

            PreparedStatement sql = null;
            try {
                sql = connection.prepareStatement("INSERT INTO Account VALUES(?,?,?,?)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                sql.setObject(1, stored.identity);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                sql.setObject(2, channel.identity);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                sql.setString(3, alias);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                sql.setString(4, ordinal.get().toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try { sql.executeUpdate(); }
            catch (SQLException e) { exception.accept(e) ; }
            ordinal.accept(ordinal.get() + 1);

        });

        Util.throwMaybe(exception.getMaybe());
        return stored;
    }
/*
    @Override
    public synchronized Stored<Account> update(Stored<Account> account,
                                               Account new_account)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<Account> current = get(account.identity);
        final Stored<Account> updated = current.newVersion(new_account);
        if(current.version.equals(account.version)) {
            String sql = "UPDATE Account SET" +
                    " (version,user) =('"
                    + updated.version  + "','"
                    + new_account.user.identity
                    + "') WHERE id='"+ updated.identity + "'";
            connection.createStatement().executeUpdate(sql);

 */
    @Override
    public synchronized Stored<Account> update(Stored<Account> account,
                                            Account new_account)
        throws UpdatedException,
            DeletedException,
            SQLException {
    final Stored<Account> current = get(account.identity);
    final Stored<Account> updated = current.newVersion(new_account);
    if(current.version.equals(account.version)) {

        PreparedStatement sql = connection.prepareStatement("UPDATE Account SET (version,user) = (?,?) WHERE id=?");
        sql.setObject(1, updated.version);
        sql.setObject(2, new_account.user.identity);
        sql.setObject(3, updated.identity);
        sql.executeUpdate();
/*
        // Rewrite the list of channels
        connection.createStatement().executeUpdate("DELETE FROM AccountChannel WHERE account='" + account.identity + "'");

        final Maybe.Builder<SQLException> exception = Maybe.builder();
        final Mutable<Integer> ordinal = new Mutable<Integer>(0);
        new_account.channels.forEach(element -> {
            String alias = element.first;
            Stored<Channel> channel = element.second;
            final String msql
                    = "INSERT INTO AccountChannel VALUES('" + account.identity + "','"
                    + channel.identity + "','"
                    + alias + "','"
                    + ordinal.get().toString() + "')";
            try { connection.createStatement().executeUpdate(msql); }
            catch (SQLException e) { exception.accept(e) ; }
            ordinal.accept(ordinal.get() + 1);
        });
 */

        // Rewrite the list of channels
        connection.prepareStatement("DELETE FROM Account WHERE id=?");
        sql.setObject(1, account.identity);
        sql.executeUpdate();

        final Maybe.Builder<SQLException> exception = Maybe.builder();
        final Mutable<Integer> ordinal = new Mutable<Integer>(0);
        new_account.channels.forEach(element -> {
            String alias = element.first;
            Stored<Channel> channel = element.second;

            PreparedStatement msql = null;
            try {
                msql = connection.prepareStatement("INSERT INTO AccountChannel VALUES(?,?,?,?)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                msql.setObject(1, account.identity);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                msql.setObject(2, channel.identity);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                msql.setString(3, alias);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                msql.setString(4, ordinal.get().toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            /*
            try {
                msql.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }*/
            try { msql.executeUpdate(); }
            catch (SQLException e) { exception.accept(e) ; }
            ordinal.accept(ordinal.get() + 1);
        });

        Util.throwMaybe(exception.getMaybe());
    } else {
        throw new UpdatedException(current);
    }
    return updated;
    }
/*
    @Override
    public synchronized void delete(Stored<Account> account)
            throws UpdatedException,
            DeletedException,
            SQLException {
        final Stored<Account> current = get(account.identity);
        if(current.version.equals(account.version)) {
            String sql =  "DELETE FROM Account WHERE id ='" + account.identity + "'";
            connection.createStatement().executeUpdate(sql);
        } else {
            throw new UpdatedException(current);
        }
*/
    @Override
    public synchronized void delete(Stored<Account> account)
       throws UpdatedException,
              DeletedException,
              SQLException {
        final Stored<Account> current = get(account.identity);
        if(current.version.equals(account.version)) {
        PreparedStatement sql = connection.prepareStatement("DELETE FROM Account WHERE id=?");
        sql.setObject(1, account.identity);
        sql.executeUpdate();

        } else {
        throw new UpdatedException(current);
        }
    }
/*
    @Override
    public Stored<Account> get(UUID id)
            throws DeletedException,
            SQLException {

        final String accountsql = "SELECT version,user,password FROM Account WHERE id = '" + id.toString() + "'";
        final String channelsql = "SELECT channel,alias,ordinal FROM AccountChannel WHERE account = '" + id.toString() + "' ORDER BY ordinal DESC";

        final Statement accountStatement = connection.createStatement();
        final Statement channelStatement = connection.createStatement();

        final ResultSet accountResult = accountStatement.executeQuery(accountsql);
        final ResultSet channelResult = channelStatement.executeQuery(channelsql);

        if(accountResult.next()) {
            final UUID version = UUID.fromString(accountResult.getString("version"));
            final UUID userid =
                    UUID.fromString(accountResult.getString("user"));
            final String password =
                    accountResult.getString("password");
            final Stored<User> user = userStore.get(userid);
            // Get all the channels associated with this account
            final List.Builder<Pair<String,Stored<Channel>>> channels = List.builder();
            while(channelResult.next()) {
                final UUID channelId =
                        UUID.fromString(channelResult.getString("channel"));
                final String alias = channelResult.getString("alias");
                channels.accept(
                        new Pair<String,Stored<Channel>>(
                                alias,channelStore.get(channelId)));
            }
            return (new Stored<Account>(new Account(user,channels.getList(),password),id,version));
        } else {
            throw new DeletedException();
        }
    }
*/
    @Override
    public Stored<Account> get(UUID id)
      throws DeletedException,
             SQLException {

        final PreparedStatement accountsql = connection.prepareStatement("SELECT version,user,password FROM Account WHERE id=?");
        accountsql.setObject(1, id.toString());

        final PreparedStatement channelsql = connection.prepareStatement("SELECT channel,alias,ordinal FROM AccountChannel WHERE account =? ORDER BY ordinal DESC");
        channelsql.setObject(1, id.toString());

        final ResultSet accountResult = accountsql.executeQuery();
        final ResultSet channelResult = channelsql.executeQuery();

        if(accountResult.next()) {
            final UUID version = UUID.fromString(accountResult.getString("version"));
            final UUID userid =
            UUID.fromString(accountResult.getString("user"));
            final String password =
            accountResult.getString("password");
            final Stored<User> user = userStore.get(userid);

            // Get all the channels associated with this account
            final List.Builder<Pair<String,Stored<Channel>>> channels = List.builder();
            while(channelResult.next()) {
                final UUID channelId = 
                    UUID.fromString(channelResult.getString("channel"));
                final String alias = channelResult.getString("alias");
                channels.accept(
                    new Pair<String,Stored<Channel>>(
                        alias,channelStore.get(channelId)));
            }
            return (new Stored<Account>(new Account(user,channels.getList(),password),id,version));
        } else {
            throw new DeletedException();
        }
    }
/*
    /**
     * Look up an account based on their username.

    public Stored<Account> lookup(String username)
            throws DeletedException,
            SQLException {
        final String sql = "SELECT Account.id from Account INNER JOIN User ON user=User.id where User.name='" + username + "'";

*/
        /**
         * Look up an account based on their username.
         */
    public Stored<Account> lookup(String username)
      throws DeletedException,
             SQLException {

        final PreparedStatement sql = connection.prepareStatement("SELECT Account.id FROM Account INNER JOIN User ON user=User.id WHERE User.name=?");
        sql.setString(1, username);
        sql.executeQuery();

        System.err.println(sql);
        final Statement statement = connection.createStatement();
        
        final ResultSet rs = sql.executeQuery();
        if(rs.next()) {
            final UUID identity = 
                    UUID.fromString(rs.getString("id"));
            return get(identity);
        }
        throw new DeletedException();
    }
    
} 
 
