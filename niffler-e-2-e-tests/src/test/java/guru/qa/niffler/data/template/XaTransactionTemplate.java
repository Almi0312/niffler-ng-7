package guru.qa.niffler.data.template;

import com.atomikos.icatch.jta.UserTransactionImp;
import guru.qa.niffler.data.jdbc.Connections;
import guru.qa.niffler.data.jdbc.JdbcConnectionHolders;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Распределенная транзакция
 */
@ParametersAreNonnullByDefault
public class XaTransactionTemplate {

    private final JdbcConnectionHolders holders;
    private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

    public XaTransactionTemplate(String... jdbcUrl) {
        this.holders = Connections.holders(jdbcUrl);
    }

    public @Nonnull XaTransactionTemplate holdConnectionAfterAction() {
        this.closeAfterAction.set(false);
        return this;
    }

    public @Nonnull <T> T execute(Supplier<T>... actions) {
        return execute(Connection.TRANSACTION_READ_COMMITTED, actions);
    }

    public @Nonnull <T> T execute(int isolatedLvl, Supplier<T>... actions) {
        UserTransaction userTransaction = new UserTransactionImp();
        try {
            holders.setIsolation(isolatedLvl);
            userTransaction.begin();
            T result = null;
            for (Supplier<T> action : actions) {
                result = action.get();
            }
            userTransaction.commit();
            return result;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            if (closeAfterAction.get()) {
                holders.close();
            }
        }
    }

    public void execute(Runnable... actions) {
        execute(Connection.TRANSACTION_READ_COMMITTED, actions);
    }

    public void execute(int isolatedLvl, Runnable... actions) {
        // Создаем распределенную транзакцию
        UserTransaction userTransaction = new UserTransactionImp();
        try {
            holders.setIsolation(isolatedLvl);
            userTransaction.begin();
            for (Runnable action : actions) {
                action.run();
            }
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            if (closeAfterAction.get()) {
                holders.close();
            }
        }
    }

}
