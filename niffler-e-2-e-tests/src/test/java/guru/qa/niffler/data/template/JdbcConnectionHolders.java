package guru.qa.niffler.data.template;

import java.sql.SQLException;
import java.util.List;

public class JdbcConnectionHolders implements AutoCloseable {

    private final List<JdbcConnectionHolder> holders;

    public JdbcConnectionHolders(List<JdbcConnectionHolder> holders) {
        this.holders = holders;
    }

    public void setIsolation(int isolationConstant) {
        holders.forEach(con -> {
            try {
                con.connection().setTransactionIsolation(isolationConstant);
            } catch (SQLException e) {
                //NOP
            }
        });
    }

    @Override
    public void close() {
        holders.forEach(JdbcConnectionHolder::close);
    }
}
