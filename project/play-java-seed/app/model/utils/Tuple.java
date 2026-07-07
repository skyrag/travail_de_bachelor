package model.utils;

import io.hypersistence.utils.hibernate.type.ImmutableType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * An immutable pair of integer coordinates {@code (x, y)}.
 * <p>
 * Typically used to represent a position (e.g. a unit's position on
 * the board), and is serialized/deserialized to/from SQL as a literal
 * of the form {@code "(x,y)"} via the nested TupleType
 * Hibernate converter.
 */
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

    /**
     * Hibernate custom type mapping a Tuple to/from a raw SQL
     * value of the form {@code "(x,y)"} (e.g. a Postgres point/composite
     * type stored as Types#OTHER.
     */
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
        public Tuple fromStringValue(CharSequence sequence) {
            if (sequence == null) return null;
            String clean = sequence.toString().replaceAll("[()]", "");
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