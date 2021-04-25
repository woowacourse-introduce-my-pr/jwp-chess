package chess.webdao;

import chess.webdto.ChessGameTableDto;
import chess.webdto.GameRoomDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class SpringChessGameDao {
    private final JdbcTemplate jdbcTemplate;

    public SpringChessGameDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ChessGameTableDto> ChessGameInfoRowMapper = (resultSet, rowNum) -> {
        ChessGameTableDto chessGameTableDTO = new ChessGameTableDto(
                resultSet.getString("current_turn_team"),
                resultSet.getBoolean("is_playing")
        );
        return chessGameTableDTO;
    };

    public int createGameRoom(final String roomName) {
        String sql = "INSERT into game_room_info (room_name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"game_id"});
            ps.setString(1, roomName);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    public List<GameRoomDto> loadGameRooms() {
        String sql = "SELECT * FROM game_room_info";
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> {
                    final int game_id = resultSet.getInt("game_id");
                    final String room_name = resultSet.getString("room_name");
                    return new GameRoomDto(game_id, room_name);
                }
        );
    }

    public void createChessGame(final int roomId, final String currentTurnTeam, final boolean isPlaying) {
        String sql = "INSERT INTO chess_game (game_id, current_turn_team, is_playing) VALUES (?, ?, ?)";
        this.jdbcTemplate.update(sql, roomId, currentTurnTeam, isPlaying);
    }

    public void createTeamInfo(final int roomId, final String team, final String pieceInfo) {
        final String sql = "INSERT INTO team_info (game_id, team, piece_info) VALUES (?, ?, ?)";
        this.jdbcTemplate.update(sql, roomId, team, pieceInfo);
    }

    public String readTeamInfo(final int roomId, final String team) {
        final String sql = "SELECT piece_info FROM team_info where team = (?) && game_id = (?)";
        return this.jdbcTemplate.queryForObject(sql, String.class, team, roomId);
    }

    public ChessGameTableDto readChessGame(final int roomId) {
        final String sql = "SELECT * FROM chess_game where game_id = (?)";
        return this.jdbcTemplate.queryForObject(sql, ChessGameInfoRowMapper, roomId);
    }

    public void updateChessGame(final int roomId, final String currentTurnTeam, final boolean isPlaying) {
        final String sql = "UPDATE chess_game SET current_turn_team = (?), is_playing = (?) where game_id = (?)";
        this.jdbcTemplate.update(sql, currentTurnTeam, isPlaying, roomId);
    }

    public void updateTeamInfo(final int roomId, final String team, final String pieceInfo) {
        final String sql = "UPDATE team_info SET piece_info = (?) WHERE team = (?) && game_id = (?)";
        this.jdbcTemplate.update(sql, pieceInfo, team, roomId);
    }

    public void deleteChessGame(final int roomId) {
        final String team_info_sql = "DELETE FROM team_info where game_id = (?)";
        final String chess_game_sql = "DELETE FROM chess_game where game_id = (?)";
        final String game_room_info_sql = "DELETE FROM game_room_info where game_id = (?)";
        this.jdbcTemplate.update(team_info_sql, roomId);
        this.jdbcTemplate.update(chess_game_sql, roomId);
        this.jdbcTemplate.update(game_room_info_sql, roomId);
    }
}