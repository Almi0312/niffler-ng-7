package guru.qa.niffler.data.template;

import guru.qa.niffler.data.jdbc.Connections;
import guru.qa.niffler.data.jdbc.JdbcConnectionHolder;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class JdbcTransactionTemplate {

    private final JdbcConnectionHolder holder;
    private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

    public JdbcTransactionTemplate(@Nonnull String jdbcUrl) {
        this.holder = Connections.holder(jdbcUrl);
    }

    public @Nonnull JdbcTransactionTemplate holdConnectionAfterAction() {
        this.closeAfterAction.set(false);
        return this;
    }

    public @Nonnull <T> T execute(int isolatedLvl, Supplier<T> action) {
        Connection connection = null;
        try {
            connection = holder.connection();
            connection.setTransactionIsolation(isolatedLvl);
            connection.setAutoCommit(false);
            T result = action.get();
            connection.commit();
            connection.setAutoCommit(true);
            return result;
        } catch (Exception e) {
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

    public @Nonnull <T> T execute(Supplier<T> action) {
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
