package org.openmuc.framework.lib.rest1.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.framework.lib.rest1.common.enums.DischargeState;
import org.openmuc.framework.lib.rest1.common.enums.Status;
import org.openmuc.framework.lib.rest1.domain.model.SoHSchedule;

public class SoHScheduleRepoImpl {
    private final static String url      = "jdbc:postgresql://localhost:5432/openmuc";
    private final static String user     = "openmuc_user";
    private final static String password = "openmuc";

    private static final String BASE_SELECT =
            "SELECT id, str_id, used_q, soh, soc_before, soc_after, current, state, status, start_datetime, update_datetime, end_datetime " +
            "FROM soh_schedule ";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void save(SoHSchedule s) {
        if (s == null) {
            return;
        }

        boolean isInsert = (s.getId() == null || s.getId() == 0);
        String insertSql = "INSERT INTO soh_schedule (str_id, used_q, soh, soc_before, soc_after, current, state, status, start_datetime, update_datetime, end_datetime) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE soh_schedule SET str_id = ?, used_q = ?, soh = ?, soc_before = ?, soc_after = ?, current = ?, state = ?, status = ?, start_datetime = ?, update_datetime = ?, end_datetime = ? " +
                        "WHERE id = ?";

        try (Connection con = getConnection()) {
            if (isInsert) {
                try (PreparedStatement ps = con.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    int idx = 1;
                    ps.setString(idx++, s.getStrId());
                    if (s.getUsedQ() != null) ps.setDouble(idx++, s.getUsedQ()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getSoh() != null) ps.setDouble(idx++, s.getSoh()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getSocBefore() != null) ps.setDouble(idx++, s.getSocBefore()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getSocAfter() != null) ps.setDouble(idx++, s.getSocAfter()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getCurrent() != null) ps.setDouble(idx++, s.getCurrent()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getState() != null) ps.setInt(idx++, s.getState().ordinal()); else ps.setNull(idx++, java.sql.Types.INTEGER);
                    if (s.getStatus() != null) ps.setInt(idx++, s.getStatus().ordinal()); else ps.setNull(idx++, java.sql.Types.INTEGER);
                    ps.setTimestamp(idx++, s.getStartDatetime() != null ? Timestamp.valueOf(s.getStartDatetime()) : null);
                    ps.setTimestamp(idx++, s.getUpdateDatetime() != null ? Timestamp.valueOf(s.getUpdateDatetime()) : null);
                    ps.setTimestamp(idx++, s.getEndDatetime() != null ? Timestamp.valueOf(s.getEndDatetime()) : null);

                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            s.setId(keys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    int idx = 1;
                    ps.setString(idx++, s.getStrId());
                    if (s.getUsedQ() != null) ps.setDouble(idx++, s.getUsedQ()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getSoh() != null) ps.setDouble(idx++, s.getSoh()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getSocBefore() != null) ps.setDouble(idx++, s.getSocBefore()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getSocAfter() != null) ps.setDouble(idx++, s.getSocAfter()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getCurrent() != null) ps.setDouble(idx++, s.getCurrent()); else ps.setNull(idx++, java.sql.Types.DOUBLE);
                    if (s.getState() != null) ps.setInt(idx++, s.getState().ordinal()); else ps.setNull(idx++, java.sql.Types.INTEGER);
                    if (s.getStatus() != null) ps.setInt(idx++, s.getStatus().ordinal()); else ps.setNull(idx++, java.sql.Types.INTEGER);
                    ps.setTimestamp(idx++, s.getStartDatetime() != null ? Timestamp.valueOf(s.getStartDatetime()) : null);
                    ps.setTimestamp(idx++, s.getUpdateDatetime() != null ? Timestamp.valueOf(s.getUpdateDatetime()) : null);
                    ps.setTimestamp(idx++, s.getEndDatetime() != null ? Timestamp.valueOf(s.getEndDatetime()) : null);
                    ps.setLong(idx, s.getId());

                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        throw new RuntimeException("Update failed, no row with id: " + s.getId());
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SoHSchedule findByIdAndStateInAndStatus(Long id, List<DischargeState> states, Status status) {
        if (id == null || states == null || states.isEmpty() || status == null) {
            return null;
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE id = ? AND state IN (");
        appendPlaceholders(sql, states.size());
        sql.append(") AND status = ?");

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setLong(idx++, id);
            for (DischargeState s : states) {
                ps.setInt(idx++, s.ordinal());
            }
            ps.setInt(idx, status.ordinal());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public SoHSchedule findByStrIdAndStateInAndStatus(String strId, List<DischargeState> states, Status status) {
        if (strId == null || states == null || states.isEmpty() || status == null) {
            return null;
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE str_id = ? AND state IN (");
        appendPlaceholders(sql, states.size());
        sql.append(") AND status = ?");

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, strId);
            for (DischargeState s : states) {
                ps.setInt(idx++, s.ordinal());
            }
            ps.setInt(idx, status.ordinal());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public SoHSchedule findByStrIdAndStateAndStatus(String strId, DischargeState state, Status status) {
        if (strId == null || state == null || status == null) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE str_id = ? AND state = ? AND status = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, strId);
            ps.setInt(2, state.ordinal());
            ps.setInt(3, status.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public SoHSchedule findByIdAndStateAndStatus(Long id, DischargeState state, Status status) {
        if (id == null || state == null || status == null) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE id = ? AND state = ? AND status = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setInt(2, state.ordinal());
            ps.setInt(3, status.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<SoHSchedule> findByStartDatetimeBeforeAndStateAndStatus(LocalDateTime startDatetime, DischargeState state, Status status) {
        List<SoHSchedule> list = new ArrayList<>();
        if (startDatetime == null || state == null || status == null) {
            return list;
        }
        String sql = BASE_SELECT + "WHERE start_datetime < ? AND state = ? AND status = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startDatetime));
            ps.setInt(2, state.ordinal());
            ps.setInt(3, status.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<SoHSchedule> findByStatus(Status status) {
        List<SoHSchedule> list = new ArrayList<>();
        if (status == null) {
            return list;
        }
        String sql = BASE_SELECT + "WHERE status = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, status.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static void appendPlaceholders(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
    }

    private SoHSchedule mapRow(ResultSet rs) throws SQLException {
        SoHSchedule s = new SoHSchedule();
        s.setId(rs.getLong("id"));
        s.setStrId(rs.getString("str_id"));
        s.setUsedQ(rs.getDouble("used_q"));
        s.setSoh(rs.getDouble("soh"));
        s.setSocBefore(rs.getDouble("soc_before"));
        s.setSocAfter(rs.getDouble("soc_after"));
        s.setCurrent(rs.getDouble("current"));
        int stateOrdinal = rs.getInt("state");
        if (!rs.wasNull()) {
            DischargeState[] values = DischargeState.values();
            if (stateOrdinal >= 0 && stateOrdinal < values.length) {
                s.setState(values[stateOrdinal]);
            }
        }
        int statusOrdinal = rs.getInt("status");
        if (!rs.wasNull()) {
            Status[] statusValues = Status.values();
            if (statusOrdinal >= 0 && statusOrdinal < statusValues.length) {
                s.setStatus(statusValues[statusOrdinal]);
            }
        }
        Timestamp tsStart = rs.getTimestamp("start_datetime");
        if (tsStart != null) s.setStartDatetime(tsStart.toLocalDateTime());
        Timestamp tsUpdate = rs.getTimestamp("update_datetime");
        if (tsUpdate != null) s.setUpdateDatetime(tsUpdate.toLocalDateTime());
        Timestamp tsEnd = rs.getTimestamp("end_datetime");
        if (tsEnd != null) s.setEndDatetime(tsEnd.toLocalDateTime());
        return s;
    }
}