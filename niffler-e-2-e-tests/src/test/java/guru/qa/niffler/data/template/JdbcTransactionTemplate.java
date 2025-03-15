package guru.qa.niffler.data.template;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JdbcTransactionTemplate {

    private final JdbcConnectionHolder holder;
    private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

    public JdbcTransactionTemplate(String jdbcUrl) {
        this.holder = Connections.holder(jdbcUrl);
    }

    public JdbcTransactionTemplate holdConnectionAfterAction() {
        this.closeAfterAction.set(false);
        return this;
    }

    public <T> T execute(int isolatedLvl, Supplier<T> action) {
        Connection connection = null;
        try {
            connection = holder.connection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(isolatedLvl);
            T result = action.get();
            connection.commit();
            connection.setAutoCommit(true);
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (closeAfterAction.get()) {
                holder.close();
            }
        }
    }
    public <T> T execute(Supplier<T> action) {
        return execute(Connection.TRANSACTION_READ_COMMITTED, action);
    }

    public void execute(int isolatedLvl, Consumer action) {
        Connection connection = null;
        try {
            connection = holder.connection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(isolatedLvl);
            action.accept(connection);
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (closeAfterAction.get()) {
                holder.close();
            }
        }
    }

    public void execute(Consumer action) {
        execute(Connection.TRANSACTION_READ_COMMITTED, action);
    }

}
