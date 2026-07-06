package model.utils;

import io.hypersistence.utils.hibernate.type.ImmutableType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class Tuple {
    private final int x;
    private final int y;

    public Tuple(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple t)) return false;
        return x == t.x && y == t.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    // Convertisseur Hibernate
    public static class TupleType extends ImmutableType<Tuple> {

        public TupleType() {
            super(Tuple.class);
        }

        @Override
        public int getSqlType() {
            return Types.OTHER;
        }

        @Override
        protected Tuple get(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
                throws SQLException {
            String val = rs.getString(position);
            if (val == null) return null;
            String clean = val.replaceAll("[()]", "");
            String[] parts = clean.split(",");
            return new Tuple(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim())
            );
        }

        @Override
        protected void set(PreparedStatement st, Tuple value, int index, SharedSessionContractImplementor session)
                throws SQLException {
            if (value == null) {
                st.setNull(index, Types.OTHER);
            } else {
                st.setObject(index, value.toString(), Types.OTHER);
            }
        }
    }
}